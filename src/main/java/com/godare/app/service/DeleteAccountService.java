package com.godare.app.service;

import com.godare.app.entity.*;
import com.godare.app.exception.BadRequestException;
import com.godare.app.repository.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DeleteAccountService {

    private static final Logger log = LoggerFactory.getLogger(DeleteAccountService.class);

    private static final String DEFAULT_PROFILE_URL =
            "https://res.cloudinary.com/dfeqlfptc/image/upload/v1774389485/default_profile_k6ojop.png";

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final PostInteractionRepository postInteractionRepository;
    private final UserChallengeRepository userChallengeRepository;
    private final NotificationRepository notificationRepository;
    private final PointsHistoryRepository pointsHistoryRepository;
    private final CloudinaryService cloudinaryService;
    private final MongoTemplate mongoTemplate;

    /**
     * Borra completamente la cuenta del usuario y todos sus datos asociados.
     */
    public void deleteAccount(String userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("User not found"));

        log.info("Iniciando borrado de cuenta para userId: {}", userId);

        // foto de perfil
        if (user.getUserImageUrl() != null &&
                !user.getUserImageUrl().equals(DEFAULT_PROFILE_URL)) {
            String publicId = extractPublicId(user.getUserImageUrl());
            if (publicId != null) {
                cloudinaryService.deleteFile(publicId, "image");
            }
        }

        deleteCloudinaryFiles(userId);

        deletePosts(userId);

        userChallengeRepository.deleteByOwnerId(userId);

        deleteNotifications(userId);

        pointsHistoryRepository.deleteByUserId(userId);

        removeFromFollowLists(userId);

        userRepository.deleteById(userId);

        log.info("Cuenta borrada correctamente para userId: {}", userId);
    }

    private void deleteCloudinaryFiles(String userId) {
        List<Post> posts = postRepository.findByUserId(userId);
        for (Post post : posts) {
            if (post.getMediaPublicId() != null && !post.getMediaPublicId().isBlank()) {
                String resourceType = cloudinaryService.inferResourceType(post.getMediaUrl());
                cloudinaryService.deleteFile(post.getMediaPublicId(), resourceType);
            }
        }

        // Borrar también los vídeos de retos completados
        List<UserChallenge> challenges = userChallengeRepository.findByOwnerId(userId);
        for (UserChallenge challenge : challenges) {
            if (challenge.getFilePublicId() != null && !challenge.getFilePublicId().isBlank()) {
                cloudinaryService.deleteFile(challenge.getFilePublicId(), "video");
            }
        }
    }

    private void deletePosts(String userId) {
        List<Post> posts = postRepository.findByUserId(userId);
        List<String> postIds = posts.stream().map(Post::getId).toList();

        // Borrar interacciones de esos posts
        if (!postIds.isEmpty()) {
            postInteractionRepository.deleteByPostIdIn(postIds);
        }

        // Borrar también las interacciones que el usuario hizo en posts ajenos
        postInteractionRepository.deleteByUserId(userId);

        // Borrar los posts
        postRepository.deleteByUserId(userId);
    }

    private void deleteNotifications(String userId) {
        // Borrar notificaciones recibidas por el usuario
        notificationRepository.deleteByUserId(userId);

        // Borrar notificaciones que referencian a este usuario como origen
        Query query = new Query(Criteria.where("data.fromUserId").is(userId));
        mongoTemplate.remove(query, "notifications");
    }

    private void removeFromFollowLists(String userId) {
        // Quitar al usuario de los arrays following[] y followers[] de otros
        Query query = new Query(Criteria.where("following").in(userId));
        Update pullFollowing = new Update().pull("following", userId);
        mongoTemplate.updateMulti(query, pullFollowing, "users");

        Query query2 = new Query(Criteria.where("followers").in(userId));
        Update pullFollowers = new Update().pull("followers", userId);
        mongoTemplate.updateMulti(query2, pullFollowers, "users");
    }

    private String extractPublicId(String url) {
        if (url == null || url.isBlank()) return null;
        try {
            String[] parts = url.split("/upload/");
            if (parts.length < 2) return null;
            String afterUpload = parts[1];
            if (afterUpload.startsWith("v") && afterUpload.contains("/")) {
                afterUpload = afterUpload.substring(afterUpload.indexOf("/") + 1);
            }
            int dotIndex = afterUpload.lastIndexOf(".");
            if (dotIndex > 0) afterUpload = afterUpload.substring(0, dotIndex);
            return afterUpload;
        } catch (Exception e) {
            return null;
        }
    }
}
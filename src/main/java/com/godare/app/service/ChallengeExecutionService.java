package com.godare.app.service;

import com.godare.app.dto.CompleteChallengeRequest;
import com.godare.app.entity.*;
import com.godare.app.exception.BadRequestException;
import com.godare.app.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;

// Servicio que gestiona la logica de completar un reto fitness.
// Cuando el usuario completa un reto se le asignan puntos y monedas,
// se crea un post en el feed y se actualiza su rango si procede.
@Service
@RequiredArgsConstructor
public class ChallengeExecutionService {

    private final UserRepository userRepository;
    private final UserChallengeRepository userChallengeRepository;
    private final PostRepository postRepository;
    private final PointsHistoryRepository pointsHistoryRepository;
    private final RangeService rangeService;
    private final NotificationService notificationService;

    // Recompensas por completar un reto fitness
    private static final int DARE_POINTS = 40;
    private static final int DARE_COINS  = 8;

    // Los posts del feed caducan a los 7 dias
    private static final long POST_EXPIRY_SECONDS = 60L * 60 * 24 * 7;

    // Marca el reto como completado, guarda el video y crea el post en el feed
    public String completeChallenge(String userId, String userChallengeId,
                                    CompleteChallengeRequest request) {

        Instant now = Instant.now();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("User not found"));

        UserChallenge challenge = userChallengeRepository.findById(userChallengeId)
                .orElseThrow(() -> new BadRequestException("Challenge not found"));

        if (!challenge.getOwnerId().equals(userId)) {
            throw new BadRequestException(
                    "This challenge does not belong to the authenticated user");
        }

        if (!"OPEN".equals(challenge.getState())) {
            throw new BadRequestException("Challenge is not pending");
        }

        // Actualizar el estado del reto
        challenge.setState("PASSED");
        challenge.setDatePassed(now);
        challenge.setFileUrl(request.getFileUrl());
        challenge.setFinalScore(DARE_POINTS);

        String mediaPublicId = extractPublicId(request.getFileUrl());
        challenge.setFilePublicId(mediaPublicId);
        userChallengeRepository.save(challenge);

        // Dar puntos y monedas al usuario
        applyRewards(user, userId, challenge.getId(),
                "COMPLETED_DARE", DARE_POINTS, DARE_COINS, now);

        // Crear el post que aparecera en el feed de seguidores
        Post post = new Post();
        post.setUserId(userId);
        post.setPostType("DARE");
        post.setReferenceId(challenge.getId());
        post.setTextResponse(challenge.getChallengeQuestion());
        post.setMediaUrl(request.getFileUrl());
        post.setMediaPublicId(mediaPublicId);
        post.setCreatedAt(now);
        post.setExpiresAt(now.plusSeconds(POST_EXPIRY_SECONDS));
        post.setLikesCount(0);
        post.setDislikesCount(0);
        post.setCommentsCount(0);
        postRepository.save(post);

        return "Challenge completed successfully";
    }

    // Aplica las recompensas al usuario: suma puntos, monedas y sube de rango si toca
    private void applyRewards(User user, String userId, String referenceId,
                              String reason, int points, int coins, Instant now) {
        int previousRange = user.getRange();

        user.setPoints(user.getPoints() + points);

        Range range = rangeService.getRangeByPoints(user.getPoints());
        user.setRange(range.getLevel());

        user.setCoinsApp(user.getCoinsApp() + coins);
        userRepository.save(user);

        // Guardar el historial de puntos para poder auditarlo
        PointsHistory history = new PointsHistory();
        history.setUserId(userId);
        history.setReferenceId(referenceId);
        history.setPointsEarned(points);
        history.setCoinsEarned(coins);
        history.setDate(now);
        history.setReason(reason);
        pointsHistoryRepository.save(history);

        // Notificar al usuario si ha subido de rango
        if (range.getLevel() > previousRange) {
            notificationService.createNotification(
                    userId,
                    "RANGE_UP",
                    "Has subido de rango!",
                    "Ahora eres " + range.getName(),
                    Map.of("rangeLevel", range.getLevel())
            );
        }
    }

    // Extrae el publicId de una URL de Cloudinary para poder borrar el archivo si hace falta.
    // Ejemplo: https://res.cloudinary.com/cloud/video/upload/v123/mi_video.mp4 -> mi_video
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
            if (dotIndex > 0) {
                afterUpload = afterUpload.substring(0, dotIndex);
            }
            return afterUpload;
        } catch (Exception e) {
            return null;
        }
    }
}

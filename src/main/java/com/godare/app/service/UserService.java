package com.godare.app.service;

import com.godare.app.dto.*;
import com.godare.app.entity.PackStore;
import com.godare.app.entity.Range;
import com.godare.app.entity.User;
import com.godare.app.exception.BadRequestException;
import com.godare.app.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RangeRepository rangeRepository;
    private final UserChallengeRepository userChallengeRepository;
    private final PostRepository postRepository;
    private final PackStoreRepository packStoreRepository;
    private final NotificationService notificationService;
    private final CloudinaryService cloudinaryService;

    // perfil del usuario autenticado
    public MyProfileResponse getMyProfile(String userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("User not found"));

        Range range = rangeRepository.findByLevel(user.getRange())
                .orElseThrow(() -> new BadRequestException("Range not found"));

        return MyProfileResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .nick(user.getNick())
                .name(user.getName())
                .surnames(user.getSurnames())
                .userImageUrl(user.getUserImageUrl())
                .points(user.getPoints())
                .coinsApp(user.getCoinsApp())
                .rangeLevel(range.getLevel())
                .rangeName(range.getName())
                .rangeImageUrl(range.getImageUrl())
                .stats(buildProfileStats(userId))
                .followersNumber(user.getFollowers().size())
                .followedNumber(user.getFollowing().size())
                .following(user.getFollowing()) // IDs de usuarios seguidos
                .build();
    }

    // perfil publico (sin email ni coins)
    public UserProfileResponse getUserProfile(String userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("User not found"));

        Range range = rangeRepository.findByLevel(user.getRange())
                .orElseThrow(() -> new BadRequestException("Range not found"));

        return UserProfileResponse.builder()
                .id(user.getId())
                .nick(user.getNick())
                .name(user.getName())
                .surnames(user.getSurnames())
                .userImageUrl(user.getUserImageUrl())
                .points(user.getPoints())
                .rangeLevel(range.getLevel())
                .rangeName(range.getName())
                .rangeImageUrl(range.getImageUrl())
                .stats(buildProfileStats(userId))
                .followersCount(user.getFollowers().size())
                .followingCount(user.getFollowing().size())
                .build();
    }

    public void followUser(String myUserId, String targetUserId) {

        if (myUserId.equals(targetUserId)) {
            throw new BadRequestException("You cannot follow yourself");
        }

        User me = userRepository.findById(myUserId)
                .orElseThrow(() -> new BadRequestException("Authenticated user not found"));

        User target = userRepository.findById(targetUserId)
                .orElseThrow(() -> new BadRequestException("Target user not found"));

        if (me.getFollowing().contains(targetUserId)) {
            throw new BadRequestException("You already follow this user");
        }

        me.getFollowing().add(targetUserId);
        target.getFollowers().add(myUserId);

        userRepository.save(me);
        userRepository.save(target);

        // Notificar al usuario seguido
        notificationService.createNotification(
                target.getId(),
                "FOLLOW",
                "¡Nuevo seguidor!",
                me.getNick() + " ha empezado a seguirte",
                Map.of(
                        "fromUserId", me.getId(),
                        "fromUserNick", me.getNick()
                )
        );
    }

    public void unfollowUser(String myUserId, String targetUserId) {

        if (myUserId.equals(targetUserId)) {
            throw new BadRequestException("You cannot unfollow yourself");
        }

        User me = userRepository.findById(myUserId)
                .orElseThrow(() -> new BadRequestException("Authenticated user not found"));

        User target = userRepository.findById(targetUserId)
                .orElseThrow(() -> new BadRequestException("Target user not found"));

        if (!me.getFollowing().contains(targetUserId)) {
            throw new BadRequestException("You do not follow this user");
        }

        me.getFollowing().remove(targetUserId);
        target.getFollowers().remove(myUserId);

        userRepository.save(me);
        userRepository.save(target);
    }

    // una sola query para todos los followers
    public List<FollowUserResponse> getFollowers(String userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("User not found"));

        if (user.getFollowers().isEmpty()) return List.of();

        // Una sola query para traer todos los usuarios de golpe
        Map<String, User> followersMap = userRepository.findAllById(user.getFollowers())
                .stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));

        return user.getFollowers().stream()
                .map(followersMap::get)
                .filter(java.util.Objects::nonNull)
                .map(this::mapToFollowResponse)
                .toList();
    }

    // igual que getFollowers pero para following
    public List<FollowUserResponse> getFollowing(String userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("User not found"));

        if (user.getFollowing().isEmpty()) return List.of();

        Map<String, User> followingMap = userRepository.findAllById(user.getFollowing())
                .stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));

        return user.getFollowing().stream()
                .map(followingMap::get)
                .filter(java.util.Objects::nonNull)
                .map(this::mapToFollowResponse)
                .toList();
    }

    public MyProfileResponse updateMyProfile(String userId, UpdateProfileRequest request) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("User not found"));

        // Solo actualizar los campos que vienen informados
        if (request.getNick() != null && !request.getNick().isBlank()) {
            String newNick = request.getNick().toLowerCase().trim();
            if (!user.getNick().equals(newNick) && userRepository.existsByNick(newNick)) {
                throw new BadRequestException("El nick ya está en uso");
            }
            user.setNick(newNick);
        }

        if (request.getName() != null && !request.getName().isBlank()) {
            user.setName(request.getName());
        }

        if (request.getSurnames() != null) {
            user.setSurnames(request.getSurnames());
        }

        if (request.getUserImageUrl() != null) {
            user.setUserImageUrl(request.getUserImageUrl());
        }

        userRepository.save(user);

        return getMyProfile(userId);
    }

    // una query para los packs
    public List<PurchasedPackResponse> getMyPurchasedPacks(String userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("User not found"));

        if (user.getPacksPurchased().isEmpty()) return List.of();

        // Extraer todos los packIds y traerlos de una sola vez
        Set<Integer> packIds = user.getPacksPurchased().stream()
                .map(p -> p.getPackId())
                .collect(Collectors.toSet());

        Map<Integer, PackStore> packsMap = packStoreRepository.findAllById(packIds)
                .stream()
                .collect(Collectors.toMap(PackStore::getId, Function.identity()));

        return user.getPacksPurchased().stream()
                .map(purchasedPack -> {
                    PackStore pack = packsMap.get(purchasedPack.getPackId());
                    return PurchasedPackResponse.builder()
                            .packId(purchasedPack.getPackId())
                            .name(pack != null ? pack.getName() : "Pack eliminado")
                            .type(purchasedPack.getType())
                            .quantity(purchasedPack.getQuantity())
                            .purchaseDate(purchasedPack.getPurchaseDate())
                            .imageUrl(pack != null ? pack.getImageUrl() : null)
                            .build();
                })
                .toList();
    }

    // helper interno
    public User getAuthenticatedUser(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("Authenticated user not found"));
    }

    // stats del perfil
    private ProfileStatsResponse buildProfileStats(String userId) {

        long completedDares  = userChallengeRepository.countByOwnerIdAndState(userId, "PASSED");
        long failedDares     = userChallengeRepository.countByOwnerIdAndState(userId, "FAILED");
        long pendingDares    = userChallengeRepository.countByOwnerIdAndState(userId, "OPEN");

        // Posts activos — los que no han caducado + los que no tienen fecha de caducidad
        Instant now = Instant.now();
        long activePosts = postRepository.countByUserIdAndExpiresAtAfter(userId, now)
                + postRepository.countByUserIdAndExpiresAtIsNull(userId);

        return ProfileStatsResponse.builder()
                .completedDares(completedDares)
                .failedDares(failedDares)
                .pendingDares(pendingDares)
                .totalPosts(activePosts)
                .build();
    }

    // User -> FollowUserResponse
    private FollowUserResponse mapToFollowResponse(User user) {
        return FollowUserResponse.builder()
                .id(user.getId())
                .nick(user.getNick())
                .name(user.getName())
                .surnames(user.getSurnames())
                .userImageUrl(user.getUserImageUrl())
                .build();
    }

    public List<FollowUserResponse> searchUsers(String query, String requestingUserId) {

        if (query == null || query.trim().length() < 2) {
            throw new BadRequestException("La búsqueda debe tener al menos 2 caracteres");
        }

        Pageable pageable = PageRequest.of(0, 20);

        return userRepository
                .findByNickContainingIgnoreCaseAndIdNot(query.trim(), requestingUserId, pageable)
                .getContent()
                .stream()
                .map(user -> FollowUserResponse.builder()
                        .id(user.getId())
                        .nick(user.getNick())
                        .name(user.getName())
                        .surnames(user.getSurnames())
                        .userImageUrl(user.getUserImageUrl())
                        .build())
                .toList();
    }

    private static final String DEFAULT_PROFILE_URL =
            "https://res.cloudinary.com/dfeqlfptc/image/upload/v1774389485/default_profile_k6ojop.png";

    public void deleteOldProfileImage(String imageUrl) {
        if (imageUrl == null || imageUrl.equals(DEFAULT_PROFILE_URL)) return;
        String publicId = extractPublicId(imageUrl);
        if (publicId != null) {
            cloudinaryService.deleteFile(publicId, "image");
        }
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
package com.godare.app.controller;

import com.godare.app.dto.*;
import com.godare.app.service.DeleteAccountService;
import com.godare.app.service.UserPostService;
import com.godare.app.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserPostService userPostService;
    private final DeleteAccountService deleteAccountService;

    // Perfil completo del usuario autenticado (email, coins, stats...)
    @GetMapping("/me")
    public MyProfileResponse getMyProfile(Authentication authentication) {
        String userId = authentication.getName();
        return userService.getMyProfile(userId);
    }

    // Perfil público de cualquier usuario (sin datos sensibles)
    @GetMapping("/{id}")
    public UserProfileResponse getUserProfile(@PathVariable String id) {
        return userService.getUserProfile(id);
    }

    // Seguir a un usuario
    @PostMapping("/{id}/follow")
    public String followUser(@PathVariable String id, Authentication authentication) {
        String myUserId = authentication.getName();
        userService.followUser(myUserId, id);
        return "User followed successfully";
    }

    // Dejar de seguir a un usuario
    @DeleteMapping("/{id}/follow")
    public String unfollowUser(@PathVariable String id, Authentication authentication) {
        String myUserId = authentication.getName();
        userService.unfollowUser(myUserId, id);
        return "User unfollowed successfully";
    }

    // Lista de seguidores de un usuario
    @GetMapping("/{id}/followers")
    public List<FollowUserResponse> getFollowers(@PathVariable String id) {
        return userService.getFollowers(id);
    }

    // Lista de usuarios a los que sigue un usuario
    @GetMapping("/{id}/following")
    public List<FollowUserResponse> getFollowing(@PathVariable String id) {
        return userService.getFollowing(id);
    }

    // Posts del usuario autenticado con paginación por cursor
    @GetMapping("/me/posts")
    public CursorFeedResponse getMyPosts(
            Authentication authentication,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) String cursorCreatedAt,
            @RequestParam(required = false) String cursorId
    ) {
        String userId = authentication.getName();

        Instant cursorInstant = null;
        if (cursorCreatedAt != null && !cursorCreatedAt.isBlank()) {
            cursorInstant = Instant.parse(cursorCreatedAt);
        }

        return userPostService.getUserPosts(userId, userId, limit, cursorInstant, cursorId);
    }

    // Posts de cualquier usuario con paginación por cursor
    @GetMapping("/{id}/posts")
    public CursorFeedResponse getUserPosts(
            @PathVariable String id,
            Authentication authentication,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) String cursorCreatedAt,
            @RequestParam(required = false) String cursorId
    ) {
        String viewerUserId = authentication.getName();

        Instant cursorInstant = null;
        if (cursorCreatedAt != null && !cursorCreatedAt.isBlank()) {
            cursorInstant = Instant.parse(cursorCreatedAt);
        }

        return userPostService.getUserPosts(viewerUserId, id, limit, cursorInstant, cursorId);
    }

    // Actualiza los datos del perfil del usuario autenticado
    @PatchMapping("/me")
    public MyProfileResponse updateMyProfile(@RequestBody UpdateProfileRequest request,
                                             Authentication authentication) {
        String userId = authentication.getName();
        return userService.updateMyProfile(userId, request);
    }

    // Packs comprados por el usuario autenticado
    @GetMapping("/me/packs")
    public List<PurchasedPackResponse> getMyPurchasedPacks(Authentication authentication) {
        String userId = authentication.getName();
        return userService.getMyPurchasedPacks(userId);
    }

    // Busca usuarios por nick (mínimo 2 caracteres)
    @GetMapping("/search")
    public List<FollowUserResponse> searchUsers(
            @RequestParam String q,
            Authentication authentication) {
        String userId = authentication.getName();
        return userService.searchUsers(q, userId);
    }

    // Borra una imagen de perfil anterior de Cloudinary
    @DeleteMapping("/me/profile-image")
    public String deleteOldProfileImage(
            @RequestParam String imageUrl,
            Authentication authentication) {
        userService.deleteOldProfileImage(imageUrl);
        return "Image deleted";
    }

    // Elimina la cuenta del usuario autenticado y todos sus datos
    @DeleteMapping("/me")
    public String deleteAccount(Authentication authentication) {
        String userId = authentication.getName();
        deleteAccountService.deleteAccount(userId);
        return "Account deleted successfully";
    }
}
package com.godare.app.controller;

import com.godare.app.dto.NotificationResponse;
import com.godare.app.dto.UnreadCountResponse;
import com.godare.app.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/me")
    public List<NotificationResponse> getMyNotifications(Authentication authentication) {
        String userId = authentication.getName();
        return notificationService.getMyNotifications(userId);
    }

    @GetMapping("/me/unread-count")
    public UnreadCountResponse getUnreadCount(Authentication authentication) {
        String userId = authentication.getName();
        return notificationService.getUnreadCount(userId);
    }

    @PatchMapping("/{id}/read")
    public NotificationResponse markAsRead(@PathVariable String id, Authentication authentication) {
        String userId = authentication.getName();
        return notificationService.markAsRead(userId, id);
    }

    @PatchMapping("/me/read-all")
    public String markAllAsRead(Authentication authentication) {
        String userId = authentication.getName();
        return notificationService.markAllAsRead(userId);
    }
}
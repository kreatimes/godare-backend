package com.godare.app.service;

import com.godare.app.dto.NotificationResponse;
import com.godare.app.dto.UnreadCountResponse;
import com.godare.app.entity.Notification;
import com.godare.app.exception.BadRequestException;
import com.godare.app.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    // Crea y persiste una nueva notificación para el usuario indicado
    public void createNotification(String userId,
                                   String type,
                                   String title,
                                   String message,
                                   Map<String, Object> data) {

        Notification notification = Notification.builder()
                .userId(userId)
                .type(type)
                .title(title)
                .message(message)
                .read(false)
                .data(data)
                .createdAt(Instant.now())
                .build();

        notificationRepository.save(notification);
    }

    // Devuelve todas las notificaciones del usuario ordenadas de más reciente a más antigua
    public List<NotificationResponse> getMyNotifications(String userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // Devuelve el número de notificaciones no leídas del usuario
    public UnreadCountResponse getUnreadCount(String userId) {
        return UnreadCountResponse.builder()
                .unreadCount(notificationRepository.countByUserIdAndReadFalse(userId))
                .build();
    }

    // Marca una notificación concreta como leída, verificando que pertenece al usuario
    public NotificationResponse markAsRead(String userId, String notificationId) {

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new BadRequestException("Notification not found"));

        if (!notification.getUserId().equals(userId)) {
            throw new BadRequestException("This notification does not belong to the authenticated user");
        }

        notification.setRead(true);
        notificationRepository.save(notification);

        return mapToResponse(notification);
    }

    /**
     * Marca todas las notificaciones no leídas del usuario como leídas.
     * Usa una query de actualización masiva directa en Mongo (@Update),
     * sin cargar ningún documento en memoria.
     */
    public String markAllAsRead(String userId) {
        notificationRepository.markAllAsReadByUserId(userId);
        return "All notifications marked as read";
    }

    // Convierte una entidad Notification en su DTO de respuesta
    private NotificationResponse mapToResponse(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .type(notification.getType())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .read(notification.isRead())
                .data(notification.getData())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
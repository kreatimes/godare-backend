package com.godare.app.repository;

import com.godare.app.entity.Notification;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.Update;

import java.util.List;

public interface NotificationRepository extends MongoRepository<Notification, String> {

    // Devuelve todas las notificaciones del usuario ordenadas por fecha descendente
    List<Notification> findByUserIdOrderByCreatedAtDesc(String userId);

    // Cuenta las notificaciones no leídas del usuario
    long countByUserIdAndReadFalse(String userId);

    // Marca todas las notificaciones no leídas del usuario como leídas directamente en Mongo
    // Sin traerse ningún documento a memoria
    @Query("{ 'userId': ?0, 'read': false }")
    @Update("{ '$set': { 'read': true } }")
    void markAllAsReadByUserId(String userId);

    void deleteByUserId(String userId);
}
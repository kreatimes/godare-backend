package com.godare.app.entity;

// Notificacion que se muestra al usuario (reto recibido, nuevo seguidor, subida de rango).

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Map;

@Document(collection = "notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@CompoundIndex(name = "user_read_created_idx", def = "{'userId':1, 'read':1, 'createdAt':-1}")
public class Notification {

    @Id
    private String id;

    @Indexed
    private String userId;

    private String type;

    private String title;

    private String message;

    private boolean read;

    private Map<String, Object> data;

    @Indexed
    private Instant createdAt;
}
package com.godare.app.entity;

// Publicacion en el feed creada automaticamente al completar un reto.

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "posts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@CompoundIndex(name = "user_created_idx", def = "{'userId':1, 'createdAt':-1}")
public class Post {

    @Id
    private String id;

    private String userId;

    private String postType;

    private String referenceId;

    private String textResponse;

    private String mediaUrl;

    private String mediaPublicId; // ID público en Cloudinary para borrado

    @Indexed
    private Instant createdAt;

    @Indexed(expireAfter = "0s")
    private Instant expiresAt;

    private int likesCount;
    private int dislikesCount;
    private int commentsCount;

    // Votos de validación: ¿el reto fue realmente superado?
    private int approvedCount;
    private int notApprovedCount;

    // true una vez que el scheduler ha evaluado el veredicto de este post.
    // Evita re-procesar posts ya evaluados en cada pasada del scheduler.
    @Builder.Default
    private boolean verdictProcessed = false;
}
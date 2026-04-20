package com.godare.app.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

// DTO que representa un post en el feed.
// textResponse contiene el texto del reto que completo el usuario.
@Data
@Builder
public class PostResponse {

    private String id;

    private String userId;
    private String userNick;
    private String userImageUrl;

    private String postType;     // Por ahora siempre DARE
    private String textResponse; // Texto del reto completado
    private String mediaUrl;     // URL del video subido a Cloudinary

    private int likesCount;
    private int dislikesCount;
    private int commentsCount;

    private String currentReaction; // Reaccion del usuario que consulta el feed (LIKE, DISLIKE o NONE)

    private Instant createdAt;
}

package com.godare.app.entity;

// Reporte de contenido inapropiado enviado por un usuario.

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Document(collection = "reports")
public class Report {

    @Id
    private String id;

    @Indexed
    private String reportedId;    // ID del post, usuario o comentario reportado

    private String reportedBy;    // ID del usuario que reporta

    private String type;          // POST, USER, COMMENT

    private String reason;        // SEXUAL_CONTENT, VIOLENCE, SPAM, HATE_SPEECH

    private String description;   // Descripción adicional opcional

    private String status;        // PENDING, REVIEWED, IGNORED, ACTION_TAKEN

    private String reviewedBy;    // ID del moderador que lo revisó

    private Instant reviewDate;

    @Indexed
    private Instant createdAt;
}
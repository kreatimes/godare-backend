package com.godare.app.entity;

// Registro historico de puntos y monedas ganados por el usuario.

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "points_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PointsHistory {

    @Id
    private String id;

    @Indexed
    private String userId;

    @Indexed
    private String referenceId;

    private int pointsEarned;

    private int coinsEarned;

    @Indexed
    private Instant date;

    private String reason;
}
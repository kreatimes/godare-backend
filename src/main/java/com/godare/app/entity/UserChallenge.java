package com.godare.app.entity;

// Reto asignado a un usuario concreto. Se crea al comprar un pack o recibir el daily.

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "user_challenges")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@CompoundIndex(name = "owner_state_idx", def = "{'ownerId':1, 'state':1}")
public class UserChallenge {

    @Id
    private String id;

    private String challengeId;

    @Indexed
    private String ownerId;

    private String challengeQuestion;

    private String challengeType;

    private String state;

    private Instant arrivalDate;

    @Indexed
    private Instant expiresAt;

    private Instant datePassed;

    private String fileUrl;

    private String filePublicId;

    private int finalScore;

    @Indexed
    private String transferredTo;

    private Instant transferDate;

    private boolean isDaily;

    // contadores del veredicto, se copian al cerrar la votacion
    @Builder.Default
    private int approvedCount = 0;

    @Builder.Default
    private int notApprovedCount = 0;
}
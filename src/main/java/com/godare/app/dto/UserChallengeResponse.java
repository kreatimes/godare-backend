package com.godare.app.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class UserChallengeResponse {

    private String id;
    private String challengeId;
    private String challengeQuestion;
    private String state;        // OPEN, PASSED, EXPIRED, TRANSFERRED, FAILED_BY_VOTE
    private boolean isDaily;
    private Instant arrivalDate;
    private Instant expiresAt;
    private Instant datePassed;
    private String fileUrl;
    private int finalScore;

    // Contadores de veredicto de la comunidad (visibles siempre en el perfil)
    private int approvedCount;
    private int notApprovedCount;
}
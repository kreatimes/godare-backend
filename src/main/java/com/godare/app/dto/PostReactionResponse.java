package com.godare.app.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PostReactionResponse {

    private String message;
    private int likesCount;
    private int dislikesCount;
    private String currentReaction; // LIKE, DISLIKE, NONE

    // Veredicto de si el reto fue superado
    private int approvedCount;
    private int notApprovedCount;
    private String currentVerdict; // APPROVED, NOT_APPROVED, NONE
}
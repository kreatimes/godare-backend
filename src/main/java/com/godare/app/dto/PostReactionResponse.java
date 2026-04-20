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
}
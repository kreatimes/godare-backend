package com.godare.app.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class CommentResponse {

    private String id;
    private String postId;
    private String userId;
    private String userNick;
    private String userImageUrl;
    private String commentText;
    private Instant createdAt;
}
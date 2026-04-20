package com.godare.app.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
@Builder
public class PostDetailResponse {

    private String id;

    private String userId;
    private String userNick;
    private String userImageUrl;

    private String postType;
    private String textResponse;
    private String mediaUrl;

    private int likesCount;
    private int dislikesCount;
    private int commentsCount;

    private String currentReaction;

    private Instant createdAt;

    private List<CommentResponse> comments;
}
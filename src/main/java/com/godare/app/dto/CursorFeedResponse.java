package com.godare.app.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
@Builder
public class CursorFeedResponse {

    private List<PostResponse> content;

    private Instant nextCursorCreatedAt;
    private String nextCursorId;

    private boolean hasMore;
}
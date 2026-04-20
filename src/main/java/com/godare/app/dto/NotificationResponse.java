package com.godare.app.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.Map;

@Data
@Builder
public class NotificationResponse {

    private String id;
    private String type;

    private String title;
    private String message;

    private boolean read;

    private Map<String, Object> data;

    private Instant createdAt;
}
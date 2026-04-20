package com.godare.app.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UnreadCountResponse {

    private long unreadCount;
}
package com.godare.app.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FollowUserResponse {
    private String id;
    private String nick;
    private String name;
    private String surnames;
    private String userImageUrl;
}
package com.godare.app.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserProfileResponse {

    private String id;
    private String email;
    private String nick;
    private String name;
    private String surnames;
    private String userImageUrl;

    private int points;
    private int coinsApp;

    private int rangeLevel;
    private String rangeName;
    private String rangeImageUrl;
    private ProfileStatsResponse stats;

    private int followersCount;
    private int followingCount;
}
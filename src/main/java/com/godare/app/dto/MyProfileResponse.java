package com.godare.app.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class MyProfileResponse {

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

    private int followersNumber;
    private int followedNumber;

    // IDs de usuarios a los que sigue el usuario autenticado.
    // Usado en el front para calcular isFollowing sin llamadas extra al back.
    private List<String> following;
}
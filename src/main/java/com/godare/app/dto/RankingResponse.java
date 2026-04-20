package com.godare.app.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RankingResponse {

    private String userId;
    private String nick;
    private String userImageUrl;

    private int points;

    private int rangeLevel;
    private String rangeName;
    private String rangeImageUrl;

    private int position;
}
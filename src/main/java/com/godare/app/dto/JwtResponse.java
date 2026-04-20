package com.godare.app.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class JwtResponse {

    private String token;
    private String type;

    private String id;
    private String nick;
    private String email;

    private int points;
    private int coinsApp;
    private int range;
}
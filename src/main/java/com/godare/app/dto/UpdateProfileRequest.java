package com.godare.app.dto;

import lombok.Data;

@Data
public class UpdateProfileRequest {

    private String name;
    private String surnames;
    private String nick;
    private String userImageUrl;
}
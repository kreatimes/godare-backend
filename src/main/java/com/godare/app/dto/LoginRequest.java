package com.godare.app.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {
    @NotBlank
    private String emailOrNick;  // Permitir login con email o nick

    @NotBlank
    private String password;
}
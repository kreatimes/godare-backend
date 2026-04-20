package com.godare.app.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {

    @Email
    @NotBlank
    private String email;

    @NotBlank
    @Size(min = 3, max = 20)
    private String nick;

    @NotBlank
    @Size(min = 6)
    private String password;

    @NotBlank
    private String name;

    private String surnames;
}
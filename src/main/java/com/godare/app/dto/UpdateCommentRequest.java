package com.godare.app.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateCommentRequest {

    @NotBlank
    private String commentText;
}
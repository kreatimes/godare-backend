package com.godare.app.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class TransferResponse {

    private String message;
    private String newOwnerId;
    private Instant transferDate;
}
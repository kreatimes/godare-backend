package com.godare.app.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PackPurchaseResponse {

    private String message;
    private int remainingCoins;
    private int generatedItems;
}
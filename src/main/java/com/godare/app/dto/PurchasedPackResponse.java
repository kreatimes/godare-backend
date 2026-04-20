package com.godare.app.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class PurchasedPackResponse {

    private Integer packId;
    private String name;
    private String type;
    private int quantity;
    private Instant purchaseDate;
    private String imageUrl;
}
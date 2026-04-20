package com.godare.app.entity;

// Registro de un pack comprado por el usuario, guardado dentro del documento User.

import lombok.Data;

import java.time.Instant;

@Data
public class PurchasedPack {

    private Integer packId;
    private String type;
    private int quantity;
    private Instant purchaseDate;
}
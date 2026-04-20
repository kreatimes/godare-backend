package com.godare.app.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

// Entidad que representa un pack de retos disponible en la tienda.
// Los packs contienen retos fitness que se asignan al usuario al comprarlos.
@Data
@Document(collection = "packs_store")
public class PackStore {

    @Id
    private Integer id;

    private String name;
    private String type;        // DARE — tipo de reto (solo fitness por ahora)
    private int quantity;       // Numero de retos que incluye el pack
    private int priceCoins;     // Precio en monedas de la app
    private String description;
    private String imageUrl;
    private boolean active;     // Si es false no aparece en la tienda
}

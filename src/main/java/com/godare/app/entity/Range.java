package com.godare.app.entity;

// Rango del sistema de niveles. El usuario sube de rango al acumular puntos.

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "ranges")
public class Range {

    @Id
    private Integer id;

    private String name;
    private Integer level;
    private Integer minPoints;
    private Integer maxPoints;
    private String imageUrl;
}
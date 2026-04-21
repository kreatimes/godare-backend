package com.godare.app.entity;

// Reto del catalogo

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Document(collection = "challenges")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Challenge {

    @Id
    private String id;

    // Titulo visible del reto
    private String question;

    // Condiciones específicas que debe cumplir el reto al completarse
    private List<Map<String, Object>> conditions;

    // Archivo de ejemplo opcional para ilustrar el reto
    private String exampleFileUrl;

    // Ventana de tiempo en la que el reto puede ser asignado
    @Indexed
    private Instant officialChallengeStartDate;

    @Indexed
    private Instant officialChallengeEndDate;

    // Número de intentos permitidos para completar el reto
    private int attempts;

    // Dificultad del reto (escala numérica)
    private int difficulty;

    // Palabras clave para búsqueda o filtrado
    private List<String> keywords;

    // Solo los challenges activos se asignan a usuarios
    @Indexed
    private boolean active;
}
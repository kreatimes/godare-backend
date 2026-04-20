package com.godare.app.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

// DTO con el reto diario del usuario.
// Cada dia el sistema asigna un reto fitness aleatorio al usuario.
@Data
@Builder
public class DailyItemResponse {

    private String id;           // id del user_challenge asignado
    private String type;         // Siempre DARE para retos fitness
    private String title;        // Texto del reto a completar
    private String state;        // Estado: CLOSED, OPEN, PASSED, TRANSFERRED
    private Instant arrivalDate;
    private Instant expiresAt;
    private boolean generatedToday; // True si se acaba de generar en esta peticion
}

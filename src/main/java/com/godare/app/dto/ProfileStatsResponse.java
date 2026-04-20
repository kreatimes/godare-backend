package com.godare.app.dto;

import lombok.Builder;
import lombok.Data;

// DTO con las estadisticas de actividad del usuario.
// Se devuelve dentro de MyProfileResponse y UserProfileResponse.
@Data
@Builder
public class ProfileStatsResponse {

    private long completedDares;  // Retos completados con exito
    private long failedDares;     // Retos que expiraron sin completar
    private long pendingDares;    // Retos pendientes en este momento
    private long totalCompleted;  // Total historico de retos completados
    private long totalPosts;      // Posts publicados en el feed
}

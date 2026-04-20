package com.godare.app.controller;

import com.godare.app.dto.CompleteChallengeRequest;
import com.godare.app.service.ChallengeExecutionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

// Endpoint para marcar un reto como completado.
// El usuario sube la URL del video grabado y el servidor valida que le pertenece.
@RestController
@RequestMapping("/api/complete")
@RequiredArgsConstructor
public class ChallengeExecutionController {

    private final ChallengeExecutionService challengeExecutionService;

    // Completa un reto — recibe la URL del video subido a Cloudinary
    @PostMapping("/challenge/{id}")
    public String completeChallenge(@PathVariable String id,
                                    @RequestBody CompleteChallengeRequest request,
                                    Authentication authentication) {
        String userId = authentication.getName();
        return challengeExecutionService.completeChallenge(userId, id, request);
    }
}

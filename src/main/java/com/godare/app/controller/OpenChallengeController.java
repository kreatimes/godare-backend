package com.godare.app.controller;

import com.godare.app.service.OpenChallengeService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

// Endpoint para abrir un reto cerrado.
// El reto llega en estado CLOSED y pasa a OPEN cuando el usuario lo acepta.
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class OpenChallengeController {

    private final OpenChallengeService openChallengeService;

    // Abre un reto: CLOSED -> OPEN
    @PatchMapping("/challenges/{id}/open")
    public String openChallenge(@PathVariable String id,
                                Authentication authentication) {
        openChallengeService.openChallenge(authentication.getName(), id);
        return "Challenge opened";
    }
}

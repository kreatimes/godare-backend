package com.godare.app.controller;

import com.godare.app.dto.UserChallengeResponse;
import com.godare.app.service.MyChallengesService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Endpoint para consultar los retos del usuario autenticado.
// El usuario solo puede ver sus propios retos.
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class MyChallengesController {

    private final MyChallengesService myChallengesService;

    // Devuelve los retos del usuario autenticado, opcionalmente filtrados por estado.
    // Estado puede ser: CLOSED, OPEN, PASSED, EXPIRED, TRANSFERRED
    @GetMapping("/challenges/me")
    public List<UserChallengeResponse> getMyChallenges(
            @RequestParam(required = false) String state,
            Authentication authentication) {
        String userId = authentication.getName();
        return myChallengesService.getMyChallenges(userId, state);
    }

    // Devuelve los retos PASSED y EXPIRED de cualquier usuario por su ID.
    // Solo expone estados públicos — no muestra retos OPEN ni CLOSED de otros usuarios.
    @GetMapping("/users/{userId}/challenges")
    public List<UserChallengeResponse> getUserChallenges(
            @PathVariable String userId,
            @RequestParam(required = false) String state) {
        return myChallengesService.getUserChallengesPublic(userId, state);
    }
}
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

    // Devuelve los retos del usuario, opcionalmente filtrados por estado
    // Estado puede ser: CLOSED, OPEN, PASSED, EXPIRED, TRANSFERRED
    @GetMapping("/challenges/me")
    public List<UserChallengeResponse> getMyChallenges(
            @RequestParam(required = false) String state,
            Authentication authentication) {
        String userId = authentication.getName();
        return myChallengesService.getMyChallenges(userId, state);
    }
}

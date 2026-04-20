package com.godare.app.controller;

import com.godare.app.dto.TransferResponse;
import com.godare.app.service.TransferService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

// Endpoint para transferir un reto a otro usuario.
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class TransferController {

    private final TransferService transferService;

    // Transfiere un reto al usuario indicado
    @PostMapping("/challenges/{id}/transfer/{targetUserId}")
    public TransferResponse transferChallenge(@PathVariable String id,
                                              @PathVariable String targetUserId,
                                              Authentication authentication) {
        String userId = authentication.getName();
        return transferService.transferChallenge(userId, id, targetUserId);
    }
}

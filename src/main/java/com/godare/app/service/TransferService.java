package com.godare.app.service;

import com.godare.app.dto.TransferResponse;
import com.godare.app.entity.User;
import com.godare.app.entity.UserChallenge;
import com.godare.app.exception.BadRequestException;
import com.godare.app.repository.UserChallengeRepository;
import com.godare.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;

// Servicio que gestiona la transferencia de retos entre usuarios.
// Solo se pueden transferir retos en estado OPEN y a un usuario diferente al propietario.
@Service
@RequiredArgsConstructor
public class TransferService {

    private final UserRepository userRepository;
    private final UserChallengeRepository userChallengeRepository;
    private final NotificationService notificationService;

    // Transfiere un reto a otro usuario y le envia una notificacion
    public TransferResponse transferChallenge(String currentUserId, String challengeId, String targetUserId) {

        if (currentUserId.equals(targetUserId)) {
            throw new BadRequestException("You cannot transfer a challenge to yourself");
        }

        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new BadRequestException("Target user not found"));

        User fromUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new BadRequestException("Source user not found"));

        UserChallenge challenge = userChallengeRepository.findById(challengeId)
                .orElseThrow(() -> new BadRequestException("Challenge not found"));

        if (!challenge.getOwnerId().equals(currentUserId)) {
            throw new BadRequestException("This challenge does not belong to the authenticated user");
        }

        if (!"OPEN".equals(challenge.getState())) {
            throw new BadRequestException("Only pending challenges can be transferred");
        }

        Instant now = Instant.now();

        // Cambiar el propietario del reto al usuario destino
        challenge.setOwnerId(targetUser.getId());
        challenge.setTransferredTo(targetUser.getId());
        challenge.setTransferDate(now);
        challenge.setArrivalDate(now);

        userChallengeRepository.save(challenge);

        // Notificar al usuario que ha recibido un nuevo reto
        notificationService.createNotification(
                targetUser.getId(),
                "CHALLENGE_TRANSFERRED",
                "Nuevo reto recibido!",
                fromUser.getNick() + " te ha transferido un reto",
                Map.of(
                        "challengeId", challenge.getId(),
                        "fromUserId", fromUser.getId(),
                        "fromUserNick", fromUser.getNick()
                )
        );

        return TransferResponse.builder()
                .message("Challenge transferred successfully")
                .newOwnerId(targetUser.getId())
                .transferDate(now)
                .build();
    }
}

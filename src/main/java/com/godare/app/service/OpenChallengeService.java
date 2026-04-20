package com.godare.app.service;

import com.godare.app.entity.UserChallenge;
import com.godare.app.exception.BadRequestException;
import com.godare.app.repository.UserChallengeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

// Servicio que gestiona la apertura de retos cerrados.
// Un reto llega en estado CLOSED — el usuario lo abre para ver el contenido
// y pasa a OPEN, momento en el que tiene que completarlo.
@Service
@RequiredArgsConstructor
public class OpenChallengeService {

    private final UserChallengeRepository userChallengeRepository;

    // Abre un reto cerrado: CLOSED -> OPEN
    public void openChallenge(String userId, String challengeId) {
        UserChallenge challenge = userChallengeRepository.findById(challengeId)
                .orElseThrow(() -> new BadRequestException("Challenge not found"));

        if (!challenge.getOwnerId().equals(userId)) {
            throw new BadRequestException("This challenge does not belong to you");
        }

        if (!"CLOSED".equals(challenge.getState())) {
            throw new BadRequestException("Challenge is not closed");
        }

        challenge.setState("OPEN");
        userChallengeRepository.save(challenge);
    }
}

package com.godare.app.service;

import com.godare.app.dto.UserChallengeResponse;
import com.godare.app.exception.BadRequestException;
import com.godare.app.repository.UserChallengeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

// Servicio que gestiona los retos del usuario autenticado.
// Permite consultar los retos filtrados por estado.
@Service
@RequiredArgsConstructor
public class MyChallengesService {

    private final UserChallengeRepository userChallengeRepository;

    // Estados validos para los retos
    private static final List<String> VALID_CHALLENGE_STATES =
            List.of("CLOSED", "OPEN", "PASSED", "EXPIRED", "TRANSFERRED");

    // Estados que se pueden ver públicamente en el perfil de otro usuario
    private static final List<String> PUBLIC_CHALLENGE_STATES =
            List.of("PASSED", "EXPIRED");

    // Devuelve los retos del usuario autenticado. Si state es null devuelve todos.
    public List<UserChallengeResponse> getMyChallenges(String userId, String state) {

        if (state != null && !VALID_CHALLENGE_STATES.contains(state)) {
            throw new BadRequestException("Estado invalido: " + state);
        }

        final var challenges = state != null
                ? userChallengeRepository.findByOwnerIdAndStateOrderByArrivalDateDesc(userId, state)
                : userChallengeRepository.findByOwnerIdOrderByArrivalDateDesc(userId);

        return challenges.stream()
                .map(this::toResponse)
                .toList();
    }

    // Devuelve los retos públicos (PASSED / EXPIRED) de cualquier usuario.
    // Si se pasa un state concreto, se valida que sea uno de los públicos.
    public List<UserChallengeResponse> getUserChallengesPublic(String userId, String state) {

        if (state != null && !PUBLIC_CHALLENGE_STATES.contains(state)) {
            throw new BadRequestException("Estado no visible publicamente: " + state);
        }

        final var challenges = state != null
                ? userChallengeRepository.findByOwnerIdAndStateOrderByArrivalDateDesc(userId, state)
                : userChallengeRepository.findByOwnerIdAndStateOrderByArrivalDateDesc(userId, "PASSED");

        return challenges.stream()
                .map(this::toResponse)
                .toList();
    }

    private UserChallengeResponse toResponse(com.godare.app.entity.UserChallenge c) {
        return UserChallengeResponse.builder()
                .id(c.getId())
                .challengeId(c.getChallengeId())
                .challengeQuestion(c.getChallengeQuestion())
                .state(c.getState())
                .isDaily(c.isDaily())
                .arrivalDate(c.getArrivalDate())
                .expiresAt(c.getExpiresAt())
                .datePassed(c.getDatePassed())
                .fileUrl(c.getFileUrl())
                .finalScore(c.getFinalScore())
                .build();
    }
}
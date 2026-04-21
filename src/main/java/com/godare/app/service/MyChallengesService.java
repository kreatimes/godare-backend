package com.godare.app.service;

import com.godare.app.dto.UserChallengeResponse;
import com.godare.app.exception.BadRequestException;
import com.godare.app.repository.UserChallengeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MyChallengesService {

    private final UserChallengeRepository userChallengeRepository;

    private static final List<String> VALID_CHALLENGE_STATES =
            List.of("CLOSED", "OPEN", "PASSED", "EXPIRED", "TRANSFERRED");

    private static final List<String> PUBLIC_CHALLENGE_STATES =
            List.of("PASSED", "EXPIRED");

    // Cuando el cliente pide "EXPIRED" devolvemos también FAILED_BY_VOTE,
    // ya que ambos representan retos fallados en el perfil del usuario.
    private static final List<String> FAILED_STATES = List.of("EXPIRED", "FAILED_BY_VOTE");

    public List<UserChallengeResponse> getMyChallenges(String userId, String state) {

        if (state != null && !VALID_CHALLENGE_STATES.contains(state)) {
            throw new BadRequestException("Estado invalido: " + state);
        }

        final var challenges = state == null
                ? userChallengeRepository.findByOwnerIdOrderByArrivalDateDesc(userId)
                : "EXPIRED".equals(state)
                ? userChallengeRepository.findByOwnerIdAndStateInOrderByArrivalDateDesc(userId, FAILED_STATES)
                : userChallengeRepository.findByOwnerIdAndStateOrderByArrivalDateDesc(userId, state);

        return challenges.stream().map(this::toResponse).toList();
    }

    public List<UserChallengeResponse> getUserChallengesPublic(String userId, String state) {

        if (state != null && !PUBLIC_CHALLENGE_STATES.contains(state)) {
            throw new BadRequestException("Estado no visible publicamente: " + state);
        }

        final var challenges = state == null
                ? userChallengeRepository.findByOwnerIdAndStateOrderByArrivalDateDesc(userId, "PASSED")
                : "EXPIRED".equals(state)
                ? userChallengeRepository.findByOwnerIdAndStateInOrderByArrivalDateDesc(userId, FAILED_STATES)
                : userChallengeRepository.findByOwnerIdAndStateOrderByArrivalDateDesc(userId, state);

        return challenges.stream().map(this::toResponse).toList();
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
                .approvedCount(c.getApprovedCount())
                .notApprovedCount(c.getNotApprovedCount())
                .build();
    }
}
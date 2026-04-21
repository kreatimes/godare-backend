package com.godare.app.controller;

import com.godare.app.dto.UserChallengeResponse;
import com.godare.app.service.MyChallengesService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class MyChallengesController {

    private final MyChallengesService myChallengesService;

    @GetMapping("/challenges/me")
    public List<UserChallengeResponse> getMyChallenges(
            @RequestParam(required = false) String state,
            Authentication authentication) {
        String userId = authentication.getName();
        return myChallengesService.getMyChallenges(userId, state);
    }

    @GetMapping("/users/{userId}/challenges")
    public List<UserChallengeResponse> getUserChallenges(
            @PathVariable String userId,
            @RequestParam(required = false) String state) {
        return myChallengesService.getUserChallengesPublic(userId, state);
    }
}
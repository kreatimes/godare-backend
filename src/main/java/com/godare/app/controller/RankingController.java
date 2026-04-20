package com.godare.app.controller;

import com.godare.app.dto.RankingResponse;
import com.godare.app.service.RankingService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ranking")
@RequiredArgsConstructor
public class RankingController {

    private final RankingService rankingService;

    @GetMapping("/global")
    public List<RankingResponse> getGlobalRanking(
            @RequestParam(defaultValue = "10") int limit
    ) {
        return rankingService.getGlobalRanking(limit);
    }

    @GetMapping("/following")
    public List<RankingResponse> getFollowingRanking(
            Authentication authentication,
            @RequestParam(defaultValue = "10") int limit
    ) {
        String userId = authentication.getName();
        return rankingService.getFollowingRanking(userId, limit);
    }
}
package com.godare.app.controller;

import com.godare.app.dto.DailyItemResponse;
import com.godare.app.service.DailyService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/daily")
@RequiredArgsConstructor
public class DailyController {

    private final DailyService dailyService;

    @GetMapping("/me")
    public DailyItemResponse getMyDaily(Authentication authentication) {
        String userId = authentication.getName();
        return dailyService.getOrGenerateDaily(userId);
    }
}
package com.godare.app.controller;

import com.godare.app.entity.Range;
import com.godare.app.service.RangeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ranges")
@RequiredArgsConstructor
public class RangeController {

    private final RangeService rangeService;

    @GetMapping
    public List<Range> getAllRanges() {
        return rangeService.getAllRanges();
    }

    @GetMapping("/{level}")
    public Range getRangeByLevel(@PathVariable Integer level) {
        return rangeService.getRangeByLevel(level);
    }
}
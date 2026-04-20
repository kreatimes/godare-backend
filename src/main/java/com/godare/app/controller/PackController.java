package com.godare.app.controller;

import com.godare.app.dto.PackPurchaseResponse;
import com.godare.app.entity.PackStore;
import com.godare.app.service.PackService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/packs")
@RequiredArgsConstructor
public class PackController {

    private final PackService packService;

    @GetMapping
    public List<PackStore> getAvailablePacks() {
        return packService.getAvailablePacks();
    }

    @PostMapping("/{id}/buy")
    public PackPurchaseResponse buyPack(@PathVariable Integer id, Authentication authentication) {
        String userId = authentication.getName();
        return packService.buyPack(userId, id);
    }
}
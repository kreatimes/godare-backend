package com.godare.app.controller;

import com.godare.app.dto.*;
import com.godare.app.entity.User;
import com.godare.app.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public User register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public JwtResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @GetMapping("/verify")
    public String verifyAccount(@RequestParam String token) {
        return authService.verifyAccount(token);
    }

    @PostMapping("/forgot-password")
    public String forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        return authService.forgotPassword(request);
    }

    @PostMapping("/reset-password")
    public String resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        return authService.resetPassword(request);
    }

    @PatchMapping("/change-password")
    public String changePassword(@RequestBody ChangePasswordRequest request,
                                 Authentication authentication) {
        String userId = authentication.getName();
        return authService.changePassword(userId, request);
    }

    @PostMapping("/verify-password")
    public boolean verifyPassword(@RequestBody VerifyPasswordRequest request,
                                  Authentication authentication) {
        String userId = authentication.getName();
        return authService.verifyPassword(userId, request.getPassword());
    }

    // Reenvía el email de verificación al correo indicado.
    // Respuestas: 200 con email ofuscado, 400 EMAIL_NOT_FOUND o EMAIL_ALREADY_VERIFIED
    @PostMapping("/resend-verification")
    public String resendVerification(@RequestBody java.util.Map<String, String> body) {
        String email = body.get("email");
        if (email == null || email.isBlank()) {
            throw new com.godare.app.exception.BadRequestException("El email es obligatorio");
        }
        return authService.resendVerificationEmail(email);
    }
}
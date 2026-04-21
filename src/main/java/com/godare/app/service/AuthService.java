package com.godare.app.service;

import com.godare.app.dto.*;
import com.godare.app.entity.User;
import com.godare.app.exception.BadRequestException;
import com.godare.app.exception.UnauthorizedException;
import com.godare.app.repository.UserRepository;
import com.godare.app.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final EmailService emailService;

    public User register(RegisterRequest request) {

        String email = request.getEmail().toLowerCase().trim();
        // Nick siempre en minúsculas para evitar duplicados case-insensitive
        String nick = request.getNick().toLowerCase().trim();

        if (userRepository.existsByEmail(email)) {
            throw new BadRequestException("El email ya está en uso");
        }

        if (userRepository.existsByNick(nick)) {
            throw new BadRequestException("El nick ya está en uso");
        }

        String verificationToken = UUID.randomUUID().toString();

        User user = User.builder()
                .email(email)
                .nick(nick)
                .name(request.getName())
                .surnames(request.getSurnames())
                .password(passwordEncoder.encode(request.getPassword()))
                .creationDate(Instant.now())
                .userImageUrl("https://res.cloudinary.com/dfeqlfptc/image/upload/v1774389485/default_profile_k6ojop.png")
                .points(0)
                .coinsApp(0)
                .range(1)
                .roles(List.of("USER"))
                .emailVerified(false)
                .temporaryHash(verificationToken)
                .unactiveAccountDate(Instant.now().plusSeconds(60 * 15))
                .build();

        User savedUser = userRepository.save(user);

        emailService.sendVerificationEmail(email, user.getNick(), verificationToken);

        return savedUser;
    }

    public JwtResponse login(LoginRequest request) {

        // Normalizar input — tanto email como nick a minúsculas
        String input = request.getEmailOrNick().toLowerCase().trim();

        User user = userRepository.findByEmail(input)
                .orElseGet(() ->
                        userRepository.findByNick(input)
                                .orElseThrow(() -> new BadRequestException("Usuario o contraseña incorrectos"))
                );

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new UnauthorizedException("Usuario o contraseña incorrectos");
        }

        if (!user.isEmailVerified()) {
            // Reenviar email de verificación con nuevo token
            String newToken = UUID.randomUUID().toString();
            user.setTemporaryHash(newToken);
            user.setUnactiveAccountDate(Instant.now().plusSeconds(60 * 15));
            userRepository.save(user);
            emailService.sendVerificationEmail(user.getEmail(), user.getNick(), newToken);

            // Devolver error especial con el email ofuscado para el front
            String emailOfuscado = ofuscarEmail(user.getEmail());
            throw new UnauthorizedException("EMAIL_NOT_VERIFIED:" + emailOfuscado);
        }

        String token = jwtService.generateToken(user);

        return JwtResponse.builder()
                .token(token)
                .type("Bearer")
                .id(user.getId())
                .nick(user.getNick())
                .email(user.getEmail())
                .points(user.getPoints())
                .coinsApp(user.getCoinsApp())
                .range(user.getRange())
                .build();
    }

    public String verifyAccount(String token) {

        User user = userRepository.findByTemporaryHash(token)
                .orElseThrow(() -> new BadRequestException("Token inválido"));

        if (user.getUnactiveAccountDate() == null ||
                user.getUnactiveAccountDate().isBefore(Instant.now())) {
            throw new BadRequestException("El enlace ha caducado");
        }

        user.setEmailVerified(true);
        user.setTemporaryHash(null);
        user.setUnactiveAccountDate(null);

        userRepository.save(user);

        return "Cuenta verificada correctamente";
    }

    public String forgotPassword(ForgotPasswordRequest request) {

        User user = userRepository.findByEmail(request.getEmail().toLowerCase().trim())
                .orElseThrow(() -> new BadRequestException("No existe ninguna cuenta con ese email"));

        String resetToken = UUID.randomUUID().toString();

        user.setTemporaryHash(resetToken);
        user.setUnactiveAccountDate(Instant.now().plusSeconds(60 * 15));

        userRepository.save(user);

        emailService.sendPasswordResetEmail(user.getEmail(), user.getNick(), resetToken);

        return "Email de recuperación enviado";
    }

    public String resetPassword(ResetPasswordRequest request) {

        User user = userRepository.findByTemporaryHash(request.getToken())
                .orElseThrow(() -> new BadRequestException("Token inválido"));

        if (user.getUnactiveAccountDate() == null ||
                user.getUnactiveAccountDate().isBefore(Instant.now())) {
            throw new BadRequestException("El enlace ha caducado");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setTemporaryHash(null);
        user.setUnactiveAccountDate(null);

        userRepository.save(user);

        return "Contraseña restablecida correctamente";
    }

    public String changePassword(String userId, ChangePasswordRequest request) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("Usuario no encontrado"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BadRequestException("La contraseña actual no es correcta");
        }

        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new BadRequestException("La nueva contraseña debe ser distinta a la actual");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        return "Contraseña actualizada correctamente";
    }

    public boolean verifyPassword(String userId, String password) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("Usuario no encontrado"));
        return passwordEncoder.matches(password, user.getPassword());
    }

    /**
     * Reenvía el email de verificación de cuenta.
     * - Si el email no existe → BadRequestException
     * - Si ya está verificado → BadRequestException
     * - Si no está verificado → genera nuevo token y envía email
     */
    public String resendVerificationEmail(String email) {
        String normalizedEmail = email.toLowerCase().trim();

        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new BadRequestException("EMAIL_NOT_FOUND"));

        if (user.isEmailVerified()) {
            throw new BadRequestException("EMAIL_ALREADY_VERIFIED");
        }

        String newToken = UUID.randomUUID().toString();
        user.setTemporaryHash(newToken);
        user.setUnactiveAccountDate(Instant.now().plusSeconds(60 * 15));
        userRepository.save(user);

        emailService.sendVerificationEmail(user.getEmail(), user.getNick(), newToken);

        return ofuscarEmail(user.getEmail());
    }

    /**
     * Ofusca un email para mostrarlo al usuario sin revelar el completo.
     */
    private String ofuscarEmail(String email) {
        try {
            String[] parts = email.split("@");
            String user = parts[0];
            String domain = parts[1];

            String userOfuscado = user.charAt(0) +
                    "*".repeat(Math.max(1, user.length() - 1));

            String[] domainParts = domain.split("\\.");
            String domainOfuscado = domainParts[0].substring(0, Math.min(3, domainParts[0].length())) +
                    "*".repeat(Math.max(1, domainParts[0].length() - 3)) +
                    "." + domainParts[1];

            return userOfuscado + "@" + domainOfuscado;
        } catch (Exception e) {
            return "tu correo";
        }
    }
}
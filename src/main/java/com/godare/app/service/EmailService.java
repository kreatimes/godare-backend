package com.godare.app.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

// Servicio para enviar emails transaccionales usando la API de Brevo.
// Se usa para la verificacion de cuenta y el reseteo de contraseña.
@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private static final String BREVO_API_URL = "https://api.brevo.com/v3/smtp/email";

    @Value("${brevo.api-key}")
    private String brevoApiKey;

    @Value("${app.url:http://localhost:8080}")
    private String appUrl;

    @Value("${mail.from.email:felipesanfernando@gmail.com}")
    private String fromEmail;

    @Value("${mail.from.name:GoDare}")
    private String fromName;

    private final RestTemplate restTemplate = new RestTemplate();

    // Email de verificacion de cuenta que se envia al registrarse
    public void sendVerificationEmail(String toEmail, String nick, String token) {
        String verifyUrl = appUrl + "/api/auth/verify?token=" + token;

        String html = """
                <div style="font-family:Arial,sans-serif;max-width:500px;margin:0 auto">
                  <div style="background:#1a1a1a;padding:24px;text-align:center">
                    <h1 style="color:#e91e8c;margin:0;font-size:28px">GODARE</h1>
                  </div>
                  <div style="padding:32px;background:#ffffff">
                    <h2 style="color:#1a1a1a">Hola, %s!</h2>
                    <p style="color:#444;font-size:15px;line-height:1.6">
                      Gracias por registrarte en <strong>GoDare</strong>.
                      Para activar tu cuenta pulsa el boton de abajo:
                    </p>
                    <div style="text-align:center;margin:32px 0">
                      <a href="%s"
                         style="background:#e91e8c;color:#fff;padding:14px 32px;
                                border-radius:8px;text-decoration:none;font-weight:bold;
                                font-size:16px">
                        Verificar mi cuenta
                      </a>
                    </div>
                    <p style="color:#999;font-size:13px">
                      Si no te has registrado en GoDare puedes ignorar este email.
                      El enlace caduca en 15 minutos.
                    </p>
                  </div>
                  <div style="background:#f5f5f5;padding:16px;text-align:center">
                    <p style="color:#aaa;font-size:12px;margin:0">
                      &copy; 2026 GoDare
                    </p>
                  </div>
                </div>
                """.formatted(nick, verifyUrl);

        sendEmail(toEmail, "Verifica tu cuenta en GoDare", html);
    }

    // Email con el codigo para restablecer la contraseña
    public void sendPasswordResetEmail(String toEmail, String nick, String token) {
        String html = """
                <div style="font-family:Arial,sans-serif;max-width:500px;margin:0 auto">
                  <div style="background:#1a1a1a;padding:24px;text-align:center">
                    <h1 style="color:#e91e8c;margin:0;font-size:28px">GODARE</h1>
                  </div>
                  <div style="padding:32px;background:#ffffff">
                    <h2 style="color:#1a1a1a">Hola, %s!</h2>
                    <p style="color:#444;font-size:15px;line-height:1.6">
                      Hemos recibido una solicitud para restablecer la contrasena
                      de tu cuenta en <strong>GoDare</strong>.
                    </p>
                    <p style="color:#444;font-size:15px;line-height:1.6">
                      Introduce el siguiente codigo en la app:
                    </p>
                    <div style="text-align:center;margin:32px 0">
                      <div style="background:#f5f5f5;border:2px dashed #e91e8c;
                                  border-radius:12px;padding:20px;display:inline-block">
                        <span style="font-size:28px;font-weight:bold;
                                     color:#1a1a1a;letter-spacing:6px">%s</span>
                      </div>
                    </div>
                    <p style="color:#999;font-size:13px">
                      Si no has solicitado este cambio puedes ignorar este email.
                      El codigo caduca en 15 minutos.
                    </p>
                  </div>
                  <div style="background:#f5f5f5;padding:16px;text-align:center">
                    <p style="color:#aaa;font-size:12px;margin:0">
                      &copy; 2026 GoDare
                    </p>
                  </div>
                </div>
                """.formatted(nick, token);

        sendEmail(toEmail, "Codigo para restablecer tu contrasena en GoDare", html);
    }

    // Metodo interno que construye y envia la peticion a la API de Brevo
    private void sendEmail(String to, String subject, String html) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("api-key", brevoApiKey);
            headers.setAccept(List.of(MediaType.APPLICATION_JSON));

            Map<String, Object> body = Map.of(
                    "sender", Map.of(
                            "name", fromName,
                            "email", fromEmail
                    ),
                    "to", List.of(Map.of("email", to)),
                    "subject", subject,
                    "htmlContent", html
            );

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(
                    BREVO_API_URL, request, String.class);

            log.info("Email enviado correctamente a {} — status: {}",
                    to, response.getStatusCode());

        } catch (Exception e) {
            log.error("Error al enviar email a {}: {}", to, e.getMessage());
        }
    }
}
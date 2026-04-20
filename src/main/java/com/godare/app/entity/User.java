package com.godare.app.entity;

// Usuario registrado en la app. Incluye perfil, puntos, monedas y lista de seguidos.

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    private String id;

    // Credenciales únicas de acceso
    @Indexed(unique = true)
    private String email;

    @Indexed(unique = true)
    private String nick;

    private String password; // Almacenada hasheada con BCrypt

    // Datos personales
    private String name;
    private String surnames;
    private String userImageUrl;

    private Instant creationDate;

    // Hash temporal usado para activación de cuenta y recuperación de contraseña
    @Indexed
    private String temporaryHash;

    // Fecha límite para activar la cuenta; si se supera, la cuenta se considera inactiva
    private Instant unactiveAccountDate;

    private boolean emailVerified;

    // Roles del usuario: USER, ADMIN, MODERATOR...
    // @Builder.Default garantiza que la lista nunca sea null aunque se use el builder
    @Builder.Default
    private List<String> roles = new ArrayList<>();

    // Gamificación
    private int range;    // Nivel/rango actual del usuario
    private int points;   // Puntos acumulados totales
    private int coinsApp; // Moneda interna disponible para gastar en la tienda

    // Fecha del último daily asignado, para controlar que solo reciba uno por día
    private Instant lastDailyAssignedAt;

    // Sistema social
    @Builder.Default
    private List<String> followers = new ArrayList<>(); // IDs de usuarios que siguen a este usuario

    @Builder.Default
    private List<String> following = new ArrayList<>(); // IDs de usuarios a los que sigue este usuario

    // Historial de packs comprados en la tienda
    @Builder.Default
    private List<PurchasedPack> packsPurchased = new ArrayList<>();
}
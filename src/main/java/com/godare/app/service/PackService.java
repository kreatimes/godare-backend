package com.godare.app.service;

import com.godare.app.dto.PackPurchaseResponse;
import com.godare.app.entity.*;
import com.godare.app.exception.BadRequestException;
import com.godare.app.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// Servicio de la tienda de packs de retos fitness.
// Gestiona la compra de packs y la generacion de los retos asignados al usuario.
@Service
@RequiredArgsConstructor
public class PackService {

    private final PackStoreRepository packStoreRepository;
    private final UserRepository userRepository;
    private final ChallengeRepository challengeRepository;
    private final UserChallengeRepository userChallengeRepository;

    // Los retos de un pack caducan a los 7 dias si no se completan
    private static final long EXPIRY_SECONDS = 60L * 60 * 24 * 7;

    // Devuelve los packs activos disponibles en la tienda
    public List<PackStore> getAvailablePacks() {
        return packStoreRepository.findByActiveTrue();
    }

    // Procesa la compra de un pack: descuenta monedas y genera los retos
    public PackPurchaseResponse buyPack(String userId, Integer packId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("User not found"));

        PackStore pack = packStoreRepository.findById(packId)
                .orElseThrow(() -> new BadRequestException("Pack not found"));

        if (!pack.isActive()) {
            throw new BadRequestException("Pack is not available");
        }

        if (user.getCoinsApp() < pack.getPriceCoins()) {
            throw new BadRequestException("Not enough coins");
        }

        // Descontar monedas y registrar la compra en el historial del usuario
        user.setCoinsApp(user.getCoinsApp() - pack.getPriceCoins());

        PurchasedPack purchasedPack = new PurchasedPack();
        purchasedPack.setPackId(pack.getId());
        purchasedPack.setType(pack.getType());
        purchasedPack.setQuantity(pack.getQuantity());
        purchasedPack.setPurchaseDate(Instant.now());
        user.getPacksPurchased().add(purchasedPack);

        // Generar los retos del pack sin repetidos entre ellos
        int generated = generatePackItems(userId, pack);

        userRepository.save(user);

        return PackPurchaseResponse.builder()
                .message("Pack purchased successfully")
                .remainingCoins(user.getCoinsApp())
                .generatedItems(generated)
                .build();
    }

    // genera los retos del pack sin repetidos
    private int generatePackItems(String userId, PackStore pack) {

        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(EXPIRY_SECONDS);

        List<Challenge> available = challengeRepository.findByActiveTrue();

        if (available.isEmpty()) {
            throw new BadRequestException("No challenges available");
        }

        // Si el pack pide mas retos de los que hay, limitamos al maximo disponible
        int toGenerate = Math.min(pack.getQuantity(), available.size());

        Collections.shuffle(available);
        List<Challenge> selected = available.subList(0, toGenerate);

        List<UserChallenge> toSave = new ArrayList<>();

        for (Challenge challenge : selected) {
            UserChallenge userChallenge = new UserChallenge();
            userChallenge.setChallengeId(challenge.getId());
            userChallenge.setOwnerId(userId);
            userChallenge.setChallengeQuestion(challenge.getQuestion());
            userChallenge.setChallengeType("DARE");
            userChallenge.setState("CLOSED");
            userChallenge.setArrivalDate(now);
            userChallenge.setExpiresAt(expiresAt);
            userChallenge.setDatePassed(null);
            userChallenge.setFileUrl(null);
            userChallenge.setFinalScore(0);
            userChallenge.setTransferredTo(null);
            userChallenge.setTransferDate(null);
            userChallenge.setDaily(false);
            toSave.add(userChallenge);
        }

        // saveAll en batch
        userChallengeRepository.saveAll(toSave);

        return toSave.size();
    }
}

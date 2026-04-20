package com.godare.app.service;

import com.godare.app.dto.DailyItemResponse;
import com.godare.app.entity.*;
import com.godare.app.exception.BadRequestException;
import com.godare.app.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.List;
import java.util.Optional;

// Servicio que gestiona el reto diario (daily) del usuario.
// Cada dia el sistema asigna automaticamente un reto fitness aleatorio.
// Si el usuario transfiere el daily, no se le genera otro hasta el dia siguiente.
@Service
@RequiredArgsConstructor
public class DailyService {

    private final UserRepository userRepository;
    private final ChallengeRepository challengeRepository;
    private final UserChallengeRepository userChallengeRepository;
    private final MongoTemplate mongoTemplate;

    // Los retos diarios caducan a los 7 dias si no se completan
    private static final long EXPIRY_SECONDS = 60L * 60 * 24 * 7;

    // Devuelve el daily del usuario para hoy si ya existe, o genera uno nuevo.
    // Usamos lastDailyAssignedAt para evitar generar otro si el usuario ya lo transfirio.
    public DailyItemResponse getOrGenerateDaily(String userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("User not found"));

        // Calcular el rango horario del dia actual en UTC
        Instant startOfDay = LocalDate.now(ZoneOffset.UTC)
                .atStartOfDay()
                .toInstant(ZoneOffset.UTC);

        Instant endOfDay = LocalDate.now(ZoneOffset.UTC)
                .plusDays(1)
                .atStartOfDay()
                .toInstant(ZoneOffset.UTC);

        // Comprobar si ya se asigno un daily hoy
        boolean alreadyAssignedToday = user.getLastDailyAssignedAt() != null
                && user.getLastDailyAssignedAt().isAfter(startOfDay)
                && user.getLastDailyAssignedAt().isBefore(endOfDay);

        if (alreadyAssignedToday) {
            // Buscar si todavia tiene el reto diario en su poder
            Optional<UserChallenge> todayChallenge =
                    userChallengeRepository
                            .findFirstByOwnerIdAndIsDailyTrueAndArrivalDateBetweenOrderByArrivalDateDesc(
                                    userId, startOfDay, endOfDay
                            );

            if (todayChallenge.isPresent()) {
                UserChallenge c = todayChallenge.get();
                return DailyItemResponse.builder()
                        .id(c.getId())
                        .type("DARE")
                        .title(c.getChallengeQuestion())
                        .state(c.getState())
                        .arrivalDate(c.getArrivalDate())
                        .expiresAt(c.getExpiresAt())
                        .generatedToday(false)
                        .build();
            }

            // Ya se asigno hoy pero lo transfirio — no generamos otro
            return DailyItemResponse.builder()
                    .id(null)
                    .type(null)
                    .title("Ya transferiste tu daily de hoy")
                    .state("TRANSFERRED")
                    .arrivalDate(null)
                    .expiresAt(null)
                    .generatedToday(false)
                    .build();
        }

        // No tiene daily hoy — generar uno nuevo
        return generateAndSaveDaily(user);
    }

    // Genera un reto daily nuevo para el usuario y lo guarda en la BD.
    // Marca lastDailyAssignedAt para evitar duplicados si el usuario lo transfiere.
    private DailyItemResponse generateAndSaveDaily(User user) {

        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(EXPIRY_SECONDS);

        Challenge selected = sampleActiveChallenge()
                .orElseThrow(() -> new BadRequestException("No challenges available"));

        // Actualizar la fecha de ultimo daily antes de guardar el reto
        user.setLastDailyAssignedAt(now);
        userRepository.save(user);

        UserChallenge userChallenge = new UserChallenge();
        userChallenge.setChallengeId(selected.getId());
        userChallenge.setOwnerId(user.getId());
        userChallenge.setChallengeQuestion(selected.getQuestion());
        userChallenge.setChallengeType("DARE");
        userChallenge.setState("CLOSED");
        userChallenge.setArrivalDate(now);
        userChallenge.setExpiresAt(expiresAt);
        userChallenge.setDatePassed(null);
        userChallenge.setFileUrl(null);
        userChallenge.setFinalScore(0);
        userChallenge.setTransferredTo(null);
        userChallenge.setTransferDate(null);
        userChallenge.setDaily(true);

        UserChallenge saved = userChallengeRepository.save(userChallenge);

        return DailyItemResponse.builder()
                .id(saved.getId())
                .type("DARE")
                .title(saved.getChallengeQuestion())
                .state(saved.getState())
                .arrivalDate(saved.getArrivalDate())
                .expiresAt(saved.getExpiresAt())
                .generatedToday(true)
                .build();
    }

    // Obtiene un reto activo al azar usando la agregacion $sample de MongoDB.
    // Asi evitamos cargar toda la coleccion en memoria para elegir uno aleatorio.
    private Optional<Challenge> sampleActiveChallenge() {
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("active").is(true)),
                Aggregation.sample(1)
        );
        List<Challenge> results = mongoTemplate
                .aggregate(aggregation, "challenges", Challenge.class)
                .getMappedResults();
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }
}

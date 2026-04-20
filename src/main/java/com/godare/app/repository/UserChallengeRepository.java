package com.godare.app.repository;

import com.godare.app.entity.UserChallenge;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface UserChallengeRepository extends MongoRepository<UserChallenge, String> {

    // Busca el daily de hoy del usuario filtrando por isDaily=true
    Optional<UserChallenge> findFirstByOwnerIdAndIsDailyTrueAndArrivalDateBetweenOrderByArrivalDateDesc(
            String ownerId,
            Instant start,
            Instant end
    );

    // Lista los retos del usuario filtrados por estado, ordenados por fecha de llegada descendente
    List<UserChallenge> findByOwnerIdAndStateOrderByArrivalDateDesc(String ownerId, String state);

    // Lista todos los retos del usuario sin filtrar por estado
    List<UserChallenge> findByOwnerIdOrderByArrivalDateDesc(String ownerId);

    long countByOwnerIdAndState(String ownerId, String state);

    List<UserChallenge> findByOwnerId(String userId);

    void deleteByOwnerId(String userId);
}
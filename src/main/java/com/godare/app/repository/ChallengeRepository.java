package com.godare.app.repository;

import com.godare.app.entity.Challenge;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface ChallengeRepository extends MongoRepository<Challenge, String> {

    // Devuelve solo los challenges activos para asignación desde packs
    List<Challenge> findByActiveTrue();

    // Usado para comprobar si hay challenges activos disponibles
    boolean existsByActiveTrue();
}
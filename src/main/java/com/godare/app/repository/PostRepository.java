package com.godare.app.repository;

import com.godare.app.entity.Post;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface PostRepository extends MongoRepository<Post, String> {

    List<Post> findByUserIdOrderByCreatedAtDesc(String userId);

    long countByUserId(String userId);

    List<Post> findByUserId(String userId);

    void deleteByUserId(String userId);

    // Posts activos — expiresAt en el futuro
    long countByUserIdAndExpiresAtAfter(String userId, Instant now);

    // Posts sin fecha de caducidad (legacy o verdades sin media)
    long countByUserIdAndExpiresAtIsNull(String userId);

    // Busca el post vinculado a un UserChallenge por su referenceId
    Optional<Post> findByReferenceId(String referenceId);
}
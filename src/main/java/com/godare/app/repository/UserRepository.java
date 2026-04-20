package com.godare.app.repository;

import com.godare.app.entity.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends MongoRepository<User, String> {

    Optional<User> findByEmail(String email);

    Optional<User> findByNick(String nick);

    boolean existsByEmail(String email);

    boolean existsByNick(String nick);

    Optional<User> findByTemporaryHash(String temporaryHash);

    Page<User> findAllByOrderByPointsDesc(Pageable pageable);

    Page<User> findByIdInOrderByPointsDesc(List<String> ids, Pageable pageable);

    // Busca usuarios cuyo nick contenga el texto dado (case insensitive), excluyendo al propio usuario
    Page<User> findByNickContainingIgnoreCaseAndIdNot(String nick, String excludeId, Pageable pageable);
}
package com.godare.app.repository;

import com.godare.app.entity.PackStore;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface PackStoreRepository extends MongoRepository<PackStore, Integer> {

    List<PackStore> findByActiveTrue();
}
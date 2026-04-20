package com.godare.app.repository;

import com.godare.app.entity.PointsHistory;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PointsHistoryRepository extends MongoRepository<PointsHistory, String> {

    void deleteByUserId(String userId);
}
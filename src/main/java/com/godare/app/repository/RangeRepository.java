package com.godare.app.repository;

import com.godare.app.entity.Range;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface RangeRepository extends MongoRepository<Range, Integer> {

    Optional<Range> findByLevel(Integer level);

    Optional<Range> findByMinPointsLessThanEqualAndMaxPointsGreaterThanEqual(Integer points1, Integer points2);

    List<Range> findAllByOrderByLevelAsc();
}
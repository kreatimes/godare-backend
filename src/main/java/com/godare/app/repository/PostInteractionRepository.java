package com.godare.app.repository;

import com.godare.app.entity.PostInteraction;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface PostInteractionRepository extends MongoRepository<PostInteraction, String> {

    Optional<PostInteraction> findByPostIdAndUserIdAndTypeIn(String postId, String userId, List<String> types);

    List<PostInteraction> findByPostIdAndTypeOrderByCreatedAtAsc(String postId, String type);

    void deleteByPostIdIn(List<String> postIds);

    void deleteByUserId(String userId);
}
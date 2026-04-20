package com.godare.app.service;

import com.godare.app.entity.Post;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PostExpiryService {

    private static final Logger log = LoggerFactory.getLogger(PostExpiryService.class);

    private final MongoTemplate mongoTemplate;
    private final CloudinaryService cloudinaryService;

    /**
     * Cada hora busca posts que caducan en la próxima hora
     * y borra sus archivos de Cloudinary antes de que MongoDB los elimine.
     * El TTL index de MongoDB se encarga de borrar el documento después.
     */
    @Scheduled(fixedDelay = 3600000) // cada hora
    public void cleanExpiredPostFiles() {

        Instant now = Instant.now();
        Instant inOneHour = now.plusSeconds(3600);

        // Posts que caducan en la próxima hora y tienen mediaPublicId
        Query query = new Query(
                Criteria.where("expiresAt").gt(now).lte(inOneHour)
                        .and("mediaPublicId").ne(null)
        );

        List<Post> expiringPosts = mongoTemplate.find(query, Post.class);

        if (expiringPosts.isEmpty()) return;

        log.info("PostExpiryService: {} posts próximos a caducar", expiringPosts.size());

        for (Post post : expiringPosts) {
            try {
                String resourceType = cloudinaryService.inferResourceType(post.getMediaUrl());
                cloudinaryService.deleteFile(post.getMediaPublicId(), resourceType);
                log.info("Archivo borrado de Cloudinary: {}", post.getMediaPublicId());
            } catch (Exception e) {
                log.error("Error borrando archivo de post {}: {}",
                        post.getId(), e.getMessage());
            }
        }
    }
}
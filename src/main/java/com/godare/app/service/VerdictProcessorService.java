package com.godare.app.service;

import com.godare.app.entity.Post;
import com.godare.app.entity.UserChallenge;
import com.godare.app.repository.PostRepository;
import com.godare.app.repository.UserChallengeRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

// Scheduler que cierra la votacion de posts tras 5 dias
@Service
@RequiredArgsConstructor
public class VerdictProcessorService {

    private static final Logger log = LoggerFactory.getLogger(VerdictProcessorService.class);

    private static final long VERDICT_WINDOW_DAYS = 5;

    private final PostRepository postRepository;
    private final UserChallengeRepository userChallengeRepository;

    @Scheduled(fixedDelay = 3_600_000) // cada hora
    public void processVerdicts() {

        Instant cutoff = Instant.now().minus(VERDICT_WINDOW_DAYS, ChronoUnit.DAYS);

        List<Post> pendingPosts = postRepository
                .findByPostTypeAndCreatedAtBeforeAndVerdictProcessedFalse("DARE", cutoff);

        if (pendingPosts.isEmpty()) return;

        log.info("VerdictProcessorService: {} posts pendientes de veredicto", pendingPosts.size());

        for (Post post : pendingPosts) {
            try {
                evaluatePost(post);
            } catch (Exception e) {
                log.error("Error evaluando veredicto del post {}: {}", post.getId(), e.getMessage());
            }
        }
    }

    private void evaluatePost(Post post) {

        boolean failedByVote = post.getNotApprovedCount() > post.getApprovedCount();

        if (post.getReferenceId() != null) {
            userChallengeRepository.findById(post.getReferenceId()).ifPresent(challenge -> {

                // Copiar los contadores al reto para que persistan más allá de la vida del post
                challenge.setApprovedCount(post.getApprovedCount());
                challenge.setNotApprovedCount(post.getNotApprovedCount());

                // Cambiar el estado solo si sigue como PASSED y la comunidad lo rechazó
                if (failedByVote && "PASSED".equals(challenge.getState())) {
                    challenge.setState("FAILED_BY_VOTE");
                    log.info(
                            "Reto {} marcado como FAILED_BY_VOTE (✓ {}  ✗ {})",
                            challenge.getId(),
                            post.getApprovedCount(),
                            post.getNotApprovedCount()
                    );
                } else {
                    log.info(
                            "Reto {} confirmado como PASSED (✓ {}  ✗ {})",
                            challenge.getId(),
                            post.getApprovedCount(),
                            post.getNotApprovedCount()
                    );
                }

                userChallengeRepository.save(challenge);
            });
        }

        post.setVerdictProcessed(true);
        postRepository.save(post);
    }
}
package com.godare.app.service;

import com.godare.app.dto.CursorFeedResponse;
import com.godare.app.dto.PostResponse;
import com.godare.app.entity.*;
import com.godare.app.exception.BadRequestException;
import com.godare.app.repository.UserChallengeRepository;
import com.godare.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

// Servicio que construye el feed de posts.
// Usamos paginacion por cursor en lugar de pagina numerada para que el feed
// no salte si se publican posts nuevos mientras el usuario esta navegando.
@Service
@RequiredArgsConstructor
public class FeedService {

    private final MongoTemplate mongoTemplate;
    private final UserRepository userRepository;
    private final UserChallengeRepository userChallengeRepository;
    private final PostInteractionService postInteractionService;

    // Feed global — muestra todos los posts de todos los usuarios
    public CursorFeedResponse getGlobalFeed(String userId, int limit,
                                            Instant cursorCreatedAt, String cursorId) {

        Query query = buildCursorQuery(null, cursorCreatedAt, cursorId, limit);
        return buildFeedResponse(userId, query, limit);
    }

    // Feed de seguidos — muestra solo posts de usuarios a los que sigue + los propios
    public CursorFeedResponse getFollowingFeed(String userId, int limit,
                                               Instant cursorCreatedAt, String cursorId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("User not found"));

        List<String> visibleUserIds = new ArrayList<>(user.getFollowing());
        visibleUserIds.add(userId);

        Query query = buildCursorQuery(
                Criteria.where("userId").in(visibleUserIds),
                cursorCreatedAt, cursorId, limit
        );

        return buildFeedResponse(userId, query, limit);
    }

    // Construye la query de paginacion por cursor.
    // El cursor usa createdAt + _id para ser estable incluso con posts del mismo instante.
    private Query buildCursorQuery(Criteria extraCriteria,
                                   Instant cursorCreatedAt, String cursorId, int limit) {
        Query query = new Query();

        if (extraCriteria != null) {
            query.addCriteria(extraCriteria);
        }

        if (cursorCreatedAt != null && cursorId != null && !cursorId.isBlank()) {
            Criteria olderByDate = Criteria.where("createdAt").lt(cursorCreatedAt);
            Criteria sameDateOlderId = new Criteria().andOperator(
                    Criteria.where("createdAt").is(cursorCreatedAt),
                    Criteria.where("_id").lt(cursorId)
            );
            query.addCriteria(new Criteria().orOperator(olderByDate, sameDateOlderId));
        }

        query.with(Sort.by(
                Sort.Order.desc("createdAt"),
                Sort.Order.desc("_id")
        ));

        // Pedimos un elemento extra para saber si hay mas paginas sin hacer otra consulta
        query.limit(limit + 1);

        return query;
    }

    // Ejecuta la query, monta la respuesta con cursor y mapea los posts a DTO
    private CursorFeedResponse buildFeedResponse(String userId, Query query, int limit) {

        List<Post> posts = mongoTemplate.find(query, Post.class);
        boolean hasMore = posts.size() > limit;
        if (hasMore) posts = posts.subList(0, limit);

        List<PostResponse> content = posts.stream()
                .map(post -> mapToPostResponse(post, userId))
                .toList();

        Instant nextCursorCreatedAt = null;
        String nextCursorId = null;

        if (!posts.isEmpty() && hasMore) {
            Post lastPost = posts.get(posts.size() - 1);
            nextCursorCreatedAt = lastPost.getCreatedAt();
            nextCursorId = lastPost.getId();
        }

        return CursorFeedResponse.builder()
                .content(content)
                .nextCursorCreatedAt(nextCursorCreatedAt)
                .nextCursorId(nextCursorId)
                .hasMore(hasMore)
                .build();
    }

    // Convierte una entidad Post al DTO que se envia al cliente.
    // Incluye el texto del reto y la reaccion del usuario que esta viendo el feed.
    private PostResponse mapToPostResponse(Post post, String viewerUserId) {

        User postUser = userRepository.findById(post.getUserId()).orElse(null);

        // Recuperar el texto del reto para mostrarlo junto al video
        String challengeText = null;
        if ("DARE".equals(post.getPostType()) && post.getReferenceId() != null) {
            challengeText = userChallengeRepository.findById(post.getReferenceId())
                    .map(UserChallenge::getChallengeQuestion)
                    .orElse(null);
        }

        return PostResponse.builder()
                .id(post.getId())
                .userId(post.getUserId())
                .userNick(postUser != null ? postUser.getNick() : "unknown")
                .userImageUrl(postUser != null ? postUser.getUserImageUrl() : null)
                .postType(post.getPostType())
                .textResponse(challengeText != null ? challengeText : post.getTextResponse())
                .mediaUrl(post.getMediaUrl())
                .likesCount(post.getLikesCount())
                .dislikesCount(post.getDislikesCount())
                .commentsCount(post.getCommentsCount())
                .currentReaction(postInteractionService.getCurrentReaction(viewerUserId, post.getId()))
                .createdAt(post.getCreatedAt())
                .build();
    }
}

package com.godare.app.service;

import com.godare.app.dto.CursorFeedResponse;
import com.godare.app.dto.PostResponse;
import com.godare.app.entity.Post;
import com.godare.app.entity.User;
import com.godare.app.exception.BadRequestException;
import com.godare.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserPostService {

    private final MongoTemplate mongoTemplate;
    private final UserRepository userRepository;
    private final PostInteractionService postInteractionService;

    public CursorFeedResponse getUserPosts(String viewerUserId, String targetUserId, int limit, Instant cursorCreatedAt, String cursorId) {

        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new BadRequestException("User not found"));

        Query query = new Query();
        query.addCriteria(Criteria.where("userId").is(targetUser.getId()));

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

        query.limit(limit + 1);

        List<Post> posts = mongoTemplate.find(query, Post.class);

        boolean hasMore = posts.size() > limit;

        if (hasMore) {
            posts = posts.subList(0, limit);
        }

        List<PostResponse> content = posts.stream()
                .map(post -> mapToPostResponse(post, viewerUserId, targetUser))
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

    private PostResponse mapToPostResponse(Post post, String viewerUserId, User targetUser) {
        return PostResponse.builder()
                .id(post.getId())
                .userId(post.getUserId())
                .userNick(targetUser.getNick())
                .userImageUrl(targetUser.getUserImageUrl())
                .postType(post.getPostType())
                .textResponse(post.getTextResponse())
                .mediaUrl(post.getMediaUrl())
                .likesCount(post.getLikesCount())
                .dislikesCount(post.getDislikesCount())
                .commentsCount(post.getCommentsCount())
                .currentReaction(postInteractionService.getCurrentReaction(viewerUserId, post.getId()))
                .createdAt(post.getCreatedAt())
                .build();
    }
}
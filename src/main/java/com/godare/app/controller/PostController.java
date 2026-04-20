package com.godare.app.controller;

import com.godare.app.dto.*;
import com.godare.app.service.FeedService;
import com.godare.app.service.PostInteractionService;
import com.godare.app.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final FeedService feedService;
    private final PostInteractionService postInteractionService;
    private final PostService postService;

    @GetMapping("/feed/global")
    public CursorFeedResponse getGlobalFeed(
            Authentication authentication,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant cursorCreatedAt,
            @RequestParam(required = false) String cursorId
    ) {
        String userId = authentication.getName();
        return feedService.getGlobalFeed(userId, limit, cursorCreatedAt, cursorId);
    }

    @GetMapping("/feed/following")
    public CursorFeedResponse getFollowingFeed(
            Authentication authentication,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) String cursorCreatedAt,
            @RequestParam(required = false) String cursorId
    ) {
        String userId = authentication.getName();

        Instant cursorInstant = null;
        if (cursorCreatedAt != null && !cursorCreatedAt.isBlank()) {
            cursorInstant = Instant.parse(cursorCreatedAt);
        }

        return feedService.getFollowingFeed(userId, limit, cursorInstant, cursorId);
    }

    @GetMapping("/{id}")
    public PostDetailResponse getPostDetail(@PathVariable String id, Authentication authentication) {
        String userId = authentication.getName();
        return postService.getPostDetail(id, userId);
    }

    @PostMapping("/{id}/like")
    public PostReactionResponse likePost(@PathVariable String id, Authentication authentication) {
        String userId = authentication.getName();
        return postInteractionService.likePost(userId, id);
    }

    @PostMapping("/{id}/dislike")
    public PostReactionResponse dislikePost(@PathVariable String id, Authentication authentication) {
        String userId = authentication.getName();
        return postInteractionService.dislikePost(userId, id);
    }

    @PostMapping("/{id}/comments")
    public CommentResponse addComment(@PathVariable String id,
                                      @Valid @RequestBody CreateCommentRequest request,
                                      Authentication authentication) {
        String userId = authentication.getName();
        return postInteractionService.addComment(userId, id, request);
    }

    @GetMapping("/{id}/comments")
    public List<CommentResponse> getComments(@PathVariable String id, Authentication authentication) {
        String userId = authentication.getName();
        return postInteractionService.getComments(id, userId);
    }

    @PostMapping("/comments/{commentId}/like")
    public PostReactionResponse likeComment(@PathVariable String commentId, Authentication authentication) {
        String userId = authentication.getName();
        return postInteractionService.likeComment(userId, commentId);
    }

    @PostMapping("/comments/{commentId}/dislike")
    public PostReactionResponse dislikeComment(@PathVariable String commentId, Authentication authentication) {
        String userId = authentication.getName();
        return postInteractionService.dislikeComment(userId, commentId);
    }

    @PutMapping("/comments/{commentId}")
    public CommentResponse updateComment(@PathVariable String commentId,
                                         @Valid @RequestBody UpdateCommentRequest request,
                                         Authentication authentication) {
        String userId = authentication.getName();
        return postInteractionService.updateComment(userId, commentId, request);
    }

    @DeleteMapping("/comments/{commentId}")
    public String deleteComment(@PathVariable String commentId,
                                Authentication authentication) {
        String userId = authentication.getName();
        return postInteractionService.deleteComment(userId, commentId);
    }
}
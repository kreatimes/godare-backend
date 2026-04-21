package com.godare.app.service;

import com.godare.app.dto.CommentResponse;
import com.godare.app.dto.CreateCommentRequest;
import com.godare.app.dto.PostReactionResponse;
import com.godare.app.dto.UpdateCommentRequest;
import com.godare.app.entity.Post;
import com.godare.app.entity.PostInteraction;
import com.godare.app.entity.User;
import com.godare.app.exception.BadRequestException;
import com.godare.app.repository.PostInteractionRepository;
import com.godare.app.repository.PostRepository;
import com.godare.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostInteractionService {

    private final PostRepository postRepository;
    private final PostInteractionRepository postInteractionRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public PostReactionResponse likePost(String userId, String postId) {
        return reactToPost(userId, postId, "LIKE");
    }

    public PostReactionResponse dislikePost(String userId, String postId) {
        return reactToPost(userId, postId, "DISLIKE");
    }

    public PostReactionResponse approvePost(String userId, String postId) {
        return verdictToPost(userId, postId, "APPROVED");
    }

    public PostReactionResponse notApprovePost(String userId, String postId) {
        return verdictToPost(userId, postId, "NOT_APPROVED");
    }

    public PostReactionResponse likeComment(String userId, String commentId) {
        return reactToComment(userId, commentId, "LIKE");
    }

    public PostReactionResponse dislikeComment(String userId, String commentId) {
        return reactToComment(userId, commentId, "DISLIKE");
    }

    // Gestiona el voto de veredicto (¿superado o no?) sobre un post.
    // Lógica igual que like/dislike: si ya votaste lo mismo se deshace, si votaste
    // lo contrario se cambia, si no habías votado se añade.
    private PostReactionResponse verdictToPost(String userId, String postId, String newVerdict) {

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BadRequestException("Post not found"));

        PostInteraction existing = postInteractionRepository
                .findByPostIdAndUserIdAndTypeIn(postId, userId, List.of("APPROVED", "NOT_APPROVED"))
                .orElse(null);

        if (existing == null) {
            PostInteraction interaction = new PostInteraction();
            interaction.setPostId(postId);
            interaction.setUserId(userId);
            interaction.setType(newVerdict);
            interaction.setCreatedAt(Instant.now());
            postInteractionRepository.save(interaction);

            if ("APPROVED".equals(newVerdict)) {
                post.setApprovedCount(post.getApprovedCount() + 1);
            } else {
                post.setNotApprovedCount(post.getNotApprovedCount() + 1);
            }
            postRepository.save(post);

            return buildVerdictResponse(post, newVerdict + " added", newVerdict);
        }

        // Ya tenía el mismo voto → se deshace (toggle off)
        if (existing.getType().equals(newVerdict)) {
            if ("APPROVED".equals(newVerdict)) {
                post.setApprovedCount(Math.max(0, post.getApprovedCount() - 1));
            } else {
                post.setNotApprovedCount(Math.max(0, post.getNotApprovedCount() - 1));
            }
            postInteractionRepository.delete(existing);
            postRepository.save(post);

            return buildVerdictResponse(post, newVerdict + " removed", "NONE");
        }

        // Tenía el voto contrario → se cambia
        if ("APPROVED".equals(existing.getType())) {
            post.setApprovedCount(Math.max(0, post.getApprovedCount() - 1));
            post.setNotApprovedCount(post.getNotApprovedCount() + 1);
        } else {
            post.setNotApprovedCount(Math.max(0, post.getNotApprovedCount() - 1));
            post.setApprovedCount(post.getApprovedCount() + 1);
        }

        existing.setType(newVerdict);
        existing.setCreatedAt(Instant.now());
        postInteractionRepository.save(existing);
        postRepository.save(post);

        return buildVerdictResponse(post, "Verdict updated", newVerdict);
    }

    private PostReactionResponse reactToComment(String userId, String commentId, String newReaction) {

        PostInteraction comment = postInteractionRepository.findById(commentId)
                .orElseThrow(() -> new BadRequestException("Comment not found"));

        if (!"COMMENT".equals(comment.getType())) {
            throw new BadRequestException("Interaction is not a comment");
        }

        PostInteraction existing = postInteractionRepository
                .findByPostIdAndUserIdAndTypeIn(commentId, userId, List.of("COMMENT_LIKE", "COMMENT_DISLIKE"))
                .orElse(null);

        if (existing == null) {
            PostInteraction reaction = PostInteraction.builder()
                    .postId(commentId)
                    .userId(userId)
                    .type("COMMENT_" + newReaction)
                    .createdAt(Instant.now())
                    .build();
            postInteractionRepository.save(reaction);

            if ("LIKE".equals(newReaction)) {
                comment.setLikesCount(comment.getLikesCount() + 1);
            } else {
                comment.setDislikesCount(comment.getDislikesCount() + 1);
            }
            postInteractionRepository.save(comment);

            return buildCommentReactionResponse(comment, newReaction + " added", newReaction);
        }

        String existingReaction = existing.getType().replace("COMMENT_", "");

        if (existingReaction.equals(newReaction)) {
            if ("LIKE".equals(newReaction)) {
                comment.setLikesCount(Math.max(0, comment.getLikesCount() - 1));
            } else {
                comment.setDislikesCount(Math.max(0, comment.getDislikesCount() - 1));
            }
            postInteractionRepository.delete(existing);
            postInteractionRepository.save(comment);

            return buildCommentReactionResponse(comment, newReaction + " removed", "NONE");
        }

        if ("LIKE".equals(existingReaction) && "DISLIKE".equals(newReaction)) {
            comment.setLikesCount(Math.max(0, comment.getLikesCount() - 1));
            comment.setDislikesCount(comment.getDislikesCount() + 1);
        } else {
            comment.setDislikesCount(Math.max(0, comment.getDislikesCount() - 1));
            comment.setLikesCount(comment.getLikesCount() + 1);
        }

        existing.setType("COMMENT_" + newReaction);
        existing.setCreatedAt(Instant.now());
        postInteractionRepository.save(existing);
        postInteractionRepository.save(comment);

        return buildCommentReactionResponse(comment, "Reaction updated", newReaction);
    }

    private PostReactionResponse reactToPost(String userId, String postId, String newReaction) {

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BadRequestException("Post not found"));

        PostInteraction existingReaction = postInteractionRepository
                .findByPostIdAndUserIdAndTypeIn(postId, userId, List.of("LIKE", "DISLIKE"))
                .orElse(null);

        if (existingReaction == null) {
            PostInteraction interaction = new PostInteraction();
            interaction.setPostId(postId);
            interaction.setUserId(userId);
            interaction.setType(newReaction);
            interaction.setCommentText(null);
            interaction.setCreatedAt(Instant.now());

            postInteractionRepository.save(interaction);

            if ("LIKE".equals(newReaction)) {
                post.setLikesCount(post.getLikesCount() + 1);
            } else {
                post.setDislikesCount(post.getDislikesCount() + 1);
            }

            postRepository.save(post);

            if ("LIKE".equals(newReaction) && !post.getUserId().equals(userId)) {
                createLikeNotification(userId, post);
            }

            return buildReactionResponse(post, newReaction + " added", newReaction);
        }

        if (existingReaction.getType().equals(newReaction)) {
            if ("LIKE".equals(newReaction)) {
                post.setLikesCount(Math.max(0, post.getLikesCount() - 1));
            } else {
                post.setDislikesCount(Math.max(0, post.getDislikesCount() - 1));
            }

            postInteractionRepository.delete(existingReaction);
            postRepository.save(post);

            return buildReactionResponse(post, newReaction + " removed", "NONE");
        }

        if ("LIKE".equals(existingReaction.getType()) && "DISLIKE".equals(newReaction)) {
            post.setLikesCount(Math.max(0, post.getLikesCount() - 1));
            post.setDislikesCount(post.getDislikesCount() + 1);
        } else {
            post.setDislikesCount(Math.max(0, post.getDislikesCount() - 1));
            post.setLikesCount(post.getLikesCount() + 1);
        }

        existingReaction.setType(newReaction);
        existingReaction.setCreatedAt(Instant.now());

        postInteractionRepository.save(existingReaction);
        postRepository.save(post);

        if ("LIKE".equals(newReaction) && !post.getUserId().equals(userId)) {
            createLikeNotification(userId, post);
        }

        return buildReactionResponse(post, "Reaction updated", newReaction);
    }

    public CommentResponse addComment(String userId, String postId, CreateCommentRequest request) {

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BadRequestException("Post not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("User not found"));

        PostInteraction comment = new PostInteraction();
        comment.setPostId(postId);
        comment.setUserId(userId);
        comment.setType("COMMENT");
        comment.setCommentText(request.getCommentText());
        comment.setCreatedAt(Instant.now());

        PostInteraction saved = postInteractionRepository.save(comment);

        post.setCommentsCount(post.getCommentsCount() + 1);
        postRepository.save(post);

        if (!post.getUserId().equals(userId)) {
            notificationService.createNotification(
                    post.getUserId(),
                    "COMMENT_RECEIVED",
                    "¡Nuevo comentario!",
                    user.getNick() + " ha comentado tu publicación",
                    Map.of(
                            "postId", post.getId(),
                            "fromUserId", user.getId(),
                            "fromUserNick", user.getNick()
                    )
            );
        }

        return mapToCommentResponse(saved, user);
    }

    public List<CommentResponse> getComments(String postId, String currentUserId) {

        postRepository.findById(postId)
                .orElseThrow(() -> new BadRequestException("Post not found"));

        List<PostInteraction> comments =
                postInteractionRepository.findByPostIdAndTypeOrderByCreatedAtAsc(postId, "COMMENT");

        if (comments.isEmpty()) return List.of();

        Set<String> userIds = comments.stream()
                .map(PostInteraction::getUserId)
                .collect(Collectors.toSet());

        Map<String, User> usersMap = userRepository.findAllById(userIds)
                .stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));

        List<String> commentIds = comments.stream().map(PostInteraction::getId).toList();
        Map<String, String> userReactionsMap = postInteractionRepository
                .findByPostIdInAndUserIdAndTypeIn(commentIds, currentUserId, List.of("COMMENT_LIKE", "COMMENT_DISLIKE"))
                .stream()
                .collect(Collectors.toMap(
                        PostInteraction::getPostId,
                        r -> r.getType().replace("COMMENT_", "")
                ));

        return comments.stream()
                .map(comment -> {
                    User user = usersMap.get(comment.getUserId());
                    String reaction = userReactionsMap.getOrDefault(comment.getId(), "NONE");
                    return CommentResponse.builder()
                            .id(comment.getId())
                            .postId(comment.getPostId())
                            .userId(comment.getUserId())
                            .userNick(user != null ? user.getNick() : "unknown")
                            .userImageUrl(user != null ? user.getUserImageUrl() : null)
                            .commentText(comment.getCommentText())
                            .createdAt(comment.getCreatedAt())
                            .likesCount(comment.getLikesCount())
                            .dislikesCount(comment.getDislikesCount())
                            .currentReaction(reaction)
                            .build();
                })
                .toList();
    }

    public List<CommentResponse> getUserComments(String userId) {

        List<PostInteraction> comments =
                postInteractionRepository.findByUserIdAndTypeOrderByCreatedAtDesc(userId, "COMMENT");

        if (comments.isEmpty()) return List.of();

        User user = userRepository.findById(userId).orElse(null);

        return comments.stream()
                .map(comment -> CommentResponse.builder()
                        .id(comment.getId())
                        .postId(comment.getPostId())
                        .userId(comment.getUserId())
                        .userNick(user != null ? user.getNick() : "unknown")
                        .userImageUrl(user != null ? user.getUserImageUrl() : null)
                        .commentText(comment.getCommentText())
                        .createdAt(comment.getCreatedAt())
                        .likesCount(comment.getLikesCount())
                        .dislikesCount(comment.getDislikesCount())
                        .currentReaction("NONE")
                        .build())
                .toList();
    }

    public String getCurrentReaction(String userId, String postId) {
        return postInteractionRepository
                .findByPostIdAndUserIdAndTypeIn(postId, userId, List.of("LIKE", "DISLIKE"))
                .map(PostInteraction::getType)
                .orElse("NONE");
    }

    // Devuelve el veredicto actual del usuario sobre un post (APPROVED, NOT_APPROVED o NONE)
    public String getCurrentVerdict(String userId, String postId) {
        return postInteractionRepository
                .findByPostIdAndUserIdAndTypeIn(postId, userId, List.of("APPROVED", "NOT_APPROVED"))
                .map(PostInteraction::getType)
                .orElse("NONE");
    }

    public CommentResponse updateComment(String userId, String commentId, UpdateCommentRequest request) {

        PostInteraction comment = postInteractionRepository.findById(commentId)
                .orElseThrow(() -> new BadRequestException("Comment not found"));

        if (!"COMMENT".equals(comment.getType())) {
            throw new BadRequestException("Interaction is not a comment");
        }

        if (!comment.getUserId().equals(userId)) {
            throw new BadRequestException("You can only edit your own comments");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("User not found"));

        comment.setCommentText(request.getCommentText());
        PostInteraction saved = postInteractionRepository.save(comment);

        return mapToCommentResponse(saved, user);
    }

    public String deleteComment(String userId, String commentId) {

        PostInteraction comment = postInteractionRepository.findById(commentId)
                .orElseThrow(() -> new BadRequestException("Comment not found"));

        if (!"COMMENT".equals(comment.getType())) {
            throw new BadRequestException("Interaction is not a comment");
        }

        if (!comment.getUserId().equals(userId)) {
            throw new BadRequestException("You can only delete your own comments");
        }

        Post post = postRepository.findById(comment.getPostId())
                .orElseThrow(() -> new BadRequestException("Post not found"));

        postInteractionRepository.delete(comment);

        post.setCommentsCount(Math.max(0, post.getCommentsCount() - 1));
        postRepository.save(post);

        return "Comment deleted successfully";
    }

    private PostReactionResponse buildReactionResponse(Post post, String message, String currentReaction) {
        return PostReactionResponse.builder()
                .message(message)
                .likesCount(post.getLikesCount())
                .dislikesCount(post.getDislikesCount())
                .currentReaction(currentReaction)
                .approvedCount(post.getApprovedCount())
                .notApprovedCount(post.getNotApprovedCount())
                .currentVerdict("NONE")
                .build();
    }

    private PostReactionResponse buildVerdictResponse(Post post, String message, String currentVerdict) {
        return PostReactionResponse.builder()
                .message(message)
                .likesCount(post.getLikesCount())
                .dislikesCount(post.getDislikesCount())
                .currentReaction("NONE")
                .approvedCount(post.getApprovedCount())
                .notApprovedCount(post.getNotApprovedCount())
                .currentVerdict(currentVerdict)
                .build();
    }

    private PostReactionResponse buildCommentReactionResponse(PostInteraction comment, String message, String currentReaction) {
        return PostReactionResponse.builder()
                .message(message)
                .likesCount(comment.getLikesCount())
                .dislikesCount(comment.getDislikesCount())
                .currentReaction(currentReaction)
                .approvedCount(0)
                .notApprovedCount(0)
                .currentVerdict("NONE")
                .build();
    }

    private CommentResponse mapToCommentResponse(PostInteraction comment, User user) {
        return CommentResponse.builder()
                .id(comment.getId())
                .postId(comment.getPostId())
                .userId(comment.getUserId())
                .userNick(user.getNick())
                .userImageUrl(user.getUserImageUrl())
                .commentText(comment.getCommentText())
                .createdAt(comment.getCreatedAt())
                .likesCount(comment.getLikesCount())
                .dislikesCount(comment.getDislikesCount())
                .currentReaction("NONE")
                .build();
    }

    private void createLikeNotification(String fromUserId, Post post) {
        User fromUser = userRepository.findById(fromUserId)
                .orElseThrow(() -> new BadRequestException("User not found"));

        notificationService.createNotification(
                post.getUserId(),
                "LIKE_RECEIVED",
                "¡Nuevo me gusta!",
                fromUser.getNick() + " le ha dado like a tu publicación",
                Map.of(
                        "postId", post.getId(),
                        "fromUserId", fromUser.getId(),
                        "fromUserNick", fromUser.getNick()
                )
        );
    }
}
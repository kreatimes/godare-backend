package com.godare.app.service;

import com.godare.app.dto.PostDetailResponse;
import com.godare.app.dto.PostResponse;
import com.godare.app.entity.Post;
import com.godare.app.entity.User;
import com.godare.app.exception.BadRequestException;
import com.godare.app.repository.PostRepository;
import com.godare.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final PostInteractionService postInteractionService;

    public PostDetailResponse getPostDetail(String postId, String viewerUserId) {

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BadRequestException("Post not found"));

        User postUser = userRepository.findById(post.getUserId())
                .orElseThrow(() -> new BadRequestException("Post owner not found"));

        return PostDetailResponse.builder()
                .id(post.getId())
                .userId(post.getUserId())
                .userNick(postUser.getNick())
                .userImageUrl(postUser.getUserImageUrl())
                .postType(post.getPostType())
                .textResponse(post.getTextResponse())
                .mediaUrl(post.getMediaUrl())
                .likesCount(post.getLikesCount())
                .dislikesCount(post.getDislikesCount())
                .commentsCount(post.getCommentsCount())
                .currentReaction(postInteractionService.getCurrentReaction(viewerUserId, postId))
                .createdAt(post.getCreatedAt())
                .comments(postInteractionService.getComments(postId, viewerUserId))
                .build();
    }

    // Busca el post asociado a un reto completado usando el referenceId.
    // El post se crea con referenceId = userChallenge.getId() al completar el reto.
    // Devuelve solo el ID del post para que el cliente navegue a él.
    public String getPostIdByUserChallengeId(String userChallengeId) {
        Post post = postRepository.findByReferenceId(userChallengeId)
                .orElseThrow(() -> new BadRequestException("No hay post asociado a este reto"));
        return post.getId();
    }
}
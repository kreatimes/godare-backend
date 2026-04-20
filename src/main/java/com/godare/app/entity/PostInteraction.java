package com.godare.app.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "post_interactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@CompoundIndex(name = "post_user_type_idx", def = "{'postId':1, 'userId':1, 'type':1}")
public class PostInteraction {

    @Id
    private String id;

    @Indexed
    private String postId;

    @Indexed
    private String userId;

    private String type;

    private String commentText;

    private Instant createdAt;

    // Contadores de reacciones — solo se usan cuando type=COMMENT
    @Builder.Default
    private int likesCount = 0;

    @Builder.Default
    private int dislikesCount = 0;
}
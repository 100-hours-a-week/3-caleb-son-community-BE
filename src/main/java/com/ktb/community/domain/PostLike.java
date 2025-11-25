package com.ktb.community.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "post_likes",
       indexes = @Index(name="idx_likes_post", columnList = "post_id"))
public class PostLike {
    @EmbeddedId
    private PostLikeId id;

    @ManyToOne(fetch = FetchType.LAZY) @MapsId("userId")
    @JoinColumn(name="user_id", foreignKey = @ForeignKey(name="fk_likes_user"))
    private User user;

    @ManyToOne(fetch = FetchType.LAZY) @MapsId("postId")
    @JoinColumn(name="post_id", foreignKey = @ForeignKey(name="fk_likes_post"))
    private Post post;

    @Column(name="created_at", nullable=false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() { this.createdAt = this.createdAt == null ? LocalDateTime.now() : this.createdAt; }

    public PostLike() { }
    public PostLike(User user, Post post) {
        this.user = user; this.post = post;
        this.id = new PostLikeId(user.getId(), post.getId());
    }

    public PostLikeId getId() { return id; }
    public User getUser() { return user; }
    public Post getPost() { return post; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}

package com.ktb.community.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "comments",
       indexes = { @Index(name="idx_comments_post_pub", columnList = "post_id,published_at") })
public class Comment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="comment_id", columnDefinition="int unsigned")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name="user_id", foreignKey = @ForeignKey(name="fk_comments_user"))
    private User author;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name="post_id", foreignKey = @ForeignKey(name="fk_comments_post"))
    private Post post;

    @Column(name="content", length=1000, nullable=false)
    private String content;

    @Column(name="is_deleted", nullable=false)
    private boolean deleted = false;

    @Column(name="deleted_at")
    private LocalDateTime deletedAt;

    @Column(name="published_at", nullable=false)
    private LocalDateTime publishedAt;

    @Column(name="updated_at", nullable=false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        var now = LocalDateTime.now();
        this.publishedAt = this.publishedAt == null ? now : this.publishedAt;
        this.updatedAt = this.updatedAt == null ? now : this.updatedAt;
    }
    @PreUpdate
    public void preUpdate() { this.updatedAt = LocalDateTime.now(); }

    public Integer getId() { return id; }
    public User getAuthor() { return author; }
    public void setAuthor(User author) { this.author = author; }
    public Post getPost() { return post; }
    public void setPost(Post post) { this.post = post; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public boolean isDeleted() { return deleted; }
    public void setDeleted(boolean deleted) { this.deleted = deleted; }
    public LocalDateTime getDeletedAt() { return deletedAt; }
    public void setDeletedAt(LocalDateTime deletedAt) { this.deletedAt = deletedAt; }
    public LocalDateTime getPublishedAt() { return publishedAt; }
    public void setPublishedAt(LocalDateTime publishedAt) { this.publishedAt = publishedAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}

package com.ktb.community.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "posts",
       indexes = { @Index(name="idx_posts_user_pub", columnList = "user_id,published_at") })
public class Post {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="post_id", columnDefinition="int unsigned")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name="user_id", foreignKey = @ForeignKey(name="fk_posts_user"))
    private User author;

    @Column(name="title", length=200, nullable=false)
    private String title;

    @Lob
    @Column(name="content", nullable=false, columnDefinition="longtext")
    private String content;

    @Column(name="is_deleted", nullable=false)
    private boolean deleted = false;

    @Column(name="deleted_at")
    private LocalDateTime deletedAt;

    @Column(name="published_at", nullable=false)
    private LocalDateTime publishedAt;

    @Column(name="updated_at", nullable=false)
    private LocalDateTime updatedAt;

    @Column(name="view_count", nullable=false, columnDefinition="int unsigned")
    private Integer viewCount = 0;

    @Column(name="like_count", nullable=false, columnDefinition="int unsigned")
    private Integer likeCount = 0;

    @Column(name="comment_count", nullable=false, columnDefinition="int unsigned")
    private Integer commentCount = 0;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC")
    private List<PostImage> images = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        var now = LocalDateTime.now();
        this.publishedAt = this.publishedAt == null ? now : this.publishedAt;
        this.updatedAt = this.updatedAt == null ? now : this.updatedAt;
    }
    @PreUpdate
    public void preUpdate() { this.updatedAt = LocalDateTime.now(); }

    public void setImages(List<PostImage> images) {
        this.images.clear();
        if (images != null) {
            for (PostImage img : images) { img.setPost(this); this.images.add(img); }
        }
    }

    public Integer getId() { return id; }
    public User getAuthor() { return author; }
    public void setAuthor(User author) { this.author = author; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
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
    public Integer getViewCount() { return viewCount; }
    public void setViewCount(Integer viewCount) { this.viewCount = viewCount; }
    public Integer getLikeCount() { return likeCount; }
    public void setLikeCount(Integer likeCount) { this.likeCount = likeCount; }
    public Integer getCommentCount() { return commentCount; }
    public void setCommentCount(Integer commentCount) { this.commentCount = commentCount; }
    public java.util.List<PostImage> getImages() { return images; }
}

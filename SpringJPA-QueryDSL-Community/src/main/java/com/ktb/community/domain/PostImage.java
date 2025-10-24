package com.ktb.community.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "post_images",
       uniqueConstraints = @UniqueConstraint(name="uq_post_images_sort", columnNames = {"post_id","sort_order"}),
       indexes = @Index(name="idx_post_images_post", columnList = "post_id"))
public class PostImage {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="image_id", columnDefinition="int unsigned")
    private Integer id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name="post_id", foreignKey = @ForeignKey(name="fk_post_images_post"))
    private Post post;

    @Column(name="image_url", length=512, nullable=false)
    private String imageUrl;

    @Column(name="sort_order", columnDefinition="int unsigned", nullable=false)
    private Integer sortOrder;

    @Column(name="created_at", nullable=false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() { this.createdAt = this.createdAt == null ? LocalDateTime.now() : this.createdAt; }

    public Integer getId() { return id; }
    public Post getPost() { return post; }
    public void setPost(Post post) { this.post = post; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}

package com.ktb.community.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import java.time.LocalDateTime;

@Entity
@Table(name = "users",
       uniqueConstraints = {
           @UniqueConstraint(name="uq_users_email", columnNames = "email"),
           @UniqueConstraint(name="uq_users_nickname", columnNames = "nickname")
       })
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="user_id", columnDefinition="int unsigned")
    private Integer id;

    @Column(name="nickname", length=50, nullable=false)
    private String nickname;

    @Email
    @Column(name="email", length=255, nullable=false)
    private String email;

    @JsonIgnore
    @Column(name="password", length=255, nullable=false)
    private String password;

    @Column(name="profile_image_url", length=512)
    private String profileImageUrl;

    @Column(name="is_deleted", nullable=false)
    private boolean deleted = false;

    @Column(name="deleted_at")
    private LocalDateTime deletedAt;

    @Column(name="created_at", nullable=false)
    private LocalDateTime createdAt;

    @Column(name="updated_at", nullable=false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = this.createdAt == null ? LocalDateTime.now() : this.createdAt;
        this.updatedAt = this.updatedAt == null ? LocalDateTime.now() : this.updatedAt;
    }
    @PreUpdate
    public void preUpdate() { this.updatedAt = LocalDateTime.now(); }

    public Integer getId() { return id; }
    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getProfileImageUrl() { return profileImageUrl; }
    public void setProfileImageUrl(String profileImageUrl) { this.profileImageUrl = profileImageUrl; }
    public boolean isDeleted() { return deleted; }
    public void setDeleted(boolean deleted) { this.deleted = deleted; }
    public LocalDateTime getDeletedAt() { return deletedAt; }
    public void setDeletedAt(LocalDateTime deletedAt) { this.deletedAt = deletedAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}

package com.ktb.community.domain;

import jakarta.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
public class PostLikeId implements Serializable {
    private Integer userId;
    private Integer postId;

    public PostLikeId() { }
    public PostLikeId(Integer userId, Integer postId) { this.userId = userId; this.postId = postId; }

    public Integer getUserId() { return userId; }
    public Integer getPostId() { return postId; }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PostLikeId other)) return false;
        return java.util.Objects.equals(userId, other.userId) && java.util.Objects.equals(postId, other.postId);
    }
    @Override public int hashCode() { return java.util.Objects.hash(userId, postId); }
}

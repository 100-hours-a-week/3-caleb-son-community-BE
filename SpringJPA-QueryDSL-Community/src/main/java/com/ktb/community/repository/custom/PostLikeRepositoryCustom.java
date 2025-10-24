package com.ktb.community.repository.custom;

import com.ktb.community.domain.Post;
import com.ktb.community.domain.PostLike;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;

public interface PostLikeRepositoryCustom {
    Optional<PostLike> findByUserIdAndPostId(Integer userId, Integer postId);
    long countByPostId(Integer postId);
    Page<Post> findPostsLikedByUser(Integer userId, Pageable pageable);
}

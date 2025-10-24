package com.ktb.community.repository.custom;

import com.ktb.community.domain.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.time.LocalDateTime;
import java.util.Optional;

public interface PostRepositoryCustom {
    Page<Post> search(String query, Integer authorId, Boolean hasImage,
                      LocalDateTime from, LocalDateTime to, Pageable pageable);
    Optional<Post> findActiveById(Integer postId);
}

package com.ktb.community.repository.custom;

import com.ktb.community.domain.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface CommentRepositoryCustom {
    List<Comment> findActiveByPostId(Integer postId);
    Page<Comment> findActiveByPostIdWithPagination(Integer postId, Pageable pageable);
}

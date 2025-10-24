package com.ktb.community.repository.custom;

import com.ktb.community.domain.Comment;
import java.util.List;

public interface CommentRepositoryCustom {
    List<Comment> findActiveByPostId(Integer postId);
}

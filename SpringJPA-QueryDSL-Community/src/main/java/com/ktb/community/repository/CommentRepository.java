package com.ktb.community.repository;

import com.ktb.community.domain.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Integer>, com.ktb.community.repository.custom.CommentRepositoryCustom { }

package com.ktb.community.repository;

import com.ktb.community.domain.Post;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Integer>, com.ktb.community.repository.custom.PostRepositoryCustom { }

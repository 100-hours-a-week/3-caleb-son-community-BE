package com.ktb.community.repository;

import com.ktb.community.domain.PostLike;
import com.ktb.community.domain.PostLikeId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostLikeRepository extends JpaRepository<PostLike, PostLikeId>, com.ktb.community.repository.custom.PostLikeRepositoryCustom { }

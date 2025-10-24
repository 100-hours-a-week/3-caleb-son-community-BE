package com.ktb.community.repository;

import com.ktb.community.domain.PostImage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostImageRepository extends JpaRepository<PostImage, Integer>, com.ktb.community.repository.custom.PostImageRepositoryCustom { }

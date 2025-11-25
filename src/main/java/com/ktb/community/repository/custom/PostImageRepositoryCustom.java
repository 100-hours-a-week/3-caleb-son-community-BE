package com.ktb.community.repository.custom;

import com.ktb.community.domain.PostImage;
import java.util.List;

public interface PostImageRepositoryCustom {
    List<PostImage> findByPostIdOrderBySort(Integer postId);
}

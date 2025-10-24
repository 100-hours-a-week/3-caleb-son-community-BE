package com.ktb.community.repository.impl;

import com.ktb.community.domain.PostImage;
import com.ktb.community.domain.QPostImage;
import com.ktb.community.repository.custom.PostImageRepositoryCustom;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class PostImageRepositoryImpl implements PostImageRepositoryCustom {
    private final JPAQueryFactory queryFactory;
    public PostImageRepositoryImpl(JPAQueryFactory queryFactory) { this.queryFactory = queryFactory; }

    @Override
    public List<PostImage> findByPostIdOrderBySort(Integer postId) {
        QPostImage i = QPostImage.postImage;
        return queryFactory.selectFrom(i)
                .where(i.post.id.eq(postId))
                .orderBy(i.sortOrder.asc(), i.id.asc())
                .fetch();
    }
}

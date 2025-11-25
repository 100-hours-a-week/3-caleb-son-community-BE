package com.ktb.community.repository.impl;

import com.ktb.community.domain.*;
import com.ktb.community.repository.custom.PostLikeRepositoryCustom;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class PostLikeRepositoryImpl implements PostLikeRepositoryCustom {
    private final JPAQueryFactory queryFactory;
    public PostLikeRepositoryImpl(JPAQueryFactory queryFactory) { this.queryFactory = queryFactory; }

    @Override
    public Optional<PostLike> findByUserIdAndPostId(Integer userId, Integer postId) {
        QPostLike pl = QPostLike.postLike;
        PostLike found = queryFactory.selectFrom(pl)
                .where(pl.user.id.eq(userId).and(pl.post.id.eq(postId)))
                .fetchOne();
        return Optional.ofNullable(found);
    }

    @Override
    public long countByPostId(Integer postId) {
        QPostLike pl = QPostLike.postLike;
        Long cnt = queryFactory.select(pl.count())
                .from(pl)
                .where(pl.post.id.eq(postId))
                .fetchOne();
        return cnt == null ? 0 : cnt;
    }

    @Override
    public Page<Post> findPostsLikedByUser(Integer userId, Pageable pageable) {
        QPostLike pl = QPostLike.postLike;
        QPost p = QPost.post;

        List<Post> content = queryFactory
                .select(pl.post)
                .from(pl)
                .join(pl.post, p).fetchJoin()
                .where(pl.user.id.eq(userId))
                .orderBy(new OrderSpecifier<>(Order.DESC, pl.createdAt), new OrderSpecifier<>(Order.DESC, p.id))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(pl.count())
                .from(pl)
                .where(pl.user.id.eq(userId))
                .fetchOne();

        return new PageImpl<>(content, pageable, total == null ? 0 : total);
    }
}

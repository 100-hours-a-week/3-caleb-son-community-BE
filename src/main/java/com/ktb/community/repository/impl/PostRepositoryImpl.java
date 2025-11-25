package com.ktb.community.repository.impl;

import com.ktb.community.domain.Post;
import com.ktb.community.domain.QPost;
import com.ktb.community.domain.QPostImage;
import com.ktb.community.domain.QUser;
import com.ktb.community.repository.custom.PostRepositoryCustom;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class PostRepositoryImpl implements PostRepositoryCustom {
    private final JPAQueryFactory queryFactory;
    public PostRepositoryImpl(JPAQueryFactory queryFactory) { this.queryFactory = queryFactory; }

    @Override
    public Page<Post> search(String query, Integer authorId, Boolean hasImage,
                             LocalDateTime from, LocalDateTime to, Pageable pageable) {
        QPost p = QPost.post;
        QUser u = QUser.user;
        QPostImage i = QPostImage.postImage;

        BooleanBuilder where = new BooleanBuilder();
        where.and(p.deleted.isFalse());
        if (query != null && !query.isBlank()) {
            where.and(p.title.containsIgnoreCase(query).or(p.content.containsIgnoreCase(query)));
        }
        if (authorId != null) where.and(p.author.id.eq(authorId));
        if (from != null) where.and(p.publishedAt.goe(from));
        if (to != null) where.and(p.publishedAt.loe(to));
        if (Boolean.TRUE.equals(hasImage)) {
            where.and(JPAExpressions.selectOne().from(i).where(i.post.eq(p)).exists());
        }

        List<OrderSpecifier<?>> orders = new ArrayList<>();
        if (pageable.getSort().isUnsorted()) {
            orders.add(new OrderSpecifier<>(Order.DESC, p.publishedAt));
        } else {
            for (Sort.Order o : pageable.getSort()) {
                switch (o.getProperty()) {
                    case "publishedAt" -> orders.add(new OrderSpecifier<>(o.isAscending() ? Order.ASC : Order.DESC, p.publishedAt));
                    case "likeCount" -> orders.add(new OrderSpecifier<>(o.isAscending() ? Order.ASC : Order.DESC, p.likeCount));
                    case "viewCount" -> orders.add(new OrderSpecifier<>(o.isAscending() ? Order.ASC : Order.DESC, p.viewCount));
                    default -> orders.add(new OrderSpecifier<>(Order.DESC, p.publishedAt));
                }
            }
        }
        orders.add(new OrderSpecifier<>(Order.DESC, p.id));

        var content = queryFactory
                .selectFrom(p)
                .leftJoin(p.author, u).fetchJoin()
                .where(where)
                .orderBy(orders.toArray(new OrderSpecifier<?>[0]))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(p.count())
                .from(p)
                .where(where)
                .fetchOne();

        return new PageImpl<>(content, pageable, total == null ? 0 : total);
    }

    @Override
    public Optional<Post> findActiveById(Integer postId) {
        QPost p = QPost.post;
        QUser u = QUser.user;
        var found = queryFactory
                .selectFrom(p)
                .leftJoin(p.author, u).fetchJoin()
                .where(p.id.eq(postId).and(p.deleted.isFalse()))
                .fetchOne();
        return Optional.ofNullable(found);
    }
}

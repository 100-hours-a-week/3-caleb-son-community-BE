package com.ktb.community.repository.impl;

import com.ktb.community.domain.Comment;
import com.ktb.community.domain.QComment;
import com.ktb.community.domain.QPost;
import com.ktb.community.domain.QUser;
import com.ktb.community.repository.custom.CommentRepositoryCustom;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class CommentRepositoryImpl implements CommentRepositoryCustom {
    private final JPAQueryFactory queryFactory;
    public CommentRepositoryImpl(JPAQueryFactory queryFactory) { this.queryFactory = queryFactory; }

    @Override
    public List<Comment> findActiveByPostId(Integer postId) {
        QComment c = QComment.comment;
        QPost p = QPost.post;
        return queryFactory
                .selectFrom(c)
                .join(c.post, p)
                .join(c.author).fetchJoin()
                .where(p.id.eq(postId).and(c.deleted.isFalse()))
                .orderBy(c.publishedAt.asc(), c.id.asc())
                .fetch();
    }

    @Override
    public Page<Comment> findActiveByPostIdWithPagination(Integer postId, Pageable pageable) {
        QComment c = QComment.comment;
        QPost p = QPost.post;
        QUser u = QUser.user;
        
        List<Comment> content = queryFactory
                .selectFrom(c)
                .join(c.post, p)
                .leftJoin(c.author, u).fetchJoin()
                .where(p.id.eq(postId).and(c.deleted.isFalse()))
                .orderBy(c.publishedAt.asc(), c.id.asc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
        
        Long total = queryFactory
                .select(c.count())
                .from(c)
                .join(c.post, p)
                .where(p.id.eq(postId).and(c.deleted.isFalse()))
                .fetchOne();
        
        return new PageImpl<>(content, pageable, total == null ? 0 : total);
    }
}

package com.ktb.community.repository.impl;

import com.ktb.community.domain.Comment;
import com.ktb.community.domain.QComment;
import com.ktb.community.domain.QPost;
import com.ktb.community.repository.custom.CommentRepositoryCustom;
import com.querydsl.jpa.impl.JPAQueryFactory;
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
}

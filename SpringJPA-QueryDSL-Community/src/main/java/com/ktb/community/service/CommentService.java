package com.ktb.community.service;

import com.ktb.community.domain.*;
import com.ktb.community.dto.CommentDtos.*;
import com.ktb.community.exception.ApiException;
import com.ktb.community.exception.ErrorCode;
import com.ktb.community.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CommentService {
    private final CommentRepository comments;
    private final PostRepository posts;
    private final UserRepository users;

    public CommentService(CommentRepository comments, PostRepository posts, UserRepository users) {
        this.comments = comments; this.posts = posts; this.users = users;
    }

    public List<Comment> list(Integer postId) {
        posts.findActiveById(postId).orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "post_not_found"));
        return comments.findActiveByPostId(postId);
    }

    @Transactional
    public Comment create(Integer userId, Integer postId, CreateCommentRequest req) {
        Post post = posts.findActiveById(postId).orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "post_not_found"));
        User user = users.findById(userId).orElseThrow(() -> new ApiException(ErrorCode.UNAUTHORIZED, "token_not_valid"));
        Comment c = new Comment();
        c.setPost(post); c.setAuthor(user); c.setContent(req.content());
        c = comments.save(c);
        post.setCommentCount(Math.max(0, post.getCommentCount() + 1)); posts.save(post);
        return c;
    }

    public Comment update(Integer userId, Integer commentId, UpdateCommentRequest req) {
        Comment c = comments.findById(commentId).orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "comment_not_found"));
        if (!c.getAuthor().getId().equals(userId)) throw new ApiException(ErrorCode.FORBIDDEN, "not_authorized");
        c.setContent(req.content());
        return comments.save(c);
    }

    @Transactional
    public void softDelete(Integer userId, Integer commentId) {
        Comment c = comments.findById(commentId).orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "comment_not_found"));
        if (!c.getAuthor().getId().equals(userId)) throw new ApiException(ErrorCode.FORBIDDEN, "not_authorized");
        if (!c.isDeleted()) {
            c.setDeleted(true); comments.save(c);
            Post p = c.getPost();
            p.setCommentCount(Math.max(0, p.getCommentCount() - 1)); posts.save(p);
        }
    }
}

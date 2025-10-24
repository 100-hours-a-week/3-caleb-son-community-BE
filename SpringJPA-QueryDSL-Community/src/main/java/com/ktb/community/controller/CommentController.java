package com.ktb.community.controller;

import com.ktb.community.domain.Comment;
import com.ktb.community.dto.ApiResponse;
import com.ktb.community.dto.CommentDtos.*;
import com.ktb.community.service.CommentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/posts/{postId}/comments")
public class CommentController {
    private final CommentService comments;
    public CommentController(CommentService comments) { this.comments = comments; }

    @GetMapping
    public ResponseEntity<?> list(@PathVariable Integer postId) {
        List<Comment> list = comments.list(postId);
        return ResponseEntity.ok(new ApiResponse<>("get_comments_success",
                java.util.Map.of("comments", list, "pagination", java.util.Map.of("total_count", list.size()))));
    }

    @PostMapping
    public ResponseEntity<?> create(@PathVariable Integer postId,
                                    @RequestHeader("X-USER-ID") Integer userId,
                                    @RequestBody @Validated CreateCommentRequest req) {
        Comment c = comments.create(userId, postId, req);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>("create_comment_success",
                java.util.Map.of("commentId", c.getId(), "content", c.getContent())));
    }

    @PatchMapping("/{commentId}")
    public ResponseEntity<?> update(@PathVariable Integer postId,
                                    @PathVariable Integer commentId,
                                    @RequestHeader("X-USER-ID") Integer userId,
                                    @RequestBody @Validated UpdateCommentRequest req) {
        Comment c = comments.update(userId, commentId, req);
        return ResponseEntity.ok(new ApiResponse<>("update_comment_success",
                java.util.Map.of("commentId", c.getId(), "content", c.getContent())));
    }

    @PutMapping("/{commentId}")
    public ResponseEntity<?> deleteSoft(@PathVariable Integer postId,
                                        @PathVariable Integer commentId,
                                        @RequestHeader("X-USER-ID") Integer userId) {
        comments.softDelete(userId, commentId);
        return ResponseEntity.ok(new ApiResponse<>("delete_comment_success", java.util.Map.of("commentId", commentId)));
    }
}

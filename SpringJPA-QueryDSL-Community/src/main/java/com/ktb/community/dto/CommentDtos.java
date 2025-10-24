package com.ktb.community.dto;

import jakarta.validation.constraints.*;

public class CommentDtos {
    public record CreateCommentRequest(@NotBlank @Size(max=1000) String content) { }
    public record UpdateCommentRequest(@NotBlank @Size(max=1000) String content) { }
}

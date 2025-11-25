package com.ktb.community.dto;

import jakarta.validation.constraints.*;
import java.util.List;

public class PostDtos {
    public record CreatePostRequest(@NotBlank String title, @NotBlank String content, String imageUrls) { }
    public record UpdatePostRequest(String title, String content, String imageUrls) { }
}

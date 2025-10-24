package com.ktb.community.dto;

import jakarta.validation.constraints.*;

public class UserDtos {
    public record SignupRequest(@NotBlank @Size(max=50) String nickname,
                                @Email @NotBlank String email,
                                @NotBlank @Size(min=8, max=255) String password,
                                @Size(max=512) String profile_image_url) { }
    
    public record LoginRequest(@Email @NotBlank String email,
                              @NotBlank String password) { }
    
    public record UpdateProfileRequest(@NotBlank @Size(max=50) String nickname,
                                      @Size(max=512) String profile_image_url) { }
    
    public record ChangePasswordRequest(@NotBlank String currentPassword,
                                       @NotBlank @Size(min=8, max=255) String newPassword) { }
}

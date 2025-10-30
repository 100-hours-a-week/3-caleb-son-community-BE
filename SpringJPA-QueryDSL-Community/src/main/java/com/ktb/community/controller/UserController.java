package com.ktb.community.controller;

import com.ktb.community.domain.User;
import com.ktb.community.dto.ApiResponse;
import com.ktb.community.dto.UserDtos.*;
import com.ktb.community.service.UserService;
import com.ktb.community.service.S3Service;
import com.ktb.community.util.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService users;
    private final S3Service s3Service;
    private final JwtUtil jwtUtil;
    
    public UserController(UserService users, S3Service s3Service, JwtUtil jwtUtil) { 
        this.users = users; 
        this.s3Service = s3Service;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody @Validated SignupRequest req) {
        User u = users.signup(req);
        return ResponseEntity.ok(new ApiResponse<>("register_success", java.util.Map.of("userId", u.getId())));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Validated LoginRequest req) {
        try {
            User user = users.login(req);
            
            // JWT 토큰 생성
            String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getEmail(), user.getNickname());
            String refreshToken = jwtUtil.generateRefreshToken(user.getId());
            
            java.util.Map<String, Object> responseData = new java.util.HashMap<>();
            responseData.put("userId", user.getId());
            responseData.put("email", user.getEmail());
            responseData.put("nickname", user.getNickname());
            responseData.put("profileImageUrl", user.getProfileImageUrl() != null ? user.getProfileImageUrl() : "");
            responseData.put("accessToken", accessToken);
            responseData.put("refreshToken", refreshToken);
            responseData.put("tokenType", "Bearer");
            
            return ResponseEntity.ok(new ApiResponse<>("login_success", responseData));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>("login_failed", java.util.Map.of("error", e.getMessage())));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>("login_failed", java.util.Map.of("error", "로그인 중 오류가 발생했습니다.")));
        }
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(HttpServletRequest request,
                                          @RequestBody @Validated UpdateProfileRequest req) {
        Integer userId = (Integer) request.getAttribute("userId");
        User user = users.updateProfile(userId, req);
        return ResponseEntity.ok(new ApiResponse<>("update_profile_success", user));
    }

    @PostMapping("/upload-profile-image")
    public ResponseEntity<?> uploadProfileImage(HttpServletRequest request,
                                               @RequestParam("file") MultipartFile file) {
        Integer userId = (Integer) request.getAttribute("userId");
        try {
            // 파일 유효성 검사
            if (file.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse<>("upload_failed", java.util.Map.of("error", "파일이 비어있습니다.")));
            }
            
            // 이미지 파일 타입 검사
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse<>("upload_failed", java.util.Map.of("error", "이미지 파일만 업로드 가능합니다.")));
            }
            
            // 파일 크기 검사 (5MB 제한)
            if (file.getSize() > 5 * 1024 * 1024) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse<>("upload_failed", java.util.Map.of("error", "파일 크기는 5MB 이하여야 합니다.")));
            }
            
            // S3에 프로필 이미지 업로드
            String imageUrl = s3Service.uploadProfileImage(file);
            
            return ResponseEntity.ok(new ApiResponse<>("upload_profile_image_success", java.util.Map.of("imageUrl", imageUrl)));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>("upload_failed", java.util.Map.of("error", e.getMessage())));
        }
    }

    @PutMapping("/change-password")
    public ResponseEntity<?> changePassword(HttpServletRequest request,
                                          @RequestBody @Validated ChangePasswordRequest req) {
        Integer userId = (Integer) request.getAttribute("userId");
        try {
            User user = users.changePassword(userId, req);
            return ResponseEntity.ok(new ApiResponse<>("change_password_success", java.util.Map.of("userId", user.getId())));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>("change_password_failed", java.util.Map.of("error", e.getMessage())));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>("change_password_failed", java.util.Map.of("error", "비밀번호 변경 중 오류가 발생했습니다.")));
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody java.util.Map<String, String> request) {
        try {
            String refreshToken = request.get("refreshToken");
            
            if (refreshToken == null || refreshToken.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse<>("refresh_failed", java.util.Map.of("error", "Refresh token이 필요합니다.")));
            }

            if (!jwtUtil.validateToken(refreshToken)) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse<>("refresh_failed", java.util.Map.of("error", "유효하지 않은 refresh token입니다.")));
            }

            if (jwtUtil.isTokenExpired(refreshToken)) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse<>("refresh_failed", java.util.Map.of("error", "Refresh token이 만료되었습니다.")));
            }

            if (!jwtUtil.isRefreshToken(refreshToken)) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse<>("refresh_failed", java.util.Map.of("error", "잘못된 토큰 타입입니다.")));
            }

            Integer userId = jwtUtil.getUserIdFromToken(refreshToken);
            String email = jwtUtil.getEmailFromToken(refreshToken);
            String nickname = jwtUtil.getNicknameFromToken(refreshToken);

            // 새로운 Access Token 생성
            String newAccessToken = jwtUtil.generateAccessToken(userId, email, nickname);
            
            java.util.Map<String, Object> responseData = new java.util.HashMap<>();
            responseData.put("accessToken", newAccessToken);
            responseData.put("tokenType", "Bearer");
            
            return ResponseEntity.ok(new ApiResponse<>("refresh_success", responseData));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>("refresh_failed", java.util.Map.of("error", "토큰 갱신 중 오류가 발생했습니다.")));
        }
    }
}

package com.ktb.community.controller;

import com.ktb.community.domain.User;
import com.ktb.community.dto.ApiResponse;
import com.ktb.community.dto.UserDtos.*;
import com.ktb.community.service.UserService;
import com.ktb.community.service.S3Service;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService users;
    private final S3Service s3Service;
    
    public UserController(UserService users, S3Service s3Service) { 
        this.users = users; 
        this.s3Service = s3Service;
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody @Validated SignupRequest req) {
        User u = users.signup(req);
        return ResponseEntity.ok(new ApiResponse<>("register_success", java.util.Map.of("userId", u.getId())));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Validated LoginRequest req, HttpServletRequest request) {
        try {
            User user = users.login(req);
            
            // 세션 생성 및 사용자 정보 저장
            HttpSession session = request.getSession(true);
            session.setAttribute("userId", user.getId());
            session.setAttribute("email", user.getEmail());
            session.setAttribute("nickname", user.getNickname());
            session.setAttribute("profileImageUrl", user.getProfileImageUrl());
            
            // 세션 만료 시간 설정 (30분)
            session.setMaxInactiveInterval(30 * 60);
            
            java.util.Map<String, Object> responseData = new java.util.HashMap<>();
            responseData.put("userId", user.getId());
            responseData.put("email", user.getEmail());
            responseData.put("nickname", user.getNickname());
            responseData.put("profileImageUrl", user.getProfileImageUrl() != null ? user.getProfileImageUrl() : "");
            
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
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>("unauthorized", java.util.Map.of("error", "로그인이 필요합니다.")));
        }
        
        Integer userId = (Integer) session.getAttribute("userId");
        User user = users.updateProfile(userId, req);
        
        // 세션 정보 업데이트
        session.setAttribute("nickname", user.getNickname());
        session.setAttribute("profileImageUrl", user.getProfileImageUrl());
        
        return ResponseEntity.ok(new ApiResponse<>("update_profile_success", user));
    }

    @PostMapping("/upload-profile-image")
    public ResponseEntity<?> uploadProfileImage(HttpServletRequest request,
                                               @RequestParam("file") MultipartFile file) {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>("unauthorized", java.util.Map.of("error", "로그인이 필요합니다.")));
        }
        
        Integer userId = (Integer) session.getAttribute("userId");
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
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>("unauthorized", java.util.Map.of("error", "로그인이 필요합니다.")));
        }
        
        Integer userId = (Integer) session.getAttribute("userId");
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

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        try {
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate();
            }
            return ResponseEntity.ok(new ApiResponse<>("logout_success", java.util.Map.of("message", "로그아웃되었습니다.")));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>("logout_failed", java.util.Map.of("error", "로그아웃 중 오류가 발생했습니다.")));
        }
    }
}

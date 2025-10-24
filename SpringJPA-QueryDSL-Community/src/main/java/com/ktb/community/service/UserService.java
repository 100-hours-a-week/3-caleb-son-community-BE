package com.ktb.community.service;

import com.ktb.community.domain.User;
import com.ktb.community.dto.UserDtos.*;
import com.ktb.community.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {
    private final UserRepository users;
    public UserService(UserRepository users) { this.users = users; }

    public User signup(SignupRequest req) {
        User u = new User();
        u.setEmail(req.email()); u.setPassword(req.password()); u.setNickname(req.nickname()); u.setProfileImageUrl(req.profile_image_url());
        return users.save(u);
    }

    public User login(LoginRequest req) {
        User user = users.findByEmail(req.email())
                .orElseThrow(() -> new RuntimeException("이메일 또는 비밀번호가 올바르지 않습니다."));
        
        if (!user.getPassword().equals(req.password())) {
            throw new RuntimeException("이메일 또는 비밀번호가 올바르지 않습니다.");
        }
        
        return user;
    }

    @Transactional
    public User updateProfile(Integer userId, UpdateProfileRequest req) {
        User user = users.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        
        user.setNickname(req.nickname());
        if (req.profile_image_url() != null) {
            user.setProfileImageUrl(req.profile_image_url());
        }
        
        return users.save(user);
    }

    @Transactional
    public User changePassword(Integer userId, ChangePasswordRequest req) {
        User user = users.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        
        // 현재 비밀번호 확인
        if (!user.getPassword().equals(req.currentPassword())) {
            throw new RuntimeException("현재 비밀번호가 일치하지 않습니다.");
        }
        
        // 새 비밀번호 설정
        user.setPassword(req.newPassword());
        
        return users.save(user);
    }
}

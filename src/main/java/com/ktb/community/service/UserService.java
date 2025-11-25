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
        // 닉네임 유효성 검사
        validateNickname(req.nickname());
        
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
        
        // 닉네임 유효성 검사
        validateNickname(req.nickname());
        
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
        
        // 현재 비밀번호와 새 비밀번호가 같은지 확인
        if (user.getPassword().equals(req.newPassword())) {
            throw new RuntimeException("새 비밀번호는 현재 비밀번호와 달라야 합니다.");
        }
        
        // 새 비밀번호 유효성 검사 (추가 검증)
        if (req.newPassword().length() < 8) {
            throw new RuntimeException("새 비밀번호는 최소 8자 이상이어야 합니다.");
        }
        
        // 새 비밀번호 설정
        user.setPassword(req.newPassword());
        
        return users.save(user);
    }

    @Transactional
    public void withdrawUser(Integer userId) {
        User user = users.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        
        // 사용자 삭제 (실제로는 soft delete나 상태 변경을 권장하지만, 여기서는 실제 삭제)
        users.delete(user);
    }
    
    // 닉네임 유효성 검사 메서드
    private void validateNickname(String nickname) {
        // 길이 체크는 @Size 어노테이션으로 처리됨 (2-10자)
        
        // 공백 체크
        if (nickname.contains(" ")) {
            throw new RuntimeException("닉네임에 공백을 포함할 수 없습니다.");
        }
        
        // 허용된 문자만 사용 (한글, 영어, 숫자, 언더바)
        if (!nickname.matches("^[가-힣a-zA-Z0-9_]+$")) {
            throw new RuntimeException("한글, 영어, 숫자, 언더바(_)만 사용 가능합니다.");
        }
        
        // 숫자만으로 구성되면 안됨
        if (nickname.matches("^[0-9]+$")) {
            throw new RuntimeException("숫자만으로 닉네임을 구성할 수 없습니다.");
        }
    }
}

package com.ktb.community.filter;

import com.ktb.community.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        String path = request.getRequestURI();
        String method = request.getMethod();
        
        // 공개 API는 JWT 검증 생략
        if (isPublicPath(path, method)) {
            filterChain.doFilter(request, response);
            return;
        }
        
        // 선택적 인증 API (토큰 있으면 userId 설정, 없으면 그냥 통과)
        if (isOptionalAuthPath(path, method)) {
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                try {
                    String token = authHeader.substring(7);
                    if (jwtUtil.validateToken(token) && !jwtUtil.isTokenExpired(token) && jwtUtil.isAccessToken(token)) {
                        Integer userId = jwtUtil.getUserIdFromToken(token);
                        request.setAttribute("userId", userId);
                    }
                } catch (Exception e) {
                    // 선택적 인증이므로 토큰 오류가 있어도 통과
                }
            }
            filterChain.doFilter(request, response);
            return;
        }

        // 필수 인증 API
        try {
            // Authorization 헤더에서 JWT 토큰 추출
            String authHeader = request.getHeader("Authorization");
            
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                
                // 토큰 유효성 검증
                if (jwtUtil.validateToken(token) && !jwtUtil.isTokenExpired(token)) {
                    // Access Token인지 확인
                    if (jwtUtil.isAccessToken(token)) {
                        // 토큰에서 userId 추출
                        Integer userId = jwtUtil.getUserIdFromToken(token);
                        
                        // request에 userId 속성 추가 (컨트롤러에서 사용)
                        request.setAttribute("userId", userId);
                        
                        filterChain.doFilter(request, response);
                        return;
                    }
                }
            }
            
            // 토큰이 없거나 유효하지 않은 경우
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write("{\"message\":\"unauthorized\",\"data\":{\"error\":\"인증이 필요합니다.\"}}");
            
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write("{\"message\":\"invalid_token\",\"data\":{\"error\":\"유효하지 않은 토큰입니다.\"}}");
        }
    }

    /**
     * 공개 API 경로 확인
     */
    private boolean isPublicPath(String path, String method) {
        // 로그인, 회원가입, 토큰 갱신은 공개
        if (path.equals("/users/login") || 
            path.equals("/users/signup") || 
            path.equals("/users/refresh")) {
            return true;
        }
        
        // 회원가입용 공개 프로필 이미지 업로드
        if (path.equals("/users/upload-profile-image-public") && method.equals("POST")) {
            return true;
        }
        
        // 게시글 목록만 공개 (상세는 선택적 인증)
        if (path.equals("/posts") && method.equals("GET")) {
            return true;
        }
        
        // 댓글 목록 조회는 공개 (GET 요청만)
        if (path.matches("/posts/\\d+/comments") && method.equals("GET")) {
            return true;
        }
        
        // Privacy 페이지는 공개
        if (path.startsWith("/privacy")) {
            return true;
        }
        
        return false;
    }
    
    /**
     * 선택적 인증 경로 확인 (토큰이 있으면 검증, 없으면 통과)
     */
    private boolean isOptionalAuthPath(String path, String method) {
        // 게시글 상세 조회는 선택적 인증
        if (path.matches("/posts/\\d+") && method.equals("GET")) {
            return true;
        }
        return false;
    }
}


package com.ktb.community.config;

import com.ktb.community.filter.SessionAuthInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    private final SessionAuthInterceptor sessionAuthInterceptor;
    
    public WebConfig(SessionAuthInterceptor sessionAuthInterceptor) {
        this.sessionAuthInterceptor = sessionAuthInterceptor;
    }
    
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:3003", "http://127.0.0.1:3003")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(sessionAuthInterceptor)
                .addPathPatterns("/api/**") // 모든 API 경로에 적용
                .excludePathPatterns(
                        "/api/users/signup",     // 회원가입
                        "/api/users/login",      // 로그인
                        "/api/posts",            // 게시글 목록 조회 (공개)
                        "/api/posts/*",          // 게시글 상세 조회 (공개)
                        "/api/posts/*/comments", // 댓글 목록 조회 (공개)
                        "/api/privacy/**"        // 개인정보처리방침 (공개)
                );
    }
}

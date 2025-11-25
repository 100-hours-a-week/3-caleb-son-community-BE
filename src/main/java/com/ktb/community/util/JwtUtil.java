package com.ktb.community.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtil {

    @Value("${jwt.secret:ktb-community-secret-key-for-jwt-token-generation-and-validation}")
    private String secret;

    @Value("${jwt.access-token-expiration:900000}") // 15분 (밀리초)
    private long accessTokenExpiration;

    @Value("${jwt.refresh-token-expiration:604800000}") // 7일 (밀리초)
    private long refreshTokenExpiration;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    /**
     * Access Token 생성
     */
    public String generateAccessToken(Integer userId, String email, String nickname) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("email", email);
        claims.put("nickname", nickname);
        claims.put("type", "access");

        return createToken(claims, userId.toString(), accessTokenExpiration);
    }

    /**
     * Refresh Token 생성
     */
    public String generateRefreshToken(Integer userId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("type", "refresh");

        return createToken(claims, userId.toString(), refreshTokenExpiration);
    }

    /**
     * JWT 토큰 생성
     */
    private String createToken(Map<String, Object> claims, String subject, long expiration) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * 토큰에서 사용자 ID 추출
     */
    public Integer getUserIdFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.get("userId", Integer.class);
    }

    /**
     * 토큰에서 이메일 추출
     */
    public String getEmailFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.get("email", String.class);
    }

    /**
     * 토큰에서 닉네임 추출
     */
    public String getNicknameFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.get("nickname", String.class);
    }

    /**
     * 토큰 타입 확인 (access 또는 refresh)
     */
    public String getTokenType(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.get("type", String.class);
    }

    /**
     * 토큰에서 Claims 추출
     */
    private Claims getClaimsFromToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 토큰 유효성 검증
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * 토큰 만료 확인
     */
    public boolean isTokenExpired(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            return claims.getExpiration().before(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            return true;
        }
    }

    /**
     * Access Token인지 확인
     */
    public boolean isAccessToken(String token) {
        try {
            return "access".equals(getTokenType(token));
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Refresh Token인지 확인
     */
    public boolean isRefreshToken(String token) {
        try {
            return "refresh".equals(getTokenType(token));
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 토큰에서 만료 시간 추출
     */
    public Date getExpirationDateFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.getExpiration();
    }
}


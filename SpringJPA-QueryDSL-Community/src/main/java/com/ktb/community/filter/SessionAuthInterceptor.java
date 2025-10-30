package com.ktb.community.filter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 세션 기반 인증 인터셉터
 * 인증이 필요한 요청에 대해 세션을 검증합니다.
 */
@Component
public class SessionAuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // OPTIONS 메서드는 통과 (CORS preflight)
        if ("OPTIONS".equals(request.getMethod())) {
            return true;
        }

        HttpSession session = request.getSession(false);
        
        // 세션이 없거나 userId가 없으면 401 반환
        if (session == null || session.getAttribute("userId") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write("{\"message\":\"unauthorized\",\"data\":{\"error\":\"로그인이 필요합니다.\"}}");
            return false;
        }

        // userId를 request attribute에 저장 (컨트롤러에서 사용 가능)
        Integer userId = (Integer) session.getAttribute("userId");
        request.setAttribute("userId", userId);
        
        return true;
    }
}


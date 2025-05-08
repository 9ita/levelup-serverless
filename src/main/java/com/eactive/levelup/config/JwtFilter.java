package com.eactive.levelup.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    /**
     * JWT 파싱·검증 로직을 담고 있는 제공자
     */
    private final JwtTokenProvider jwt;

    /**
     * 토큰 검증을 건너뛸 경로 목록
     */
    private static final List<String> WHITELIST = List.of(
            "/auth/signup", "/actuator/health",
            "/css/**", "/js/**", "/images/**", "/assets/**", "/tabulator/**", "fonts/**",
            "/", "/login", "/login/**", "/signup", "/auth/login", "/auth/signup");

    private static final AntPathMatcher matcher = new AntPathMatcher();

    protected void doFilterInternal(HttpServletRequest req,
                                    HttpServletResponse res,
                                    FilterChain chain)
            throws IOException, ServletException {

        String path = req.getServletPath();
        if (WHITELIST.stream().anyMatch(p -> matcher.match(p, path))) {
            chain.doFilter(req, res);
            return;
        }

        String token = null;

        // 1) Authorization 헤더에 토큰이 있는지 확인
        String header = req.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            token = header.substring(7);
        }

        // 2) 헤더가 없으면 쿠키에서 JWT_TOKEN 읽기
        else if (req.getCookies() != null) {
            for (Cookie cookie : req.getCookies()) {
                if ("JWT_TOKEN".equals(cookie.getName())) {
                    token = cookie.getValue();
                    break;
                }
            }
        }

        // 3) 토큰이 있으면 파싱·검증
        if (token != null) {
            try {
                Authentication auth = jwt.parse(token);
                SecurityContextHolder.getContext().setAuthentication(auth);
            } catch (Exception ex) {
                // 만료나 위변조 예외 시 401 리턴
                log.error("JWT Error: {}", ex.getMessage());
                res.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid or expired JWT");
                return;
            }
        }

        chain.doFilter(req, res);
    }
}

package com.eactive.levelup.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    /** JWT 파싱·검증 로직을 담고 있는 제공자 */
    private final JwtTokenProvider jwt;

    /** 토큰 검증을 건너뛸 경로 목록 */
    private static final List<String> WHITELIST = List.of(
            "/auth/login",
            "/auth/signup",
            "/actuator/health",
            "/css/**", "/js/**", "/images/**", "/assets/**", "/tabulator/**",
            "/", "/login", "/login/**", "/signup"
    );
    private static final AntPathMatcher matcher = new AntPathMatcher();

    @Override
    protected void doFilterInternal(HttpServletRequest req,
                                    HttpServletResponse res,
                                    FilterChain chain) throws IOException, ServletException {

        String path = req.getServletPath();

        /* 1. 화이트리스트 URL이면 바로 다음 필터로 */
        if (WHITELIST.stream().anyMatch(p -> matcher.match(p, path))) {
            chain.doFilter(req, res);
            return;
        }

        /* 2. Authorization 헤더가 없으면(비로그인 요청) 그대로 통과 */
        String authHeader = req.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            chain.doFilter(req, res);
            return;
        }

        /* 3. 토큰이 있는 경우에만 검증 */
        try {
            String token = authHeader.substring(7);        // "Bearer " 이후만 추출
            Authentication auth = jwt.parse(token);        // 유효하지 않으면 예외 발생
            SecurityContextHolder.getContext().setAuthentication(auth);
            chain.doFilter(req, res);                      // 정상 통과
        } catch (Exception ex) {
            // 토큰 불일치·만료·서명 오류 등 → 401 Unauthorized
            res.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid or expired JWT");
        }
    }
}

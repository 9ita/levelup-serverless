package com.eactive.levelup.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.annotation.web.configurers.LogoutConfigurer;
import org.springframework.security.config.annotation.web.configurers.RequestCacheConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final LoginSuccessHandler loginSuccessHandler;
    private final LoginFailureHandler loginFailureHandler;

    @Bean
    SecurityFilterChain chain(HttpSecurity http, JwtFilter jwt) throws Exception {
        http
                // ── 무상태 설정 ─────────────────────────────────────────────
                .sessionManagement(sm ->
                        sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .requestCache(RequestCacheConfigurer::disable)

                // ── 인가 규칙 ───────────────────────────────────────────────
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/assets/**", "/tabulator/**").permitAll()
                        .requestMatchers("/", "/login", "/login/**", "/signup").permitAll()
                        .requestMatchers("/auth/signup", "/auth/login").permitAll()
                        .requestMatchers("/error", "/error/**").permitAll()
                        .requestMatchers("/actuator/health").permitAll()
                        .anyRequest().authenticated())

                // ── 로그인 설정 ─────────────────────────────────────────────
                .addFilterBefore(jwt, UsernamePasswordAuthenticationFilter.class)

                // ── CSRF·헤더 ───────────────────────────────────────────────
                .csrf(AbstractHttpConfigurer::disable)
                .headers(h -> h.frameOptions(
                        HeadersConfigurer.FrameOptionsConfig::sameOrigin))

                // ── 로그아웃은 의미 없으므로 비활성(필요 시 enable) ─────────
                .logout(LogoutConfigurer::disable);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }

}


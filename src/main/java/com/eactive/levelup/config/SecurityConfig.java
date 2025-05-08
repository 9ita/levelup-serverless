package com.eactive.levelup.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.annotation.web.configurers.LogoutConfigurer;
import org.springframework.security.config.annotation.web.configurers.RequestCacheConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final LoginSuccessHandler loginSuccessHandler;
    private final LoginFailureHandler loginFailureHandler;
    private final JwtFilter jwtFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // ── 무상태 세션 ───────────────────────────────────
                .sessionManagement(sm ->
                        sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .requestCache(RequestCacheConfigurer::disable)

                // ── CSRF 비활성, X-Frame 허용 ────────────────────
                .csrf(csrf -> csrf.disable())
                .headers(h -> h.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("/login"))
                )

                // ── URL 권한 ─────────────────────────────────────
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/css/**", "/js/**", "/images/**", "/assets/**", "/tabulator/**", "/fonts/**",
                                "/", "/login", "/signup", "/auth/signup",
                                "/actuator/**",
                                "/error"
                        ).permitAll()
                        .anyRequest().authenticated()
                )

                // ── 로그인 처리(formLogin) ───────────────────────
                .formLogin(form -> form
                        .loginPage("/login")                // 로그인 페이지 URL
                        .loginProcessingUrl("/auth/login")  // 로그인 처리 URL
                        .usernameParameter("email")                 // 이메일 필드 이름
                        .passwordParameter("password")              // 비밀번호 필드 이름
                        .successHandler(loginSuccessHandler)        // 성공 핸들러
                        .failureHandler(loginFailureHandler)        // 실패 핸들러
                        .defaultSuccessUrl("/dashboard", true)  // 로그인 성공 시 이동할 URL
                        .permitAll()
                )

                // ── 로그아웃 비활성화 ─────────────────────────────
                .logout(LogoutConfigurer::disable)

                // ── JWT 필터는 formLogin 후, 인증된 요청에서만 실행 ───
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

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


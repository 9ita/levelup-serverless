package com.eactive.levelup.controller.auth;

import com.eactive.levelup.config.JwtTokenProvider;
import com.eactive.levelup.service.SignupService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager authManager;
    private final JwtTokenProvider jwt;

    @PostMapping("/login")
    public Map<String, String> login(@RequestBody LoginRequest req) {
        Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.email(), req.password()));
        String token = jwt.generate((UserDetails) auth.getPrincipal());
        return Map.of("token", token);
    }

    record LoginRequest(String email, String password) {
    }

    private final SignupService signupService;

    @PostMapping("/signup")
    public String signup(@RequestBody SignupRequest req) {
        signupService.signup(req.email(), req.password(), req.name());
        return "회원가입 성공";
    }

    record SignupRequest(String email, String password, String name) {
        public SignupRequest {
            if (email == null || email.isBlank()) {
                throw new IllegalArgumentException("이메일을 입력하세요.");
            }
            if (password == null || password.isBlank()) {
                throw new IllegalArgumentException("비밀번호를 입력하세요.");
            }
            if (name == null || name.isBlank()) {
                throw new IllegalArgumentException("이름을 입력하세요.");
            }
        }
    }

}

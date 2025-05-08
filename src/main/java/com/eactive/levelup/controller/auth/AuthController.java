package com.eactive.levelup.controller.auth;

import com.eactive.levelup.service.SignupService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

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

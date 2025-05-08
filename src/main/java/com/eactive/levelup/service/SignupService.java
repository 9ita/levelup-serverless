package com.eactive.levelup.service;

import com.eactive.levelup.domain.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class SignupService {

    private final DynamoDbEnhancedClient enhancedClient;
    private final PasswordEncoder passwordEncoder;

    private DynamoDbTable<UserEntity> table() {
        return enhancedClient.table("Users", TableSchema.fromBean(UserEntity.class));
    }

    public void signup(String email, String rawPassword, String name) {

        // 1) 중복 이메일 체크
        boolean exists = table().getItem(Key.builder().partitionValue(email).build()) != null;
        if (exists) throw new IllegalStateException("이미 가입된 이메일입니다.");

        // 2) 해시 생성
        String hash = passwordEncoder.encode(rawPassword);

        // 3) 저장
        UserEntity entity = new UserEntity();
        entity.setEmail(email);
        entity.setPasswordHash(hash);
        entity.setName(name);

        // 4) 기본 역할 부여
        Set<String> roles = entity.getRoles();
        if (roles == null || roles.isEmpty()) {
            entity.setRoles(Set.of("USER"));
        }

        table().putItem(entity);
    }
}

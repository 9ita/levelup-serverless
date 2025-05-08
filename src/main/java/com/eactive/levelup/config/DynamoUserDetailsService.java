package com.eactive.levelup.config;

import com.eactive.levelup.domain.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

@Service
@RequiredArgsConstructor
public class DynamoUserDetailsService implements UserDetailsService {

    private final DynamoDbEnhancedClient enhanced;
    private final PasswordEncoder encoder;

    private DynamoDbTable<UserEntity> table() {
        return enhanced.table("Users", TableSchema.fromBean(UserEntity.class));
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        // 1) 이메일로 사용자 조회
        UserEntity e = table().getItem(Key.builder().partitionValue(username).build());
        // 2) 사용자 없으면 예외 발생
        if (e == null) throw new UsernameNotFoundException(username);
        // 3) 사용자 비밀번호 해시와 입력 비밀번호 해시 비교
        return User.withUsername(e.getEmail())
                   .password(e.getPasswordHash())
                   .roles(e.getRoles().toArray(new String[0]))
                   .build();
    }
}

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
        UserEntity e = table().getItem(Key.builder().partitionValue(username).build());
        if (e == null) throw new UsernameNotFoundException(username);
        return User.withUsername(e.getEmail())
                   .password(e.getPasswordHash())
                   .roles(e.getRoles().toArray(new String[0]))
                   .build();
    }
}

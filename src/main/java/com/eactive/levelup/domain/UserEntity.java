package com.eactive.levelup.domain;

import java.util.Set;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

@DynamoDbBean
public class UserEntity {

    private String email;                // PK
    private String passwordHash;
    private String name;
    private Set<String> roles = Set.of();

    @DynamoDbPartitionKey
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String hash) { this.passwordHash = hash; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Set<String> getRoles() { return roles; }
    public void setRoles(Set<String> roles) { this.roles = roles; }
}

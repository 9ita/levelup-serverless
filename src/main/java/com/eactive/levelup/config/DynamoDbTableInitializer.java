package com.eactive.levelup.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.*;
import software.amazon.awssdk.services.dynamodb.*;
import software.amazon.awssdk.services.dynamodb.model.*;

@Configuration
@Profile("local")
public class DynamoDbTableInitializer {

    @Bean
    public CommandLineRunner initTables(DynamoDbClient client) {
        return args -> {
            String tableName = "Users";
            // 1) 이미 있으면 스킵
            if (!client.listTables().tableNames().contains(tableName)) {
                client.createTable(CreateTableRequest.builder()
                    .tableName(tableName)
                    .attributeDefinitions(AttributeDefinition.builder()
                        .attributeName("email")
                        .attributeType(ScalarAttributeType.S)
                        .build())
                    .keySchema(KeySchemaElement.builder()
                        .attributeName("email")
                        .keyType(KeyType.HASH)
                        .build())
                    .provisionedThroughput(ProvisionedThroughput.builder()
                        .readCapacityUnits(5L)
                        .writeCapacityUnits(5L)
                        .build())
                    .build());

                // 2) 테이블이 ACTIVE 상태가 될 때까지 대기
                client.waiter().waitUntilTableExists(DescribeTableRequest.builder()
                    .tableName(tableName).build());
            }
        };
    }
}

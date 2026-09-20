package com.studentcentral.notification.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.Sort;

@Configuration
public class MongoIndexConfig {

    @Bean
    CommandLineRunner ensureIndexes(@Autowired(required = false) MongoTemplate mongoTemplate) {
        return args -> {
            if (mongoTemplate != null) {
                try {
                    var ops = mongoTemplate.indexOps("notifications");
                    if (ops != null) {
                        ops.ensureIndex(new Index().on("notificationId", Sort.Direction.ASC).unique());
                        ops.ensureIndex(new Index().on("userId", Sort.Direction.ASC).on("createdAt", Sort.Direction.DESC));
                        ops.ensureIndex(new Index().on("userId", Sort.Direction.ASC).on("isRead", Sort.Direction.ASC));
                    }
                } catch (Exception ignored) {
                }
            }
        };
    }
}

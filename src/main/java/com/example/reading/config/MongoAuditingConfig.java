package com.example.reading.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

/**
 * Configuration to enable MongoDB auditing for @CreatedDate and @LastModifiedDate.
 */
@Configuration
@EnableMongoAuditing
public class MongoAuditingConfig {
}


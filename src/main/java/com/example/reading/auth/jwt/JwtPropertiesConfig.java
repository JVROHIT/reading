package com.example.reading.auth.jwt;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Enables binding of JWT configuration properties.
 */
@Configuration
@EnableConfigurationProperties(JwtProperties.class)
public class JwtPropertiesConfig {
}


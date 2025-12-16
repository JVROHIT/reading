package com.example.reading.auth.jwt;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for JWT token generation and validation.
 */
@ConfigurationProperties(prefix = "security.jwt")
public class JwtProperties {

    private String secret;
    private long expirationSeconds;

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public long getExpirationSeconds() {
        return expirationSeconds;
    }

    public void setExpirationSeconds(long expirationSeconds) {
        this.expirationSeconds = expirationSeconds;
    }
}


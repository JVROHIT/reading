package com.example.reading.credential.encryption;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Enables binding of encryption configuration properties.
 */
@Configuration
@EnableConfigurationProperties(EncryptionProperties.class)
public class EncryptionConfig {
}


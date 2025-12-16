package com.example.reading.credential.encryption;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Base64;

/**
 * Configuration properties for encryption.
 * Requires APP_ENCRYPTION_KEY_BASE64 environment variable or property.
 */
@ConfigurationProperties(prefix = "app.encryption")
public class EncryptionProperties {

    private static final int REQUIRED_KEY_LENGTH = 32; // 256 bits for AES-256

    /**
     * Base64-encoded 32-byte key for AES-256 encryption.
     */
    private String keyBase64;

    public String getKeyBase64() {
        return keyBase64;
    }

    public void setKeyBase64(String keyBase64) {
        this.keyBase64 = keyBase64;
    }

    /**
     * Returns the decoded encryption key bytes.
     *
     * @return the 32-byte AES key
     * @throws IllegalStateException if key is missing or invalid
     */
    public byte[] getDecodedKey() {
        if (keyBase64 == null || keyBase64.isBlank()) {
            throw new IllegalStateException("Encryption key not configured. Set APP_ENCRYPTION_KEY_BASE64 environment variable.");
        }

        try {
            byte[] decoded = Base64.getDecoder().decode(keyBase64);
            if (decoded.length != REQUIRED_KEY_LENGTH) {
                throw new IllegalStateException(
                        "Encryption key must be exactly " + REQUIRED_KEY_LENGTH + " bytes (256 bits). Got: " + decoded.length + " bytes.");
            }
            return decoded;
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("Encryption key is not valid Base64.", e);
        }
    }

    @PostConstruct
    public void validate() {
        // Fail fast on startup if key is missing or invalid
        getDecodedKey();
    }
}


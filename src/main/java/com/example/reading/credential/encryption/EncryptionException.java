package com.example.reading.credential.encryption;

/**
 * Exception thrown when encryption or decryption fails.
 * Does not include sensitive data in messages.
 */
public class EncryptionException extends RuntimeException {

    public EncryptionException(String message) {
        super(message);
    }

    public EncryptionException(String message, Throwable cause) {
        super(message, cause);
    }
}


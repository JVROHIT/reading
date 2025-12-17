package com.example.reading.service.llm;

/**
 * Exception thrown when no LLM credentials are configured for a user.
 */
public class NoCredentialConfiguredException extends RuntimeException {

    public NoCredentialConfiguredException(String message) {
        super(message);
    }
}


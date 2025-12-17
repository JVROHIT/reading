package com.example.reading.service.llm;

/**
 * Exception thrown when multiple credentials exist and provider selection is ambiguous.
 */
public class AmbiguousCredentialException extends RuntimeException {

    public AmbiguousCredentialException(String message) {
        super(message);
    }
}


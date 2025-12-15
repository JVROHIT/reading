package com.example.reading.auth.exception;

/**
 * Exception thrown when attempting to register with an email that already exists.
 */
public class EmailAlreadyUsedException extends RuntimeException {

    public EmailAlreadyUsedException(String email) {
        super("Email already registered: " + email);
    }
}


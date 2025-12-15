package com.example.reading.auth.exception;

/**
 * Exception thrown when login credentials are invalid (wrong email or password).
 */
public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException() {
        super("Invalid email or password");
    }
}


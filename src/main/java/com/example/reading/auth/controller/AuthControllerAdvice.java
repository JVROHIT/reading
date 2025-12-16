package com.example.reading.auth.controller;

import com.example.reading.auth.exception.EmailAlreadyUsedException;
import com.example.reading.auth.exception.InvalidCredentialsException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

/**
 * Global exception handler for authentication-related exceptions.
 */
@RestControllerAdvice
public class AuthControllerAdvice {

    @ExceptionHandler(EmailAlreadyUsedException.class)
    public ResponseEntity<Map<String, String>> handleEmailAlreadyUsed(EmailAlreadyUsedException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of(
                        "error", "EMAIL_ALREADY_EXISTS",
                        "message", ex.getMessage()
                ));
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<Map<String, String>> handleInvalidCredentials(InvalidCredentialsException ex) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(Map.of(
                        "error", "INVALID_CREDENTIALS",
                        "message", ex.getMessage()
                ));
    }
}

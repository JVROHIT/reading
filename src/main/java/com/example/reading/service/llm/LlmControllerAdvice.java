package com.example.reading.service.llm;

import com.example.reading.credential.exception.CredentialNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

/**
 * Global exception handler for LLM-related exceptions.
 */
@RestControllerAdvice
public class LlmControllerAdvice {

    @ExceptionHandler(NoCredentialConfiguredException.class)
    public ResponseEntity<Map<String, String>> handleNoCredentialConfigured(NoCredentialConfiguredException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of(
                        "error", "NO_CREDENTIAL_CONFIGURED",
                        "message", ex.getMessage()
                ));
    }

    @ExceptionHandler(AmbiguousCredentialException.class)
    public ResponseEntity<Map<String, String>> handleAmbiguousCredential(AmbiguousCredentialException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of(
                        "error", "AMBIGUOUS_CREDENTIAL",
                        "message", ex.getMessage()
                ));
    }

    @ExceptionHandler(CredentialNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleCredentialNotFound(CredentialNotFoundException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(Map.of(
                        "error", "CREDENTIAL_NOT_FOUND",
                        "message", ex.getMessage()
                ));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> handleIllegalState(IllegalStateException ex) {
        // Catches "No authenticated user found" from CurrentUserService
        if (ex.getMessage() != null && ex.getMessage().contains("authenticated")) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(
                            "error", "UNAUTHORIZED",
                            "message", ex.getMessage()
                    ));
        }
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                        "error", "INTERNAL_ERROR",
                        "message", ex.getMessage()
                ));
    }
}


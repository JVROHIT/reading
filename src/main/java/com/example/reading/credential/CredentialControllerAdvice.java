package com.example.reading.credential;

import com.example.reading.credential.exception.CredentialNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

/**
 * Exception handler for credential-related exceptions.
 */
@RestControllerAdvice(assignableTypes = CredentialController.class)
public class CredentialControllerAdvice {

    @ExceptionHandler(CredentialNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleCredentialNotFound(CredentialNotFoundException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(Map.of(
                        "error", "CREDENTIAL_NOT_FOUND",
                        "message", ex.getMessage()
                ));
    }
}


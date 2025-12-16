package com.example.reading.credential.exception;

/**
 * Exception thrown when a credential is not found for the user.
 */
public class CredentialNotFoundException extends RuntimeException {

    public CredentialNotFoundException(String credentialId) {
        super("Credential not found: " + credentialId);
    }
}


package com.example.reading.credential;

import com.example.reading.auth.CurrentUserService;
import com.example.reading.credential.dto.CreateCredentialRequest;
import com.example.reading.credential.dto.CredentialResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for managing API credentials.
 */
@RestController
@RequestMapping("/api/credentials")
public class CredentialController {

    private final CredentialService credentialService;
    private final CurrentUserService currentUserService;

    public CredentialController(CredentialService credentialService,
                                CurrentUserService currentUserService) {
        this.credentialService = credentialService;
        this.currentUserService = currentUserService;
    }

    /**
     * Gets all credentials for the authenticated user.
     *
     * @return list of credentials (without API keys)
     */
    @GetMapping
    public ResponseEntity<List<CredentialResponse>> getCredentials() {
        String userId = currentUserService.getCurrentUserId();
        List<CredentialResponse> credentials = credentialService.getCredentials(userId);
        return ResponseEntity.ok(credentials);
    }

    /**
     * Creates a new credential for the authenticated user.
     *
     * @param request the create request with provider and API key
     * @return the created credential
     */
    @PostMapping
    public ResponseEntity<CredentialResponse> createCredential(
            @Valid @RequestBody CreateCredentialRequest request) {
        String userId = currentUserService.getCurrentUserId();
        CredentialResponse response = credentialService.createCredential(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Deletes a credential for the authenticated user.
     *
     * @param id the credential ID
     * @return 204 No Content on success
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCredential(@PathVariable String id) {
        String userId = currentUserService.getCurrentUserId();
        credentialService.deleteCredential(userId, id);
        return ResponseEntity.noContent().build();
    }
}


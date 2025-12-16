package com.example.reading.credential;

import com.example.reading.credential.dto.CredentialResponse;
import com.example.reading.credential.dto.CreateCredentialRequest;
import com.example.reading.credential.encryption.EncryptionService;
import com.example.reading.credential.exception.CredentialNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service for managing API credentials.
 */
@Service
public class CredentialService {

    private final ApiCredentialRepository credentialRepository;
    private final EncryptionService encryptionService;

    public CredentialService(ApiCredentialRepository credentialRepository,
                             EncryptionService encryptionService) {
        this.credentialRepository = credentialRepository;
        this.encryptionService = encryptionService;
    }

    /**
     * Gets all credentials for a user.
     *
     * @param userId the user ID
     * @return list of credential responses (without API keys)
     */
    public List<CredentialResponse> getCredentials(String userId) {
        return credentialRepository.findByUserId(userId).stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Creates a new credential for a user.
     *
     * @param userId the user ID
     * @param request the create request
     * @return the created credential response
     */
    public CredentialResponse createCredential(String userId, CreateCredentialRequest request) {
        String encryptedKey = encryptionService.encrypt(request.getApiKey());

        ApiCredential credential = ApiCredential.builder()
                .userId(userId)
                .provider(request.getProvider())
                .encryptedApiKey(encryptedKey)
                .build();

        ApiCredential saved = credentialRepository.save(credential);
        return toResponse(saved);
    }

    /**
     * Deletes a credential for a user.
     *
     * @param userId the user ID
     * @param credentialId the credential ID
     * @throws CredentialNotFoundException if credential not found for user
     */
    public void deleteCredential(String userId, String credentialId) {
        ApiCredential credential = credentialRepository.findByIdAndUserId(credentialId, userId)
                .orElseThrow(() -> new CredentialNotFoundException(credentialId));
        credentialRepository.delete(credential);
    }

    /**
     * Gets the decrypted API key for a user and provider.
     *
     * @param userId the user ID
     * @param provider the provider
     * @return the decrypted API key
     * @throws CredentialNotFoundException if no credential found
     */
    public String getDecryptedApiKey(String userId, Provider provider) {
        ApiCredential credential = credentialRepository.findByUserIdAndProvider(userId, provider)
                .orElseThrow(() -> new CredentialNotFoundException(provider.name()));
        return encryptionService.decrypt(credential.getEncryptedApiKey());
    }

    private CredentialResponse toResponse(ApiCredential credential) {
        return CredentialResponse.builder()
                .id(credential.getId())
                .provider(credential.getProvider())
                .createdAt(credential.getCreatedAt())
                .updatedAt(credential.getUpdatedAt())
                .build();
    }
}


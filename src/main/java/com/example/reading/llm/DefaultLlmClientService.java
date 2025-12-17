package com.example.reading.llm;

import com.example.reading.credential.CredentialService;
import com.example.reading.credential.Provider;
import dev.langchain4j.model.chat.ChatLanguageModel;
import org.springframework.stereotype.Service;

/**
 * Default implementation of LlmClientService.
 * Fetches user credentials and builds LLM models via the factory.
 */
@Service
public class DefaultLlmClientService implements LlmClientService {

    private final CredentialService credentialService;
    private final LlmClientFactory llmClientFactory;

    public DefaultLlmClientService(CredentialService credentialService, LlmClientFactory llmClientFactory) {
        this.credentialService = credentialService;
        this.llmClientFactory = llmClientFactory;
    }

    @Override
    public ChatLanguageModel getChatModel(String userId, Provider provider, LlmModelOptions options) {
        validateInputs(userId, provider);

        // Fetch decrypted key - throws CredentialNotFoundException if not found
        String decryptedApiKey = credentialService.getDecryptedApiKey(userId, provider);

        // Build and return model via factory
        return llmClientFactory.createChatModel(provider, decryptedApiKey, options);
    }

    private void validateInputs(String userId, Provider provider) {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("User ID cannot be null or blank");
        }
        if (provider == null) {
            throw new IllegalArgumentException("Provider cannot be null");
        }
    }
}


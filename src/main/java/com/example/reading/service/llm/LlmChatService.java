package com.example.reading.service.llm;

import com.example.reading.auth.CurrentUserService;
import com.example.reading.credential.CredentialService;
import com.example.reading.credential.Provider;
import com.example.reading.credential.dto.CredentialResponse;
import com.example.reading.llm.LlmClientFactory;
import com.example.reading.llm.LlmModelOptions;
import dev.langchain4j.model.chat.ChatLanguageModel;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Wraps calls to the external LLM API.
 * Integrates with user credentials and LlmClientFactory.
 */
@Service
public class LlmChatService {

    private final CurrentUserService currentUserService;
    private final CredentialService credentialService;
    private final LlmClientFactory llmClientFactory;

    public LlmChatService(CurrentUserService currentUserService,
                          CredentialService credentialService,
                          LlmClientFactory llmClientFactory) {
        this.currentUserService = currentUserService;
        this.credentialService = credentialService;
        this.llmClientFactory = llmClientFactory;
    }

    /**
     * Calls the LLM API with the given system and user prompts.
     *
     * @param systemPrompt the system prompt
     * @param userPrompt   the user prompt
     * @param providerName optional provider name (if null, uses single credential or throws if ambiguous)
     * @return the raw string response from the LLM
     * @throws NoCredentialConfiguredException if no credentials are configured
     * @throws AmbiguousCredentialException    if multiple credentials exist and provider not specified
     */
    public String chat(String systemPrompt, String userPrompt, String providerName) {
        String userId = currentUserService.getCurrentUserId();

        // Determine provider
        Provider provider = resolveProvider(userId, providerName);

        // Fetch decrypted key
        String decryptedApiKey = credentialService.getDecryptedApiKey(userId, provider);

        // Build model options (defaults are handled by factory)
        LlmModelOptions options = LlmModelOptions.builder().build();

        // Create model and generate response
        ChatLanguageModel model = llmClientFactory.createChatModel(provider, decryptedApiKey, options);

        // Combine prompts for generation
        String fullPrompt = systemPrompt + "\n\n" + userPrompt;
        return model.generate(fullPrompt);
    }

    private Provider resolveProvider(String userId, String providerName) {
        List<CredentialResponse> credentials = credentialService.getCredentials(userId);

        if (credentials.isEmpty()) {
            throw new NoCredentialConfiguredException("No LLM credentials configured for user");
        }

        if (providerName != null && !providerName.isBlank()) {
            // Validate the requested provider exists
            try {
                Provider requestedProvider = Provider.valueOf(providerName.toUpperCase());
                // Verify user has this provider configured
                boolean hasProvider = credentials.stream()
                        .anyMatch(c -> c.getProvider() == requestedProvider);
                if (!hasProvider) {
                    throw new NoCredentialConfiguredException("No credential configured for provider: " + providerName);
                }
                return requestedProvider;
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid provider: " + providerName);
            }
        }

        if (credentials.size() > 1) {
            throw new AmbiguousCredentialException("Multiple credentials found; provider selection ambiguous");
        }

        // Single credential - use its provider
        return credentials.get(0).getProvider();
    }
}

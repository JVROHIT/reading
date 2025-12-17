package com.example.reading.llm;

import com.example.reading.credential.Provider;
import dev.langchain4j.model.chat.ChatLanguageModel;

/**
 * Service for obtaining LLM chat models for authenticated users.
 */
public interface LlmClientService {

    /**
     * Gets a ChatLanguageModel for the specified user and provider.
     *
     * @param userId   the authenticated user's ID
     * @param provider the LLM provider
     * @param options  optional model configuration (may be null for defaults)
     * @return a configured ChatLanguageModel
     * @throws IllegalArgumentException if userId is blank or provider is null
     * @throws com.example.reading.credential.exception.CredentialNotFoundException if no credential exists for user/provider
     */
    ChatLanguageModel getChatModel(String userId, Provider provider, LlmModelOptions options);
}


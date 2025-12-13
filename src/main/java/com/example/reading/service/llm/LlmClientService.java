package com.example.reading.service.llm;

import org.springframework.stereotype.Service;

/**
 * Wraps calls to the external LLM API.
 */
@Service
public class LlmClientService {

    /**
     * Calls the LLM API with the given system and user prompts.
     *
     * @param systemPrompt the system prompt
     * @param userPrompt the user prompt
     * @return the raw string response from the LLM
     * @throws UnsupportedOperationException if LLM is not configured
     */
    public String chat(String systemPrompt, String userPrompt) {
        throw new UnsupportedOperationException("LLM not configured");
    }
}

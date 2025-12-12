package com.example.reading.service.llm;

import org.springframework.stereotype.Service;

/**
 * Wraps calls to the external LLM API.
 */
@Service
public class LlmClientService {

    /**
     * Calls the LLM API with the given system and user messages.
     *
     * @param systemMessage the system prompt
     * @param userMessage the user message
     * @return the raw string response from the LLM
     */
    public String call(String systemMessage, String userMessage) {
        return null;
    }
}


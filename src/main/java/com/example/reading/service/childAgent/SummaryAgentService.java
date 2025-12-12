package com.example.reading.service.childAgent;

import com.example.reading.dto.AgentResponse;
import com.example.reading.dto.LearningRequest;
import com.example.reading.service.llm.LlmClientService;
import org.springframework.stereotype.Service;

/**
 * Implements ChildAgentService for generating summaries using LLM.
 */
@Service
public class SummaryAgentService implements ChildAgentService {

    private final LlmClientService llmClientService;

    public SummaryAgentService(LlmClientService llmClientService) {
        this.llmClientService = llmClientService;
    }

    @Override
    public AgentResponse generateResponse(LearningRequest request) {
        return null;
    }
}


package com.example.reading.service.childAgent;

import com.example.reading.dto.AgentResponse;
import com.example.reading.dto.LearningRequest;
import com.example.reading.service.llm.LlmClientService;
import org.springframework.stereotype.Service;

/**
 * Implements ChildAgentService for generating quizzes using LLM.
 */
@Service
public class QuizAgentService implements ChildAgentService {

    private final LlmClientService llmClientService;

    public QuizAgentService(LlmClientService llmClientService) {
        this.llmClientService = llmClientService;
    }

    @Override
    public AgentResponse generateResponse(LearningRequest request) {
        return null;
    }
}


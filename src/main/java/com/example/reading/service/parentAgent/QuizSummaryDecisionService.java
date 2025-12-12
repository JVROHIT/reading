package com.example.reading.service.parentAgent;

import com.example.reading.dto.LearningRequest;
import com.example.reading.dto.MasterDecisionResponse;
import com.example.reading.service.llm.LlmClientService;
import org.springframework.stereotype.Service;

/**
 * Implements OrchestratorAgentService using LLM to decide between SUMMARY and QUIZ.
 */
@Service
public class QuizSummaryDecisionService implements OrchestratorAgentService {

    private final LlmClientService llmClientService;

    public QuizSummaryDecisionService(LlmClientService llmClientService) {
        this.llmClientService = llmClientService;
    }

    @Override
    public MasterDecisionResponse generateDecision(LearningRequest request) {
        return null;
    }
}


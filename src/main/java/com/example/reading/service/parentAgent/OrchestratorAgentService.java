package com.example.reading.service.parentAgent;

import com.example.reading.dto.LearningRequest;
import com.example.reading.dto.MasterDecisionResponse;

/**
 * Interface for orchestrator decision generation.
 */
public interface OrchestratorAgentService {

    /**
     * Generates a decision on whether to produce a SUMMARY or QUIZ.
     *
     * @param request the learning request containing instruction and text
     * @return the decision response with action type and explanation
     */
    MasterDecisionResponse generateDecision(LearningRequest request);
}


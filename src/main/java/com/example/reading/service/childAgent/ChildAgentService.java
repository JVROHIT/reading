package com.example.reading.service.childAgent;

import com.example.reading.dto.AgentResponse;
import com.example.reading.dto.LearningRequest;

/**
 * Interface for child agent response generation.
 */
public interface ChildAgentService {

    /**
     * Generates a response based on the learning request.
     *
     * @param request the learning request containing instruction and text
     * @return the agent response with action, content, and explanation
     */
    AgentResponse generateResponse(LearningRequest request);
}


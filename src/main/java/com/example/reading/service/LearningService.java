package com.example.reading.service;

import com.example.reading.dto.LearningRequest;
import com.example.reading.dto.LearningResponse;
import org.springframework.stereotype.Service;

/**
 * Validates requests and delegates to OrchestratorService.
 */
@Service
public class LearningService {

    private final OrchestratorService orchestratorService;

    public LearningService(OrchestratorService orchestratorService) {
        this.orchestratorService = orchestratorService;
    }

    /**
     * Processes the learning request after validation.
     *
     * @param request the learning request
     * @return the learning response
     */
    public LearningResponse process(LearningRequest request) {
        return orchestratorService.orchestrate(request);
    }
}

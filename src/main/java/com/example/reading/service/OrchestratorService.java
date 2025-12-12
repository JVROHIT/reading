package com.example.reading.service;

import com.example.reading.dto.LearningRequest;
import com.example.reading.dto.LearningResponse;
import com.example.reading.service.childAgent.QuizAgentService;
import com.example.reading.service.childAgent.SummaryAgentService;
import com.example.reading.service.parentAgent.OrchestratorAgentService;
import org.springframework.stereotype.Service;

/**
 * Coordinates decision-making and delegates to appropriate agent services.
 */
@Service
public class OrchestratorService {

    private final OrchestratorAgentService orchestratorAgentService;
    private final SummaryAgentService summaryAgentService;
    private final QuizAgentService quizAgentService;

    public OrchestratorService(OrchestratorAgentService orchestratorAgentService,
                               SummaryAgentService summaryAgentService,
                               QuizAgentService quizAgentService) {
        this.orchestratorAgentService = orchestratorAgentService;
        this.summaryAgentService = summaryAgentService;
        this.quizAgentService = quizAgentService;
    }

    /**
     * Orchestrates the learning request by deciding action and calling appropriate agent.
     *
     * @param request the learning request
     * @return the learning response
     */
    public LearningResponse orchestrate(LearningRequest request) {
        return null;
    }
}


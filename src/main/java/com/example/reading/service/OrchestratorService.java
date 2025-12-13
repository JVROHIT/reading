package com.example.reading.service;

import com.example.reading.dto.ActionType;
import com.example.reading.dto.AgentResponse;
import com.example.reading.dto.LearningRequest;
import com.example.reading.dto.LearningResponse;
import com.example.reading.dto.MasterDecisionResponse;
import com.example.reading.dto.QuizQuestionDto;
import com.example.reading.service.childAgent.QuizAgentService;
import com.example.reading.service.childAgent.SummaryAgentService;
import com.example.reading.service.parentAgent.OrchestratorAgentService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Coordinates decision-making and delegates to appropriate agent services.
 */
@Service
public class OrchestratorService {

    private final OrchestratorAgentService orchestratorAgentService;
    private final SummaryAgentService summaryAgentService;
    private final QuizAgentService quizAgentService;
    private final ObjectMapper objectMapper;

    public OrchestratorService(OrchestratorAgentService orchestratorAgentService,
                               SummaryAgentService summaryAgentService,
                               QuizAgentService quizAgentService,
                               ObjectMapper objectMapper) {
        this.orchestratorAgentService = orchestratorAgentService;
        this.summaryAgentService = summaryAgentService;
        this.quizAgentService = quizAgentService;
        this.objectMapper = objectMapper;
    }

    /**
     * Orchestrates the learning request by deciding action and calling appropriate agent.
     *
     * @param request the learning request
     * @return the learning response
     */
    public LearningResponse orchestrate(LearningRequest request) {
        MasterDecisionResponse decision = orchestratorAgentService.generateDecision(request);
        ActionType action = decision.getActionToTake();

        if (action == ActionType.QUIZ) {
            AgentResponse agentResponse = quizAgentService.generateResponse(request);
            
            // If quiz generation failed (content is null), fallback to summary
            if (agentResponse.getContent() == null) {
                return fallbackToSummary(request, "Quiz generation failed, falling back to summary");
            }
            
            List<QuizQuestionDto> quiz = parseQuizContent(agentResponse.getContent());
            
            // If parsing failed (empty quiz), fallback to summary
            if (quiz.isEmpty()) {
                return fallbackToSummary(request, "Quiz parsing failed, falling back to summary");
            }
            
            return LearningResponse.builder()
                    .action(ActionType.QUIZ)
                    .quiz(quiz)
                    .explanation(decision.getExplanation())
                    .build();
        } else {
            AgentResponse agentResponse = summaryAgentService.generateResponse(request);
            return LearningResponse.builder()
                    .action(ActionType.SUMMARY)
                    .summary(agentResponse.getContent())
                    .explanation(decision.getExplanation())
                    .build();
        }
    }

    private LearningResponse fallbackToSummary(LearningRequest request, String reason) {
        AgentResponse summaryResponse = summaryAgentService.generateResponse(request);
        return LearningResponse.builder()
                .action(ActionType.SUMMARY)
                .summary(summaryResponse.getContent())
                .explanation(reason)
                .build();
    }

    private List<QuizQuestionDto> parseQuizContent(String content) {
        try {
            return objectMapper.readValue(content, new TypeReference<List<QuizQuestionDto>>() {});
        } catch (JsonProcessingException e) {
            return List.of();
        }
    }
}

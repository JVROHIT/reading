package com.example.reading.service.parentAgent;

import com.example.reading.dto.ActionType;
import com.example.reading.dto.DecisionLlmResponse;
import com.example.reading.dto.LearningRequest;
import com.example.reading.dto.MasterDecisionResponse;
import com.example.reading.service.llm.LlmClientService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Implements OrchestratorAgentService using LLM to decide between SUMMARY and QUIZ.
 */
@Service
public class QuizSummaryDecisionService implements OrchestratorAgentService {

    private static final String DEFAULT_SYSTEM_PROMPT = """
            You are an orchestrator. Decide if the user wants a SUMMARY or QUIZ.
            Respond with JSON: {"decision": "SUMMARY" or "QUIZ", "explanation": "reason"}
            """;

    private final LlmClientService llmClientService;
    private final ObjectMapper objectMapper;
    private final String systemPrompt;

    public QuizSummaryDecisionService(LlmClientService llmClientService, ObjectMapper objectMapper) {
        this.llmClientService = llmClientService;
        this.objectMapper = objectMapper;
        this.systemPrompt = loadPrompt();
    }

    @Override
    public MasterDecisionResponse generateDecision(LearningRequest request) {
        try {
            String userPrompt = "Instruction: " + request.getInstruction();
            String llmResponse = llmClientService.chat(systemPrompt, userPrompt);
            
            DecisionLlmResponse decisionResponse = objectMapper.readValue(llmResponse, DecisionLlmResponse.class);
            ActionType action = parseActionType(decisionResponse.getDecision());
            
            return MasterDecisionResponse.builder()
                    .actionToTake(action)
                    .explanation(decisionResponse.getExplanation())
                    .build();
        } catch (JsonProcessingException e) {
            return MasterDecisionResponse.builder()
                    .actionToTake(ActionType.SUMMARY)
                    .explanation("Fallback to SUMMARY: failed to parse LLM decision response")
                    .build();
        } catch (Exception e) {
            return MasterDecisionResponse.builder()
                    .actionToTake(ActionType.SUMMARY)
                    .explanation("Fallback to SUMMARY: " + e.getMessage())
                    .build();
        }
    }

    private ActionType parseActionType(String decision) {
        if (decision == null) {
            return ActionType.SUMMARY;
        }
        return "QUIZ".equalsIgnoreCase(decision.trim()) ? ActionType.QUIZ : ActionType.SUMMARY;
    }

    private String loadPrompt() {
        try {
            ClassPathResource resource = new ClassPathResource("prompts/orchestrator_prompt.md");
            return resource.getContentAsString(StandardCharsets.UTF_8);
        } catch (IOException e) {
            return DEFAULT_SYSTEM_PROMPT;
        }
    }
}

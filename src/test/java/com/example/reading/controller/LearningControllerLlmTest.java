package com.example.reading.controller;

import com.example.reading.service.llm.LlmClientService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests for LearningController with mocked LLM responses.
 */
@SpringBootTest
@AutoConfigureMockMvc // disables security filters in MockMvc
class LearningControllerLlmTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LlmClientService llmClientService;

    @Test
    @WithMockUser
    void shouldReturnQuizWhenLlmDecisionIsQuizAndQuizLlmReturnsValidJson() throws Exception {
        // Mock orchestrator LLM response
        String orchestratorResponse = """
                {"decision": "QUIZ", "explanation": "User wants to test knowledge"}
                """;
        
        // Mock quiz LLM response with 3 questions
        String quizResponse = """
                {
                    "questions": [
                        {"type": "MCQ", "question": "What is X?", "options": ["A", "B", "C", "D"], "answer": "A"},
                        {"type": "MCQ", "question": "What is Y?", "options": ["A", "B", "C", "D"], "answer": "B"},
                        {"type": "SHORT_ANSWER", "question": "Explain Z", "options": [], "answer": "Z is..."}
                    ]
                }
                """;

        // First call is for orchestrator, subsequent calls are for quiz agent
        when(llmClientService.chat(anyString(), contains("Instruction:")))
                .thenReturn(orchestratorResponse)
                .thenReturn(quizResponse);

        String requestBody = """
                {
                    "instruction": "create a quiz",
                    "text": "Some content to quiz on"
                }
                """;

        mockMvc.perform(post("/api/learning")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.action").value("QUIZ"))
                .andExpect(jsonPath("$.quiz", hasSize(3)))
                .andExpect(jsonPath("$.quiz[0].type").value("MCQ"))
                .andExpect(jsonPath("$.quiz[1].type").value("MCQ"))
                .andExpect(jsonPath("$.quiz[2].type").value("SHORT_ANSWER"));
    }

    @Test
    @WithMockUser
    void shouldReturnSummaryWhenLlmDecisionIsSummary() throws Exception {
        // Mock orchestrator LLM response
        String orchestratorResponse = """
                {"decision": "SUMMARY", "explanation": "User wants a summary"}
                """;
        
        // Mock summary LLM response
        String summaryResponse = "hello";

        when(llmClientService.chat(anyString(), contains("Instruction:")))
                .thenReturn(orchestratorResponse)
                .thenReturn(summaryResponse);

        String requestBody = """
                {
                    "instruction": "summarise this",
                    "text": "Some content to summarise"
                }
                """;

        mockMvc.perform(post("/api/learning")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.action").value("SUMMARY"))
                .andExpect(jsonPath("$.summary").value("hello"));
    }

    @Test
    @WithMockUser
    void shouldFallbackToSummaryWhenQuizLlmReturnsInvalidJson() throws Exception {
        // Mock orchestrator LLM response - decides QUIZ
        String orchestratorResponse = """
                {"decision": "QUIZ", "explanation": "User wants a quiz"}
                """;
        
        // Mock quiz LLM response - invalid JSON
        String invalidQuizResponse = "This is not valid JSON at all";
        
        // Mock summary LLM response for fallback
        String summaryResponse = "Fallback summary content";

        when(llmClientService.chat(anyString(), contains("Instruction:")))
                .thenReturn(orchestratorResponse)
                .thenReturn(invalidQuizResponse)
                .thenReturn(summaryResponse);

        String requestBody = """
                {
                    "instruction": "quiz me",
                    "text": "Some content"
                }
                """;

        mockMvc.perform(post("/api/learning")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.action").value("SUMMARY"));
    }

    @Test
    @WithMockUser
    void shouldFallbackToSummaryWhenOrchestratorLlmReturnsInvalidJson() throws Exception {
        // Mock orchestrator LLM response - invalid JSON
        String invalidOrchestratorResponse = "not valid json";
        
        // Mock summary LLM response for fallback (default is SUMMARY)
        String summaryResponse = "Summary after orchestrator failure";

        when(llmClientService.chat(anyString(), contains("Instruction:")))
                .thenReturn(invalidOrchestratorResponse)
                .thenReturn(summaryResponse);

        String requestBody = """
                {
                    "instruction": "do something",
                    "text": "Some content"
                }
                """;

        mockMvc.perform(post("/api/learning")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.action").value("SUMMARY"));
    }

    @Test
    @WithMockUser
    void shouldFallbackToSummaryWhenQuizLlmReturnsMissingQuestionsArray() throws Exception {
        // Mock orchestrator LLM response
        String orchestratorResponse = """
                {"decision": "QUIZ", "explanation": "User wants a quiz"}
                """;
        
        // Mock quiz LLM response - valid JSON but missing questions array
        String invalidQuizResponse = """
                {"data": "no questions here"}
                """;
        
        // Mock summary LLM response for fallback
        String summaryResponse = "Fallback summary";

        when(llmClientService.chat(anyString(), contains("Instruction:")))
                .thenReturn(orchestratorResponse)
                .thenReturn(invalidQuizResponse)
                .thenReturn(summaryResponse);

        String requestBody = """
                {
                    "instruction": "quiz me",
                    "text": "Some content"
                }
                """;

        mockMvc.perform(post("/api/learning")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.action").value("SUMMARY"));
    }

    @Test
    void shouldReturn401WhenNoAuthentication() throws Exception {
        String requestBody = """
                {
                    "instruction": "summarise this",
                    "text": "Some text content"
                }
                """;

        mockMvc.perform(post("/api/learning")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isUnauthorized());
    }
}

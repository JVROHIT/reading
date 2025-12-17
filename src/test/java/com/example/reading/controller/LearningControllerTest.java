package com.example.reading.controller;

import com.example.reading.service.llm.LlmChatService;
import org.junit.jupiter.api.BeforeEach;
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
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests for LearningController validation and endpoint behavior.
 */
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false) // disables security filters in MockMvc
class LearningControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LlmChatService llmChatService;

    @BeforeEach
    void setUp() {
        // Default mock responses for orchestrator and agents
        String orchestratorSummaryResponse = """
                {"decision": "SUMMARY", "explanation": "Default to summary"}
                """;
        String summaryResponse = "Default summary content";

        when(llmChatService.chat(anyString(), anyString(), anyString()))
                .thenReturn(orchestratorSummaryResponse)
                .thenReturn(summaryResponse);
    }

    @Test
    @WithMockUser
    void shouldReturn400WhenInstructionIsBlank() throws Exception {
        String requestBody = """
                {
                    "instruction": "",
                    "text": "Some valid text content"
                }
                """;

        mockMvc.perform(post("/api/learning")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void shouldReturn400WhenInstructionIsNull() throws Exception {
        String requestBody = """
                {
                    "text": "Some valid text content"
                }
                """;

        mockMvc.perform(post("/api/learning")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void shouldReturn400WhenTextIsBlank() throws Exception {
        String requestBody = """
                {
                    "instruction": "summarise this",
                    "text": ""
                }
                """;

        mockMvc.perform(post("/api/learning")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void shouldReturn400WhenTextIsNull() throws Exception {
        String requestBody = """
                {
                    "instruction": "summarise this"
                }
                """;

        mockMvc.perform(post("/api/learning")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void shouldReturn400WhenTextExceeds50kCharacters() throws Exception {
        String longText = "a".repeat(50001);
        String requestBody = """
                {
                    "instruction": "summarise this",
                    "text": "%s"
                }
                """.formatted(longText);

        mockMvc.perform(post("/api/learning")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void shouldReturnQuizWithThreeQuestionsForQuizInstruction() throws Exception {
        String orchestratorResponse = """
                {"decision": "QUIZ", "explanation": "User wants a quiz"}
                """;
        String quizResponse = """
                {
                    "questions": [
                        {"type": "MCQ", "question": "Q1?", "options": ["A", "B", "C", "D"], "answer": "A"},
                        {"type": "MCQ", "question": "Q2?", "options": ["A", "B", "C", "D"], "answer": "B"},
                        {"type": "SHORT_ANSWER", "question": "Q3?", "options": [], "answer": "Answer"}
                    ]
                }
                """;

        when(llmChatService.chat(anyString(), contains("Instruction:"), isNull()))
                .thenReturn(orchestratorResponse)
                .thenReturn(quizResponse);

        String requestBody = """
                {
                    "instruction": "create a quiz on this",
                    "text": "Some valid text content to create quiz from"
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
    void shouldReturnSummaryForSummariseInstruction() throws Exception {
        String orchestratorResponse = """
                {"decision": "SUMMARY", "explanation": "User wants a summary"}
                """;
        String summaryResponse = "This is a great summary of the content.";

        when(llmChatService.chat(anyString(), contains("Instruction:"), isNull()))
                .thenReturn(orchestratorResponse)
                .thenReturn(summaryResponse);

        String requestBody = """
                {
                    "instruction": "summarise this",
                    "text": "Some valid text content to summarise"
                }
                """;

        mockMvc.perform(post("/api/learning")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.action").value("SUMMARY"))
                .andExpect(jsonPath("$.summary").value("This is a great summary of the content."));
    }

    @Test
    @WithMockUser
    void shouldReturnQuizForTestKeyword() throws Exception {
        String orchestratorResponse = """
                {"decision": "QUIZ", "explanation": "User wants a test"}
                """;
        String quizResponse = """
                {
                    "questions": [
                        {"type": "MCQ", "question": "Q1?", "options": ["A", "B"], "answer": "A"}
                    ]
                }
                """;

        when(llmChatService.chat(anyString(), contains("Instruction:"), isNull()))              
                .thenReturn(orchestratorResponse)
                .thenReturn(quizResponse);

        String requestBody = """
                {
                    "instruction": "test me on this content",
                    "text": "Some valid text content"
                }
                """;

        mockMvc.perform(post("/api/learning")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.action").value("QUIZ"));
    }

    @Test
    @WithMockUser
    void shouldReturnSummaryForGenericInstruction() throws Exception {
        String orchestratorResponse = """
                {"decision": "SUMMARY", "explanation": "Defaulting to summary"}
                """;
        String summaryResponse = "MCQ summary content";

        when(llmChatService.chat(anyString(), contains("Instruction:"), anyString()))
                .thenReturn(orchestratorResponse)
                .thenReturn(summaryResponse);

        String requestBody = """
                {
                    "instruction": "explain this to me",
                    "text": "Some valid text content"
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
    void shouldReturnQuizForPracticeKeyword() throws Exception {
        String orchestratorResponse = """
                {"decision": "QUIZ", "explanation": "User wants practice"}
                """;
        String quizResponse = """
                {
                    "questions": [
                        {"type": "MCQ", "question": "Q1?", "options": ["A", "B"], "answer": "A"}
                    ]
                }
                """;

        when(llmChatService.chat(anyString(), contains("Instruction:"), isNull()))
                .thenReturn(orchestratorResponse)
                .thenReturn(quizResponse);

        String requestBody = """
                {
                    "instruction": "practice questions please",
                    "text": "Some valid text content"
                }
                """;

        mockMvc.perform(post("/api/learning")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.action").value("QUIZ"));
    }

    @Test
    @WithMockUser
    void shouldReturnSummaryContentFromLlm() throws Exception {
        String orchestratorResponse = """
                {"decision": "SUMMARY", "explanation": "User wants a summary"}
                """;
        String summaryResponse = "LLM generated summary content here";

        when(llmChatService.chat(anyString(), contains("Instruction:"), isNull()))
                .thenReturn(orchestratorResponse)
                .thenReturn(summaryResponse);

        String requestBody = """
                {
                    "instruction": "summarise this",
                    "text": "Some text content"
                }
                """;

        mockMvc.perform(post("/api/learning")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.action").value("SUMMARY"))
                .andExpect(jsonPath("$.summary").value("LLM generated summary content here"));
    }
}

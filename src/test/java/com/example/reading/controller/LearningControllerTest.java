package com.example.reading.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests for LearningController validation and endpoint behavior.
 */
@WebMvcTest(LearningController.class)
class LearningControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
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
    void shouldReturn501WithNonNullResponseForValidRequest() throws Exception {
        String requestBody = """
                {
                    "instruction": "summarise this",
                    "text": "Some valid text content to summarise"
                }
                """;

        mockMvc.perform(post("/api/learning")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isNotImplemented())
                .andExpect(jsonPath("$").exists())
                .andExpect(jsonPath("$.explanation").value("Not implemented yet"));
    }

    @Test
    void shouldAcceptTextExactly50kCharacters() throws Exception {
        String exactText = "a".repeat(50000);
        String requestBody = """
                {
                    "instruction": "summarise this",
                    "text": "%s"
                }
                """.formatted(exactText);

        mockMvc.perform(post("/api/learning")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isNotImplemented());
    }
}


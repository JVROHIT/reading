package com.example.reading.llm;

import java.time.Duration;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Immutable options for configuring an LLM model.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LlmModelOptions {

    @NotNull(message = "Model is required")
    private String model;

    private Double temperature;

    private Duration timeout;

    @NotNull(message = "Max tokens is required")
    private Integer maxTokens;

    private String baseUrl;
}

package com.example.reading.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Raw structured LLM output from the orchestrator prompt before mapping to MasterDecisionResponse.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DecisionLlmResponse {

    private String decision;
    private String explanation;
}

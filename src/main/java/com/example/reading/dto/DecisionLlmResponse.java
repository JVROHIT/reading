package com.example.reading.dto;

/**
 * Raw structured LLM output from the orchestrator prompt before mapping to MasterDecisionResponse.
 */
public class DecisionLlmResponse {

    private String decision;
    private String explanation;
}


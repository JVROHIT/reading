package com.example.reading.dto;

/**
 * Response from the orchestrator containing the decided action and explanation.
 */
public class MasterDecisionResponse {

    private ActionType actionToTake;
    private String explanation;
}


package com.example.reading.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Response from the orchestrator containing the decided action and explanation.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MasterDecisionResponse {

    private ActionType actionToTake;
    private String explanation;
}

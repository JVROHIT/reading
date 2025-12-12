package com.example.reading.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Response DTO containing the action taken, summary or quiz content, and explanation.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LearningResponse {

    private ActionType action;
    private String summary;
    private List<QuizQuestionDto> quiz;
    private String explanation;
}

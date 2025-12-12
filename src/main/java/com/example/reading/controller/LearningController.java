package com.example.reading.controller;

import com.example.reading.dto.LearningRequest;
import com.example.reading.dto.LearningResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller exposing the learning API endpoint.
 */
@RestController
@RequestMapping("/api/learning")
public class LearningController {

    /**
     * Processes a learning request to generate a summary or quiz.
     *
     * @param request the learning request containing instruction and text
     * @return the learning response with HTTP 501 Not Implemented
     */
    @PostMapping
    public ResponseEntity<LearningResponse> processLearningRequest(@Valid @RequestBody LearningRequest request) {
        LearningResponse response = LearningResponse.builder()
                .explanation("Not implemented yet")
                .build();
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(response);
    }
}

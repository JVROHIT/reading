package com.example.reading.controller;

import com.example.reading.dto.LearningRequest;
import com.example.reading.dto.LearningResponse;
import com.example.reading.service.LearningService;
import jakarta.validation.Valid;
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

    private final LearningService learningService;

    public LearningController(LearningService learningService) {
        this.learningService = learningService;
    }

    /**
     * Processes a learning request to generate a summary or quiz.
     *
     * @param request the learning request containing instruction and text
     * @return the learning response with HTTP 200 OK
     */
    @PostMapping
    public ResponseEntity<LearningResponse> processLearningRequest(@Valid @RequestBody LearningRequest request) {
        LearningResponse response = learningService.process(request);
        return ResponseEntity.ok(response);
    }
}

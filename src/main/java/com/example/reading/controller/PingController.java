package com.example.reading.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Simple controller for testing protected endpoints.
 */
@RestController
@RequestMapping("/api")
public class PingController {

    @GetMapping("/ping")
    public ResponseEntity<Map<String, Boolean>> ping() {
        return ResponseEntity.ok(Map.of("ok", true));
    }
}


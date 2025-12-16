package com.example.reading.auth.controller;

import com.example.reading.auth.dto.AuthResponse;
import com.example.reading.auth.dto.LoginRequest;
import com.example.reading.auth.dto.RegisterRequest;
import com.example.reading.auth.jwt.JwtService;
import com.example.reading.auth.service.AuthService;
import com.example.reading.user.User;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for authentication endpoints (register and login).
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;

    public AuthController(AuthService authService, JwtService jwtService) {
        this.authService = authService;
        this.jwtService = jwtService;
    }

    /**
     * Registers a new user and returns a JWT token.
     *
     * @param request the registration request containing email and password
     * @return AuthResponse with JWT token
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        User user = authService.register(request.getEmail(), request.getPassword());
        String token = jwtService.generateToken(user.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(AuthResponse.builder().token(token).build());
    }

    /**
     * Authenticates a user and returns a JWT token.
     *
     * @param request the login request containing email and password
     * @return AuthResponse with JWT token
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        User user = authService.login(request.getEmail(), request.getPassword());
        String token = jwtService.generateToken(user.getId());
        return ResponseEntity.ok(AuthResponse.builder().token(token).build());
    }
}


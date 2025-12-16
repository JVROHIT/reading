package com.example.reading.auth;

import com.example.reading.user.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for the full authentication flow.
 * Requires MongoDB running locally.
 * Start Mongo: docker run -d --name reading-mongo -p 27017:27017 mongo:7
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local-mongo")
class AuthFlowTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void fullAuthFlowShouldWork() throws Exception {
        String email = "flowtest@example.com";
        String password = "securePassword123";

        // 1. Register a new user
        String registerRequest = """
                {
                    "email": "%s",
                    "password": "%s"
                }
                """.formatted(email, password);

        MvcResult registerResult = mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andReturn();

        String registerToken = extractToken(registerResult);
        assertThat(registerToken).isNotBlank();

        // 2. Call /api/ping with the registration token → 200
        mockMvc.perform(get("/api/ping")
                        .header("Authorization", "Bearer " + registerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true));

        // 3. Login with the same credentials → token returned
        String loginRequest = """
                {
                    "email": "%s",
                    "password": "%s"
                }
                """.formatted(email, password);

        MvcResult loginResult = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andReturn();

        String loginToken = extractToken(loginResult);
        assertThat(loginToken).isNotBlank();

        // 4. Verify login token also works for /api/ping
        mockMvc.perform(get("/api/ping")
                        .header("Authorization", "Bearer " + loginToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true));
    }

    @Test
    void loginWithWrongPasswordShouldReturn401() throws Exception {
        String email = "wrongpass@example.com";
        String correctPassword = "correctPassword";
        String wrongPassword = "wrongPassword";

        // Register first
        String registerRequest = """
                {
                    "email": "%s",
                    "password": "%s"
                }
                """.formatted(email, correctPassword);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerRequest))
                .andExpect(status().isCreated());

        // Try to login with wrong password
        String loginRequest = """
                {
                    "email": "%s",
                    "password": "%s"
                }
                """.formatted(email, wrongPassword);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequest))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("INVALID_CREDENTIALS"));
    }

    @Test
    void loginWithUnknownEmailShouldReturn401() throws Exception {
        String loginRequest = """
                {
                    "email": "unknown@example.com",
                    "password": "anyPassword"
                }
                """;

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequest))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("INVALID_CREDENTIALS"));
    }

    @Test
    void registerWithDuplicateEmailShouldReturn400() throws Exception {
        String email = "duplicate@example.com";
        String password = "password123";

        String registerRequest = """
                {
                    "email": "%s",
                    "password": "%s"
                }
                """.formatted(email, password);

        // First registration should succeed
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerRequest))
                .andExpect(status().isCreated());

        // Second registration with same email should fail
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("EMAIL_ALREADY_EXISTS"));
    }

    @Test
    void accessProtectedEndpointWithoutTokenShouldReturn401() throws Exception {
        mockMvc.perform(get("/api/ping"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("UNAUTHORIZED"));
    }

    @Test
    void accessProtectedEndpointWithInvalidTokenShouldReturn401() throws Exception {
        mockMvc.perform(get("/api/ping")
                        .header("Authorization", "Bearer invalid.token.here"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("UNAUTHORIZED"));
    }

    private String extractToken(MvcResult result) throws Exception {
        String responseBody = result.getResponse().getContentAsString();
        JsonNode jsonNode = objectMapper.readTree(responseBody);
        return jsonNode.get("token").asText();
    }
}


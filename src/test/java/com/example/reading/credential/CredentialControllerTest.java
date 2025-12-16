package com.example.reading.credential;

import com.example.reading.auth.jwt.JwtService;
import com.example.reading.user.User;
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

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for CredentialController.
 * Requires MongoDB running locally.
 */
@SpringBootTest(properties = {
        "app.encryption.key-base64=dGhpcy1pcy1hLTMyLWJ5dGUta2V5LWZvci10ZXN0ISE="
})
@AutoConfigureMockMvc
@ActiveProfiles("local-mongo")
class CredentialControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ApiCredentialRepository credentialRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    private String user1Token;
    private String user2Token;
    private String user1Id;
    private String user2Id;

    @BeforeEach
    void setUp() {
        credentialRepository.deleteAll();
        userRepository.deleteAll();

        // Create two users for isolation testing
        User user1 = userRepository.save(User.builder()
                .email("user1@example.com")
                .passwordHash("hash1")
                .build());
        user1Id = user1.getId();
        user1Token = jwtService.generateToken(user1Id);

        User user2 = userRepository.save(User.builder()
                .email("user2@example.com")
                .passwordHash("hash2")
                .build());
        user2Id = user2.getId();
        user2Token = jwtService.generateToken(user2Id);
    }

    // ==================== Happy Path Tests ====================

    @Test
    void getCredentialsShouldReturnEmptyListInitially() throws Exception {
        mockMvc.perform(get("/api/credentials")
                        .header("Authorization", "Bearer " + user1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void createCredentialShouldReturn201WithCredential() throws Exception {
        String requestBody = """
                {
                    "provider": "OPENAI",
                    "apiKey": "sk-test-api-key-12345"
                }
                """;

        mockMvc.perform(post("/api/credentials")
                        .header("Authorization", "Bearer " + user1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.provider").value("OPENAI"))
                .andExpect(jsonPath("$.createdAt").isNotEmpty())
                .andExpect(jsonPath("$.updatedAt").isNotEmpty());
    }

    @Test
    void getCredentialsShouldReturnCreatedCredentials() throws Exception {
        // Create a credential first
        String requestBody = """
                {
                    "provider": "ANTHROPIC",
                    "apiKey": "sk-ant-test-key"
                }
                """;

        mockMvc.perform(post("/api/credentials")
                        .header("Authorization", "Bearer " + user1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated());

        // Get credentials
        mockMvc.perform(get("/api/credentials")
                        .header("Authorization", "Bearer " + user1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].provider").value("ANTHROPIC"));
    }

    @Test
    void deleteCredentialShouldReturn204() throws Exception {
        // Create a credential first
        String requestBody = """
                {
                    "provider": "OPENAI",
                    "apiKey": "sk-to-delete"
                }
                """;

        MvcResult createResult = mockMvc.perform(post("/api/credentials")
                        .header("Authorization", "Bearer " + user1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andReturn();

        String credentialId = extractId(createResult);

        // Delete the credential
        mockMvc.perform(delete("/api/credentials/" + credentialId)
                        .header("Authorization", "Bearer " + user1Token))
                .andExpect(status().isNoContent());

        // Verify it's gone
        mockMvc.perform(get("/api/credentials")
                        .header("Authorization", "Bearer " + user1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    // ==================== Unauthorized Tests ====================

    @Test
    void getCredentialsWithoutTokenShouldReturn401() throws Exception {
        mockMvc.perform(get("/api/credentials"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createCredentialWithoutTokenShouldReturn401() throws Exception {
        String requestBody = """
                {
                    "provider": "OPENAI",
                    "apiKey": "sk-test"
                }
                """;

        mockMvc.perform(post("/api/credentials")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deleteCredentialWithoutTokenShouldReturn401() throws Exception {
        mockMvc.perform(delete("/api/credentials/some-id"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getCredentialsWithInvalidTokenShouldReturn401() throws Exception {
        mockMvc.perform(get("/api/credentials")
                        .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized());
    }

    // ==================== Isolation Tests ====================

    @Test
    void userShouldOnlySeeOwnCredentials() throws Exception {
        // User 1 creates a credential
        String user1Request = """
                {
                    "provider": "OPENAI",
                    "apiKey": "sk-user1-key"
                }
                """;

        mockMvc.perform(post("/api/credentials")
                        .header("Authorization", "Bearer " + user1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(user1Request))
                .andExpect(status().isCreated());

        // User 2 creates a credential
        String user2Request = """
                {
                    "provider": "ANTHROPIC",
                    "apiKey": "sk-user2-key"
                }
                """;

        mockMvc.perform(post("/api/credentials")
                        .header("Authorization", "Bearer " + user2Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(user2Request))
                .andExpect(status().isCreated());

        // User 1 should only see their own credential
        mockMvc.perform(get("/api/credentials")
                        .header("Authorization", "Bearer " + user1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].provider").value("OPENAI"));

        // User 2 should only see their own credential
        mockMvc.perform(get("/api/credentials")
                        .header("Authorization", "Bearer " + user2Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].provider").value("ANTHROPIC"));
    }

    @Test
    void userCannotDeleteOtherUsersCredential() throws Exception {
        // User 1 creates a credential
        String requestBody = """
                {
                    "provider": "OPENAI",
                    "apiKey": "sk-user1-key"
                }
                """;

        MvcResult createResult = mockMvc.perform(post("/api/credentials")
                        .header("Authorization", "Bearer " + user1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andReturn();

        String credentialId = extractId(createResult);

        // User 2 tries to delete User 1's credential
        mockMvc.perform(delete("/api/credentials/" + credentialId)
                        .header("Authorization", "Bearer " + user2Token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("CREDENTIAL_NOT_FOUND"));

        // Verify credential still exists for User 1
        mockMvc.perform(get("/api/credentials")
                        .header("Authorization", "Bearer " + user1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    // ==================== Validation Tests ====================

    @Test
    void createCredentialWithMissingProviderShouldReturn400() throws Exception {
        String requestBody = """
                {
                    "apiKey": "sk-test"
                }
                """;

        mockMvc.perform(post("/api/credentials")
                        .header("Authorization", "Bearer " + user1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createCredentialWithMissingApiKeyShouldReturn400() throws Exception {
        String requestBody = """
                {
                    "provider": "OPENAI"
                }
                """;

        mockMvc.perform(post("/api/credentials")
                        .header("Authorization", "Bearer " + user1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createCredentialWithBlankApiKeyShouldReturn400() throws Exception {
        String requestBody = """
                {
                    "provider": "OPENAI",
                    "apiKey": "   "
                }
                """;

        mockMvc.perform(post("/api/credentials")
                        .header("Authorization", "Bearer " + user1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createCredentialWithInvalidProviderShouldReturn400() throws Exception {
        String requestBody = """
                {
                    "provider": "INVALID_PROVIDER",
                    "apiKey": "sk-test"
                }
                """;

        mockMvc.perform(post("/api/credentials")
                        .header("Authorization", "Bearer " + user1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    // ==================== Not Found Tests ====================

    @Test
    void deleteNonExistentCredentialShouldReturn404() throws Exception {
        mockMvc.perform(delete("/api/credentials/non-existent-id")
                        .header("Authorization", "Bearer " + user1Token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("CREDENTIAL_NOT_FOUND"));
    }

    // ==================== Helper Methods ====================

    private String extractId(MvcResult result) throws Exception {
        String responseBody = result.getResponse().getContentAsString();
        JsonNode jsonNode = objectMapper.readTree(responseBody);
        return jsonNode.get("id").asText();
    }
}


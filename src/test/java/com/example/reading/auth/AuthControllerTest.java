package com.example.reading.auth;

import com.example.reading.auth.exception.EmailAlreadyUsedException;
import com.example.reading.auth.exception.InvalidCredentialsException;
import com.example.reading.auth.service.AuthService;
import com.example.reading.user.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests for AuthController endpoints.
 */
@SpringBootTest(properties = {
        "security.jwt.secret=aGltc2FpbHBvd2VyY29sb3JkZWFkdHJhaWxzaG9yZWluY2hwb3JjaGVpdGhlcndvb2Q=",
        "security.jwt.expiration-seconds=86400"
})
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @Test
    void registerShouldReturnJwtToken() throws Exception {
        User user = User.builder()
                .id("user-123")
                .email("test@example.com")
                .passwordHash("hashedPassword")
                .build();

        when(authService.register(eq("test@example.com"), eq("password123")))
                .thenReturn(user);

        String requestBody = """
                {
                    "email": "test@example.com",
                    "password": "password123"
                }
                """;

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    void registerWithDuplicateEmailShouldReturn400() throws Exception {
        when(authService.register(eq("existing@example.com"), anyString()))
                .thenThrow(new EmailAlreadyUsedException("existing@example.com"));

        String requestBody = """
                {
                    "email": "existing@example.com",
                    "password": "password123"
                }
                """;

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("EMAIL_ALREADY_EXISTS"));
    }

    @Test
    void registerWithInvalidEmailShouldReturn400() throws Exception {
        String requestBody = """
                {
                    "email": "not-an-email",
                    "password": "password123"
                }
                """;

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registerWithShortPasswordShouldReturn400() throws Exception {
        String requestBody = """
                {
                    "email": "test@example.com",
                    "password": "12345"
                }
                """;

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void loginShouldReturnJwtToken() throws Exception {
        User user = User.builder()
                .id("user-456")
                .email("login@example.com")
                .passwordHash("hashedPassword")
                .build();

        when(authService.login(eq("login@example.com"), eq("correctPassword")))
                .thenReturn(user);

        String requestBody = """
                {
                    "email": "login@example.com",
                    "password": "correctPassword"
                }
                """;

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    void loginWithWrongPasswordShouldReturn401() throws Exception {
        when(authService.login(eq("user@example.com"), eq("wrongPassword")))
                .thenThrow(new InvalidCredentialsException());

        String requestBody = """
                {
                    "email": "user@example.com",
                    "password": "wrongPassword"
                }
                """;

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("INVALID_CREDENTIALS"));
    }

    @Test
    void loginWithUnknownEmailShouldReturn401() throws Exception {
        when(authService.login(eq("unknown@example.com"), anyString()))
                .thenThrow(new InvalidCredentialsException());

        String requestBody = """
                {
                    "email": "unknown@example.com",
                    "password": "anyPassword"
                }
                """;

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("INVALID_CREDENTIALS"));
    }

    @Test
    void loginWithMissingEmailShouldReturn400() throws Exception {
        String requestBody = """
                {
                    "password": "password123"
                }
                """;

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void loginWithMissingPasswordShouldReturn400() throws Exception {
        String requestBody = """
                {
                    "email": "test@example.com"
                }
                """;

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }
}


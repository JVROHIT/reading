package com.example.reading.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import com.example.reading.auth.jwt.JwtService;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests for PingController and security configuration.
 */
@SpringBootTest(properties = {
    "security.jwt.secret=aGltc2FpbHBvd2VyY29sb3JkZWFkdHJhaWxzaG9yZWluY2hwb3JjaGVpdGhlcndvb2Q=",
    "security.jwt.expiration-seconds=8400"
})
@AutoConfigureMockMvc
class PingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Test
    void pingWithoutJwtShouldReturn401() throws Exception {
        mockMvc.perform(get("/api/ping"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("UNAUTHORIZED"));
    }

    @Test
    void pingWithValidJwtShouldReturn200() throws Exception {
        String token = jwtService.generateToken("test-user-id");

        mockMvc.perform(get("/api/ping")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true));
    }

    @Test
    void pingWithInvalidJwtShouldReturn401() throws Exception {
        mockMvc.perform(get("/api/ping")
                        .header("Authorization", "Bearer invalid.token.here"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("UNAUTHORIZED"));
    }

    @Test
    void pingWithMalformedAuthHeaderShouldReturn401() throws Exception {
        mockMvc.perform(get("/api/ping")
                        .header("Authorization", "NotBearer token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("UNAUTHORIZED"));
    }
}


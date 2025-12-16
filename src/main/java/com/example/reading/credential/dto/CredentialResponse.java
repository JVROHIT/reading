package com.example.reading.credential.dto;

import com.example.reading.credential.Provider;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * Response DTO for API credential (excludes the encrypted key).
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CredentialResponse {

    private String id;
    private Provider provider;
    private Instant createdAt;
    private Instant updatedAt;
}


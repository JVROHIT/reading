package com.example.reading.credential.dto;

import com.example.reading.credential.Provider;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Request DTO for creating a new API credential.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateCredentialRequest {

    @NotNull(message = "Provider is required")
    private Provider provider;

    @NotBlank(message = "API key is required")
    private String apiKey;
}


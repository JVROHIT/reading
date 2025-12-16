package com.example.reading.credential;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Objects;

/**
 * MongoDB document representing a user's API credential for an LLM provider.
 * The API key is stored encrypted - never in plaintext.
 */
@Document(collection = "api_credentials")
@CompoundIndex(name = "user_provider_idx", def = "{'userId': 1, 'provider': 1}")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiCredential {

    @Id
    private String id;

    private String userId;

    private Provider provider;

    /**
     * The API key encrypted using application-level encryption.
     * NEVER store plaintext API keys.
     */
    private String encryptedApiKey;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ApiCredential that = (ApiCredential) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}


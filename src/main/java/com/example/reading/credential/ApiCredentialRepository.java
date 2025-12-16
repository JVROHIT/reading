package com.example.reading.credential;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for ApiCredential document CRUD operations.
 */
public interface ApiCredentialRepository extends MongoRepository<ApiCredential, String> {

    /**
     * Finds all API credentials for a given user.
     *
     * @param userId the user ID
     * @return list of API credentials belonging to the user
     */
    List<ApiCredential> findByUserId(String userId);

    /**
     * Finds an API credential by ID and user ID.
     * Ensures users can only access their own credentials.
     *
     * @param id the credential ID
     * @param userId the user ID
     * @return the credential if found and owned by the user
     */
    Optional<ApiCredential> findByIdAndUserId(String id, String userId);

    /**
     * Finds an API credential by user ID and provider.
     *
     * @param userId the user ID
     * @param provider the LLM provider
     * @return the credential if found
     */
    Optional<ApiCredential> findByUserIdAndProvider(String userId, Provider provider);

    /**
     * Deletes an API credential by ID and user ID.
     * Ensures users can only delete their own credentials.
     *
     * @param id the credential ID
     * @param userId the user ID
     */
    void deleteByIdAndUserId(String id, String userId);

    /**
     * Checks if a credential exists for a user and provider.
     *
     * @param userId the user ID
     * @param provider the LLM provider
     * @return true if a credential exists
     */
    boolean existsByUserIdAndProvider(String userId, Provider provider);
}


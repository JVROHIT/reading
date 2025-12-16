package com.example.reading.auth;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Utility to extract the current authenticated user's ID from the security context.
 */
@Component
public class CurrentUserService {

    /**
     * Gets the current authenticated user's ID.
     *
     * @return the user ID from the JWT token
     * @throws IllegalStateException if no user is authenticated
     */
    public String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("No authenticated user found");
        }
        return (String) authentication.getPrincipal();
    }
}


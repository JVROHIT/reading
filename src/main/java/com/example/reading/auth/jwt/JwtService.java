package com.example.reading.auth.jwt;

import com.example.reading.auth.exception.InvalidJwtException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.util.Date;

/**
 * Service for JWT token generation and validation.
 */
@Service
public class JwtService {

    private final SecretKey secretKey;
    private final long expirationSeconds;
    private final Clock clock;

    @Autowired
    public JwtService(JwtProperties jwtProperties) {
        this(jwtProperties, Clock.systemUTC());
    }

    /**
     * Constructor with clock injection for testing.
     */
    JwtService(JwtProperties jwtProperties, Clock clock) {
        this.secretKey = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
        this.expirationSeconds = jwtProperties.getExpirationSeconds();
        this.clock = clock;
    }

    /**
     * Generates a signed JWT token for the given user ID.
     *
     * @param userId the user ID to include as subject
     * @return the signed JWT token string
     */
    public String generateToken(String userId) {
        Instant now = clock.instant();
        Instant expiry = now.plusSeconds(expirationSeconds);

        return Jwts.builder()
                .subject(userId)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(secretKey, Jwts.SIG.HS256)
                .compact();
    }

    /**
     * Validates a JWT token's signature and expiry.
     *
     * @param token the JWT token to validate
     * @return true if valid, false otherwise
     */
    public boolean isValid(String token) {
        try {
            parseToken(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Extracts the user ID (subject) from a JWT token.
     *
     * @param token the JWT token
     * @return the user ID
     * @throws InvalidJwtException if token is invalid or expired
     */
    public String extractUserId(String token) {
        try {
            Claims claims = parseToken(token);
            return claims.getSubject();
        } catch (ExpiredJwtException e) {
            throw new InvalidJwtException("Token has expired", e);
        } catch (JwtException | IllegalArgumentException e) {
            throw new InvalidJwtException("Invalid token", e);
        }
    }

    private Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .clock(() -> Date.from(clock.instant()))
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}


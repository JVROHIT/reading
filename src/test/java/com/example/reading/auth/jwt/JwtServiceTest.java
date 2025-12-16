package com.example.reading.auth.jwt;

import com.example.reading.auth.exception.InvalidJwtException;
import com.example.reading.auth.jwt.JwtProperties;
import com.example.reading.auth.jwt.JwtService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for JwtService.
 */
class JwtServiceTest {

    private static final String SECRET = "myTestSecretKeyForJwtSigningMustBeAtLeast256BitsLongForHS256";
    private static final long EXPIRATION_SECONDS = 3600; // 1 hour

    private JwtProperties jwtProperties;
    private Clock fixedClock;

    @BeforeEach
    void setUp() {
        jwtProperties = new JwtProperties();
        jwtProperties.setSecret(SECRET);
        jwtProperties.setExpirationSeconds(EXPIRATION_SECONDS);

        fixedClock = Clock.fixed(Instant.parse("2024-01-01T12:00:00Z"), ZoneId.of("UTC"));
    }

    @Test
    void generateTokenAndValidateShouldSucceed() {
        JwtService jwtService = new JwtService(jwtProperties, fixedClock);
        String userId = "user-123";

        String token = jwtService.generateToken(userId);

        assertThat(token).isNotNull().isNotEmpty();
        assertThat(jwtService.isValid(token)).isTrue();
    }

    @Test
    void extractUserIdShouldReturnCorrectUserId() {
        JwtService jwtService = new JwtService(jwtProperties, fixedClock);
        String userId = "user-456";

        String token = jwtService.generateToken(userId);
        String extractedUserId = jwtService.extractUserId(token);

        assertThat(extractedUserId).isEqualTo(userId);
    }

    @Test
    void tamperedTokenShouldBeInvalid() {
        JwtService jwtService = new JwtService(jwtProperties, fixedClock);
        String userId = "user-789";

        String token = jwtService.generateToken(userId);
        // Tamper with the token by modifying a character
        String tamperedToken = token.substring(0, token.length() - 5) + "XXXXX";

        assertThat(jwtService.isValid(tamperedToken)).isFalse();
    }

    @Test
    void tamperedTokenShouldThrowOnExtractUserId() {
        JwtService jwtService = new JwtService(jwtProperties, fixedClock);
        String userId = "user-789";

        String token = jwtService.generateToken(userId);
        String tamperedToken = token.substring(0, token.length() - 5) + "XXXXX";

        assertThatThrownBy(() -> jwtService.extractUserId(tamperedToken))
                .isInstanceOf(InvalidJwtException.class)
                .hasMessageContaining("Invalid token");
    }

    @Test
    void expiredTokenShouldBeInvalid() {
        // Create token with very short expiration
        JwtProperties shortExpiryProps = new JwtProperties();
        shortExpiryProps.setSecret(SECRET);
        shortExpiryProps.setExpirationSeconds(1); // 1 second

        Clock creationClock = Clock.fixed(Instant.parse("2024-01-01T12:00:00Z"), ZoneId.of("UTC"));
        JwtService creationService = new JwtService(shortExpiryProps, creationClock);

        String token = creationService.generateToken("user-expired");

        // Validate with a clock 10 seconds in the future (token expired)
        Clock futureClock = Clock.fixed(Instant.parse("2024-01-01T12:00:10Z"), ZoneId.of("UTC"));
        JwtService validationService = new JwtService(shortExpiryProps, futureClock);

        assertThat(validationService.isValid(token)).isFalse();
    }

    @Test
    void expiredTokenShouldThrowOnExtractUserId() {
        JwtProperties shortExpiryProps = new JwtProperties();
        shortExpiryProps.setSecret(SECRET);
        shortExpiryProps.setExpirationSeconds(1);

        Clock creationClock = Clock.fixed(Instant.parse("2024-01-01T12:00:00Z"), ZoneId.of("UTC"));
        JwtService creationService = new JwtService(shortExpiryProps, creationClock);

        String token = creationService.generateToken("user-expired");

        Clock futureClock = Clock.fixed(Instant.parse("2024-01-01T12:00:10Z"), ZoneId.of("UTC"));
        JwtService validationService = new JwtService(shortExpiryProps, futureClock);

        assertThatThrownBy(() -> validationService.extractUserId(token))
                .isInstanceOf(InvalidJwtException.class)
                .hasMessageContaining("expired");
    }

    @Test
    void malformedTokenShouldBeInvalid() {
        JwtService jwtService = new JwtService(jwtProperties, fixedClock);

        assertThat(jwtService.isValid("not.a.valid.jwt")).isFalse();
        assertThat(jwtService.isValid("")).isFalse();
        assertThat(jwtService.isValid("random-string")).isFalse();
    }

    @Test
    void malformedTokenShouldThrowOnExtractUserId() {
        JwtService jwtService = new JwtService(jwtProperties, fixedClock);

        assertThatThrownBy(() -> jwtService.extractUserId("not.a.valid.jwt"))
                .isInstanceOf(InvalidJwtException.class)
                .hasMessageContaining("Invalid token");
    }

    @Test
    void nullTokenShouldBeInvalid() {
        JwtService jwtService = new JwtService(jwtProperties, fixedClock);

        assertThat(jwtService.isValid(null)).isFalse();
    }

    @Test
    void tokenSignedWithDifferentSecretShouldBeInvalid() {
        JwtService jwtService = new JwtService(jwtProperties, fixedClock);

        // Create token with different secret
        JwtProperties differentSecretProps = new JwtProperties();
        differentSecretProps.setSecret("aDifferentSecretKeyThatIsAlsoAtLeast256BitsLongForHS256Algorithm");
        differentSecretProps.setExpirationSeconds(EXPIRATION_SECONDS);
        JwtService differentSecretService = new JwtService(differentSecretProps, fixedClock);

        String token = differentSecretService.generateToken("user-123");

        // Validate with original service (different secret)
        assertThat(jwtService.isValid(token)).isFalse();
    }

    @Test
    void validTokenBeforeExpiryShouldBeValid() {
        JwtProperties props = new JwtProperties();
        props.setSecret(SECRET);
        props.setExpirationSeconds(3600); // 1 hour

        Clock creationClock = Clock.fixed(Instant.parse("2024-01-01T12:00:00Z"), ZoneId.of("UTC"));
        JwtService creationService = new JwtService(props, creationClock);

        String token = creationService.generateToken("user-valid");

        // Validate 30 minutes later (still valid)
        Clock laterClock = Clock.fixed(Instant.parse("2024-01-01T12:30:00Z"), ZoneId.of("UTC"));
        JwtService validationService = new JwtService(props, laterClock);

        assertThat(validationService.isValid(token)).isTrue();
        assertThat(validationService.extractUserId(token)).isEqualTo("user-valid");
    }
}


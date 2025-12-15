package com.example.reading.auth;

import com.example.reading.auth.exception.EmailAlreadyUsedException;
import com.example.reading.auth.exception.InvalidCredentialsException;
import com.example.reading.user.User;
import com.example.reading.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for AuthService using Mockito.
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository, passwordEncoder);
    }

    @Test
    void registerShouldHashPasswordAndSaveUser() {
        String email = "test@example.com";
        String rawPassword = "password123";
        String hashedPassword = "$2a$10$hashedPasswordValue";

        when(userRepository.existsByEmail(email)).thenReturn(false);
        when(passwordEncoder.encode(rawPassword)).thenReturn(hashedPassword);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId("generated-id");
            return user;
        });

        User result = authService.register(email, rawPassword);

        assertThat(result.getEmail()).isEqualTo(email);
        assertThat(result.getPasswordHash()).isEqualTo(hashedPassword);
        assertThat(result.getId()).isNotNull();

        // Verify password was encoded
        verify(passwordEncoder).encode(rawPassword);

        // Verify user was saved with hashed password
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getPasswordHash()).isEqualTo(hashedPassword);
    }

    @Test
    void registerShouldThrowWhenEmailAlreadyExists() {
        String email = "existing@example.com";
        String rawPassword = "password123";

        when(userRepository.existsByEmail(email)).thenReturn(true);

        assertThatThrownBy(() -> authService.register(email, rawPassword))
                .isInstanceOf(EmailAlreadyUsedException.class)
                .hasMessageContaining(email);

        // Verify no user was saved
        verify(userRepository, never()).save(any());
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void loginShouldReturnUserWhenCredentialsAreValid() {
        String email = "user@example.com";
        String rawPassword = "correctPassword";
        String hashedPassword = "$2a$10$hashedPasswordValue";

        User existingUser = User.builder()
                .id("user-id")
                .email(email)
                .passwordHash(hashedPassword)
                .build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches(rawPassword, hashedPassword)).thenReturn(true);

        User result = authService.login(email, rawPassword);

        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo(email);
        assertThat(result.getId()).isEqualTo("user-id");

        verify(passwordEncoder).matches(rawPassword, hashedPassword);
    }

    @Test
    void loginShouldThrowWhenPasswordIsWrong() {
        String email = "user@example.com";
        String wrongPassword = "wrongPassword";
        String hashedPassword = "$2a$10$hashedPasswordValue";

        User existingUser = User.builder()
                .id("user-id")
                .email(email)
                .passwordHash(hashedPassword)
                .build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches(wrongPassword, hashedPassword)).thenReturn(false);

        assertThatThrownBy(() -> authService.login(email, wrongPassword))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Invalid email or password");
    }

    @Test
    void loginShouldThrowWhenEmailNotFound() {
        String email = "unknown@example.com";
        String rawPassword = "password123";

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(email, rawPassword))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Invalid email or password");

        // Verify password matching was never called
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }
}


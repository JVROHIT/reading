package com.example.reading.user;

import com.example.reading.auth.jwt.JwtService;
import com.example.reading.config.MongoAuditingConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Integration tests for UserRepository against a real MongoDB instance.
 * Start Mongo before running tests: docker run -d --name reading-mongo -p 27017:27017 mongo:7
 */
@DataMongoTest
@ActiveProfiles("local-mongo")
@Import(MongoAuditingConfig.class)
class UserRepositoryIT {

    @Autowired
    private UserRepository userRepository;

    @MockBean
    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void savingUserShouldAssignIdAndTimestamps() {
        User user = User.builder()
                .email("test@example.com")
                .passwordHash("hashedPassword123")
                .build();

        User savedUser = userRepository.save(user);

        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getCreatedAt()).isNotNull();
        assertThat(savedUser.getUpdatedAt()).isNotNull();
    }

    @Test
    void findByEmailShouldReturnUser() {
        String email = "find@example.com";
        User user = User.builder()
                .email(email)
                .passwordHash("hashedPassword123")
                .build();
        userRepository.save(user);

        Optional<User> found = userRepository.findByEmail(email);

        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo(email);
    }

    @Test
    void existsByEmailShouldReturnTrue() {
        String email = "exists@example.com";
        User user = User.builder()
                .email(email)
                .passwordHash("hashedPassword123")
                .build();
        userRepository.save(user);

        boolean exists = userRepository.existsByEmail(email);

        assertThat(exists).isTrue();
    }

    @Test
    void existsByEmailShouldReturnFalseWhenNotExists() {
        boolean exists = userRepository.existsByEmail("nonexistent@example.com");

        assertThat(exists).isFalse();
    }

    @Test
    void savingDuplicateEmailShouldFail() {
        String email = "duplicate@example.com";
        
        User firstUser = User.builder()
                .email(email)
                .passwordHash("hashedPassword1")
                .build();
        userRepository.save(firstUser);

        User secondUser = User.builder()
                .email(email)
                .passwordHash("hashedPassword2")
                .build();

        assertThatThrownBy(() -> userRepository.save(secondUser))
                .isInstanceOf(DuplicateKeyException.class);
    }
}


package com.example.reading.auth;

import com.example.reading.auth.exception.EmailAlreadyUsedException;
import com.example.reading.auth.exception.InvalidCredentialsException;
import com.example.reading.user.User;
import com.example.reading.user.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Service handling user registration and login.
 */
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Registers a new user with the given email and password.
     *
     * @param email the user's email
     * @param rawPassword the user's plain-text password
     * @return the created User
     * @throws EmailAlreadyUsedException if email is already registered
     */
    public User register(String email, String rawPassword) {
        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyUsedException(email);
        }

        User user = User.builder()
                .email(email)
                .passwordHash(passwordEncoder.encode(rawPassword))
                .build();

        return userRepository.save(user);
    }

    /**
     * Authenticates a user with the given email and password.
     *
     * @param email the user's email
     * @param rawPassword the user's plain-text password
     * @return the authenticated User
     * @throws InvalidCredentialsException if email not found or password mismatch
     */
    public User login(String email, String rawPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        return user;
    }
}


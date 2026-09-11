package com.studentcentral.auth.service;

import com.studentcentral.auth.dto.*;
import com.studentcentral.auth.exception.AccountDisabledException;
import com.studentcentral.auth.exception.InvalidCredentialsException;
import com.studentcentral.auth.exception.UserAlreadyExistsException;
import com.studentcentral.auth.exception.UserNotFoundException;
import com.studentcentral.auth.model.Role;
import com.studentcentral.auth.model.User;
import com.studentcentral.auth.repository.UserRepository;
import com.studentcentral.auth.security.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    /**
     * Register a new user.
     * For public registration, the role is always enforced as STUDENT for security.
     */
    public UserResponse register(RegisterRequest request) {
        String normalizedEmail = normalizeEmail(request.getEmail());

        if (userRepository.existsByEmail(normalizedEmail)) {
            log.warn("Registration attempt failed: email '{}' already exists", normalizedEmail);
            throw new UserAlreadyExistsException("An account with this email address already exists");
        }

        // Public registration always defaults/enforces STUDENT role
        Role assignedRole = Role.STUDENT;

        User user = new User();
        user.setName(request.getName().trim());
        user.setEmail(normalizedEmail);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(assignedRole);
        user.setActive(true);
        user.setCreatedAt(Instant.now());
        user.setUpdatedAt(Instant.now());

        User savedUser = userRepository.save(user);
        log.info("User registered successfully: id={}, email={}, role={}", savedUser.getId(), savedUser.getEmail(), savedUser.getRole());

        return UserResponse.fromUser(savedUser);
    }

    /**
     * Authenticate a user and return a signed JWT token.
     */
    public AuthResponse login(LoginRequest request) {
        String normalizedEmail = normalizeEmail(request.getEmail());

        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            log.warn("Login failed: password mismatch for email '{}'", normalizedEmail);
            throw new InvalidCredentialsException("Invalid email or password");
        }

        if (!user.isActive()) {
            log.warn("Login rejected: user account '{}' is disabled", normalizedEmail);
            throw new AccountDisabledException("Account is disabled. Please contact administrator.");
        }

        String token = jwtService.generateToken(user.getId(), user.getEmail(), user.getRole());
        log.info("User authenticated successfully: id={}, email={}, role={}", user.getId(), user.getEmail(), user.getRole());

        return new AuthResponse(
                token,
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                jwtService.getExpirationMillis()
        );
    }

    /**
     * Retrieve the user profile by user ID.
     */
    public UserResponse getUserById(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));
        return UserResponse.fromUser(user);
    }

    /**
     * Retrieve the user profile by email.
     */
    public UserResponse getUserByEmail(String email) {
        String normalizedEmail = normalizeEmail(email);
        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + normalizedEmail));
        return UserResponse.fromUser(user);
    }

    /**
     * Validate an incoming JWT token and return its details.
     */
    public TokenValidationResponse validateToken(String rawToken) {
        String token = cleanToken(rawToken);

        if (token == null || token.isBlank()) {
            return TokenValidationResponse.invalid("Token is missing or blank");
        }

        try {
            if (!jwtService.validateToken(token)) {
                return TokenValidationResponse.invalid("Token is invalid or expired");
            }

            String userId = jwtService.extractUserId(token);
            String email = jwtService.extractEmail(token);
            Role role = jwtService.extractRole(token);

            return TokenValidationResponse.valid(userId, email, role);
        } catch (Exception ex) {
            return TokenValidationResponse.invalid("Token validation error: " + ex.getMessage());
        }
    }

    private String normalizeEmail(String email) {
        return email != null ? email.trim().toLowerCase() : "";
    }

    private String cleanToken(String token) {
        if (token == null) {
            return null;
        }
        if (token.startsWith("Bearer ")) {
            return token.substring(7).trim();
        }
        return token.trim();
    }
}

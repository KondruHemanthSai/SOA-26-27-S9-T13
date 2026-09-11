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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository, passwordEncoder, jwtService);
    }

    @Test
    void shouldRegisterUserSuccessfullyWithHashedPasswordAndDefaultStudentRole() {
        RegisterRequest request = new RegisterRequest("John Doe", "john@example.com", "Password@123", Role.ADMIN);

        when(userRepository.existsByEmail("john@example.com")).thenReturn(false);
        when(passwordEncoder.encode("Password@123")).thenReturn("$2a$10$hashedPasswordValue");

        User savedUser = new User("1", "John Doe", "john@example.com", "$2a$10$hashedPasswordValue", Role.STUDENT, true, Instant.now(), Instant.now());
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        UserResponse response = authService.register(request);

        assertNotNull(response);
        assertEquals("1", response.getId());
        assertEquals("John Doe", response.getName());
        assertEquals("john@example.com", response.getEmail());
        assertEquals(Role.STUDENT, response.getRole()); // Enforced safe STUDENT role
        assertTrue(response.isActive());

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User captured = userCaptor.getValue();
        assertEquals("$2a$10$hashedPasswordValue", captured.getPasswordHash());
        assertEquals(Role.STUDENT, captured.getRole());
    }

    @Test
    void shouldRejectDuplicateEmailRegistration() {
        RegisterRequest request = new RegisterRequest("John Doe", "john@example.com", "Password@123", null);

        when(userRepository.existsByEmail("john@example.com")).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class, () -> authService.register(request));
        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldLoginSuccessfullyWithValidCredentials() {
        LoginRequest request = new LoginRequest("john@example.com", "Password@123");
        User user = new User("1", "John Doe", "john@example.com", "$2a$10$hashedPasswordValue", Role.STUDENT, true, Instant.now(), Instant.now());

        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Password@123", "$2a$10$hashedPasswordValue")).thenReturn(true);
        when(jwtService.generateToken("1", "john@example.com", Role.STUDENT)).thenReturn("mock.jwt.token");
        when(jwtService.getExpirationMillis()).thenReturn(86400000L);

        AuthResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("mock.jwt.token", response.getToken());
        assertEquals("1", response.getUserId());
        assertEquals("John Doe", response.getName());
        assertEquals("john@example.com", response.getEmail());
        assertEquals(Role.STUDENT, response.getRole());
        assertEquals(86400000L, response.getExpiresIn());
    }

    @Test
    void shouldRejectLoginWhenUserNotFound() {
        LoginRequest request = new LoginRequest("unknown@example.com", "Password@123");

        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        assertThrows(InvalidCredentialsException.class, () -> authService.login(request));
    }

    @Test
    void shouldRejectLoginWhenPasswordMismatch() {
        LoginRequest request = new LoginRequest("john@example.com", "WrongPassword");
        User user = new User("1", "John Doe", "john@example.com", "$2a$10$hashedPasswordValue", Role.STUDENT, true, Instant.now(), Instant.now());

        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("WrongPassword", "$2a$10$hashedPasswordValue")).thenReturn(false);

        assertThrows(InvalidCredentialsException.class, () -> authService.login(request));
    }

    @Test
    void shouldRejectLoginWhenAccountIsDisabled() {
        LoginRequest request = new LoginRequest("john@example.com", "Password@123");
        User user = new User("1", "John Doe", "john@example.com", "$2a$10$hashedPasswordValue", Role.STUDENT, false, Instant.now(), Instant.now());

        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Password@123", "$2a$10$hashedPasswordValue")).thenReturn(true);

        assertThrows(AccountDisabledException.class, () -> authService.login(request));
    }

    @Test
    void shouldGetUserByIdSuccessfully() {
        User user = new User("1", "John Doe", "john@example.com", "hash", Role.STUDENT, true, Instant.now(), Instant.now());
        when(userRepository.findById("1")).thenReturn(Optional.of(user));

        UserResponse response = authService.getUserById("1");
        assertNotNull(response);
        assertEquals("1", response.getId());
        assertEquals("John Doe", response.getName());
    }

    @Test
    void shouldThrowWhenUserNotFoundById() {
        when(userRepository.findById("nonexistent")).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> authService.getUserById("nonexistent"));
    }

    @Test
    void shouldValidateValidToken() {
        when(jwtService.validateToken("valid.token")).thenReturn(true);
        when(jwtService.extractUserId("valid.token")).thenReturn("123");
        when(jwtService.extractEmail("valid.token")).thenReturn("user@example.com");
        when(jwtService.extractRole("valid.token")).thenReturn(Role.STUDENT);

        TokenValidationResponse response = authService.validateToken("Bearer valid.token");

        assertTrue(response.isValid());
        assertEquals("123", response.getUserId());
        assertEquals("user@example.com", response.getEmail());
        assertEquals(Role.STUDENT, response.getRole());
    }

    @Test
    void shouldReturnInvalidForInvalidToken() {
        when(jwtService.validateToken("invalid.token")).thenReturn(false);

        TokenValidationResponse response = authService.validateToken("Bearer invalid.token");

        assertFalse(response.isValid());
    }
}

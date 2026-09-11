package com.studentcentral.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.studentcentral.auth.dto.*;
import com.studentcentral.auth.exception.InvalidCredentialsException;
import com.studentcentral.auth.exception.UserAlreadyExistsException;
import com.studentcentral.auth.model.Role;
import com.studentcentral.auth.security.CustomUserDetails;
import com.studentcentral.auth.security.JwtAuthenticationEntryPoint;
import com.studentcentral.auth.security.JwtAuthenticationFilter;
import com.studentcentral.auth.security.JwtService;
import com.studentcentral.auth.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "eureka.client.enabled=false",
        "spring.data.mongodb.uri=mongodb://localhost:27017/test_db",
        "app.admin.bootstrap.enabled=false"
})
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private com.studentcentral.auth.repository.UserRepository userRepository;

    @Test
    void shouldRegisterStudentSuccessfully() throws Exception {
        RegisterRequest request = new RegisterRequest("Jane Doe", "jane@example.com", "Password@123", Role.STUDENT);
        UserResponse response = new UserResponse("user-1", "Jane Doe", "jane@example.com", Role.STUDENT, true, Instant.now());

        when(authService.register(any(RegisterRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("user-1"))
                .andExpect(jsonPath("$.name").value("Jane Doe"))
                .andExpect(jsonPath("$.email").value("jane@example.com"))
                .andExpect(jsonPath("$.role").value("STUDENT"))
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    @Test
    void shouldReturnBadRequestWhenRegistrationInputIsInvalid() throws Exception {
        RegisterRequest invalidRequest = new RegisterRequest("", "not-an-email", "123", null);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.validationErrors.name").exists())
                .andExpect(jsonPath("$.validationErrors.email").exists())
                .andExpect(jsonPath("$.validationErrors.password").exists());
    }

    @Test
    void shouldReturnConflictWhenUserAlreadyExists() throws Exception {
        RegisterRequest request = new RegisterRequest("Jane Doe", "jane@example.com", "Password@123", Role.STUDENT);

        when(authService.register(any(RegisterRequest.class)))
                .thenThrow(new UserAlreadyExistsException("An account with this email address already exists"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("USER_ALREADY_EXISTS"))
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void shouldLoginSuccessfully() throws Exception {
        LoginRequest request = new LoginRequest("jane@example.com", "Password@123");
        AuthResponse response = new AuthResponse("mocked.jwt.token", "user-1", "Jane Doe", "jane@example.com", Role.STUDENT, 86400000L);

        when(authService.login(any(LoginRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mocked.jwt.token"))
                .andExpect(jsonPath("$.userId").value("user-1"))
                .andExpect(jsonPath("$.name").value("Jane Doe"))
                .andExpect(jsonPath("$.email").value("jane@example.com"))
                .andExpect(jsonPath("$.role").value("STUDENT"));
    }

    @Test
    void shouldReturnUnauthorizedOnInvalidLoginCredentials() throws Exception {
        LoginRequest request = new LoginRequest("jane@example.com", "WrongPassword");

        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new InvalidCredentialsException("Invalid email or password"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("INVALID_CREDENTIALS"))
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void shouldReturnCurrentUserWhenAuthenticated() throws Exception {
        CustomUserDetails userDetails = new CustomUserDetails("user-1", "Jane Doe", "jane@example.com", "", Role.STUDENT, true);
        UserResponse response = new UserResponse("user-1", "Jane Doe", "jane@example.com", Role.STUDENT, true, Instant.now());

        when(authService.getUserById("user-1")).thenReturn(response);

        mockMvc.perform(get("/api/auth/me")
                        .with(user(userDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("user-1"))
                .andExpect(jsonPath("$.name").value("Jane Doe"))
                .andExpect(jsonPath("$.email").value("jane@example.com"))
                .andExpect(jsonPath("$.role").value("STUDENT"));
    }

    @Test
    void shouldRejectMeEndpointWhenUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldValidateTokenSuccessfully() throws Exception {
        CustomUserDetails userDetails = new CustomUserDetails("user-1", "Jane Doe", "jane@example.com", "", Role.STUDENT, true);
        TokenValidationResponse validationResponse = TokenValidationResponse.valid("user-1", "jane@example.com", Role.STUDENT);

        when(authService.validateToken("Bearer some.valid.token")).thenReturn(validationResponse);

        mockMvc.perform(get("/api/auth/validate")
                        .header("Authorization", "Bearer some.valid.token")
                        .with(user(userDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(true))
                .andExpect(jsonPath("$.userId").value("user-1"))
                .andExpect(jsonPath("$.role").value("STUDENT"));
    }

    @Test
    void shouldRejectInvalidTokenOnValidation() throws Exception {
        CustomUserDetails userDetails = new CustomUserDetails("user-1", "Jane Doe", "jane@example.com", "", Role.STUDENT, true);
        TokenValidationResponse validationResponse = TokenValidationResponse.invalid("Invalid signature");

        when(authService.validateToken("Bearer bad.token")).thenReturn(validationResponse);

        mockMvc.perform(get("/api/auth/validate")
                        .header("Authorization", "Bearer bad.token")
                        .with(user(userDetails)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.valid").value(false));
    }
}

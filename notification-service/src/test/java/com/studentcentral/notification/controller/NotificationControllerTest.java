package com.studentcentral.notification.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.studentcentral.notification.dto.*;
import com.studentcentral.notification.exception.GlobalExceptionHandler;
import com.studentcentral.notification.model.NotificationPriority;
import com.studentcentral.notification.model.NotificationType;
import com.studentcentral.notification.security.AuthenticatedUser;
import com.studentcentral.notification.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NotificationController.class)
@Import({GlobalExceptionHandler.class, NotificationControllerTest.TestSecurityConfig.class})
@AutoConfigureMockMvc
class NotificationControllerTest {

    @TestConfiguration
    @EnableWebSecurity
    @EnableMethodSecurity
    static class TestSecurityConfig {
        @Bean
        public SecurityFilterChain testFilterChain(HttpSecurity http) throws Exception {
            http.csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
            return http.build();
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private NotificationService notificationService;

    @MockBean
    private com.studentcentral.notification.security.JwtService jwtService;

    // ==========================================
    // Student Endpoint Tests
    // ==========================================

    @Test
    void shouldReturnMyNotificationsPaginated() throws Exception {
        AuthenticatedUser student = new AuthenticatedUser("user-101", "student@example.com", "STUDENT");
        NotificationPageResponse pageResponse = new NotificationPageResponse(
                List.of(), 0, 20, 0, 0);
        when(notificationService.getMyNotifications(eq("user-101"), anyInt(), anyInt()))
                .thenReturn(pageResponse);

        mockMvc.perform(get("/api/notifications/my")
                        .with(user(student)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void shouldReturnUnreadCount() throws Exception {
        AuthenticatedUser student = new AuthenticatedUser("user-101", "student@example.com", "STUDENT");
        when(notificationService.getUnreadCount("user-101"))
                .thenReturn(new UnreadCountResponse(4));

        mockMvc.perform(get("/api/notifications/my/unread-count")
                        .with(user(student)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.unreadCount").value(4));
    }

    // ==========================================
    // Admin Endpoint Tests
    // ==========================================

    @Test
    void shouldRejectStudentCreatingAdminNotification() throws Exception {
        AuthenticatedUser student = new AuthenticatedUser("user-101", "student@example.com", "STUDENT");
        CreateNotificationRequest request = new CreateNotificationRequest(
                "user-102", "Test", "Test message",
                NotificationType.SYSTEM, NotificationPriority.NORMAL);

        mockMvc.perform(post("/api/notifications")
                        .with(user(student))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldAllowAdminToCreateNotification() throws Exception {
        AuthenticatedUser admin = new AuthenticatedUser("admin-001", "admin@example.com", "ADMIN");
        CreateNotificationRequest request = new CreateNotificationRequest(
                "user-102", "Admission Approved", "Your admission has been approved.",
                NotificationType.ADMISSION, NotificationPriority.HIGH);

        NotificationResponse response = new NotificationResponse();
        response.setNotificationId("NOTIF-TEST001");
        response.setUserId("user-102");
        response.setTitle("Admission Approved");

        when(notificationService.createAdminNotification(eq("admin-001"), any(CreateNotificationRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/notifications")
                        .with(user(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.notificationId").value("NOTIF-TEST001"));
    }

    // ==========================================
    // Validation Tests
    // ==========================================

    @Test
    void shouldRejectCreateNotificationWithMissingTitle() throws Exception {
        AuthenticatedUser admin = new AuthenticatedUser("admin-001", "admin@example.com", "ADMIN");
        CreateNotificationRequest request = new CreateNotificationRequest();
        request.setUserId("user-102");
        request.setMessage("Test message");
        request.setType(NotificationType.SYSTEM);
        request.setPriority(NotificationPriority.NORMAL);
        // title is missing

        mockMvc.perform(post("/api/notifications")
                        .with(user(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // ==========================================
    // Internal Endpoint Tests
    // ==========================================

    @Test
    void shouldAcceptInternalNotificationRequest() throws Exception {
        InternalNotificationRequest request = new InternalNotificationRequest(
                "user-101", "Registration Successful", "You registered for CS301.",
                NotificationType.REGISTRATION, NotificationPriority.NORMAL,
                "REGISTRATION", "REG-123", "registration-service"
        );

        NotificationResponse response = new NotificationResponse();
        response.setNotificationId("NOTIF-INT001");
        response.setUserId("user-101");

        when(notificationService.createInternalNotification(any(InternalNotificationRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/notifications/internal")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.notificationId").value("NOTIF-INT001"));
    }
}

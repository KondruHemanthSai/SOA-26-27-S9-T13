package com.studentcentral.registration.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.studentcentral.registration.dto.*;
import com.studentcentral.registration.model.RegistrationStatus;
import com.studentcentral.registration.repository.RegistrationRepository;
import com.studentcentral.registration.security.AuthenticatedUser;
import com.studentcentral.registration.service.RegistrationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "eureka.client.enabled=false",
        "spring.data.mongodb.uri=mongodb://localhost:27017/test_registration_db"
})
@AutoConfigureMockMvc
class RegistrationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RegistrationService registrationService;

    @MockBean
    private RegistrationRepository registrationRepository;

    @Test
    void shouldAllowStudentToRegisterCourse() throws Exception {
        AuthenticatedUser student = new AuthenticatedUser("user-101", "student@example.com", "STUDENT");
        CreateRegistrationRequest request = new CreateRegistrationRequest("CS501");

        RegistrationResponse regResponse = new RegistrationResponse(
                "reg-doc-1", "SC-REG-2026-00001", "SC20260001", "c-ml-1", "CS501",
                "Machine Learning", 5, "2026-27", 4, RegistrationStatus.REGISTERED, Instant.now(), Instant.now()
        );

        RegistrationSuccessResponse successResponse = new RegistrationSuccessResponse(
                true, "Course registered successfully", regResponse
        );

        when(registrationService.registerCourse(any(AuthenticatedUser.class), any(CreateRegistrationRequest.class)))
                .thenReturn(successResponse);

        mockMvc.perform(post("/api/registrations")
                        .with(user(student))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.registration.registrationId").value("SC-REG-2026-00001"))
                .andExpect(jsonPath("$.registration.courseCode").value("CS501"));
    }

    @Test
    void shouldRejectInvalidRegistrationInput() throws Exception {
        AuthenticatedUser student = new AuthenticatedUser("user-101", "student@example.com", "STUDENT");
        CreateRegistrationRequest invalid = new CreateRegistrationRequest("");

        mockMvc.perform(post("/api/registrations")
                        .with(user(student))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }

    @Test
    void shouldAllowStudentToViewOwnRegistrations() throws Exception {
        AuthenticatedUser student = new AuthenticatedUser("user-101", "student@example.com", "STUDENT");
        RegistrationResponse regResponse = new RegistrationResponse(
                "reg-doc-1", "SC-REG-2026-00001", "SC20260001", "c-ml-1", "CS501",
                "Machine Learning", 5, "2026-27", 4, RegistrationStatus.REGISTERED, Instant.now(), Instant.now()
        );

        RegistrationListResponse listResponse = new RegistrationListResponse("SC20260001", List.of(regResponse));

        when(registrationService.getMyRegistrations(any(AuthenticatedUser.class))).thenReturn(listResponse);

        mockMvc.perform(get("/api/registrations/my")
                        .with(user(student)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.studentId").value("SC20260001"))
                .andExpect(jsonPath("$.registrations[0].courseCode").value("CS501"));
    }

    @Test
    void shouldAllowStudentToDropCourse() throws Exception {
        AuthenticatedUser student = new AuthenticatedUser("user-101", "student@example.com", "STUDENT");
        RegistrationResponse regResponse = new RegistrationResponse(
                "reg-doc-1", "SC-REG-2026-00001", "SC20260001", "c-ml-1", "CS501",
                "Machine Learning", 5, "2026-27", 4, RegistrationStatus.DROPPED, Instant.now(), Instant.now()
        );

        when(registrationService.dropCourse(any(AuthenticatedUser.class), eq("SC-REG-2026-00001")))
                .thenReturn(regResponse);

        mockMvc.perform(delete("/api/registrations/SC-REG-2026-00001")
                        .with(user(student)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DROPPED"));
    }

    @Test
    void shouldAllowAdminToListRegistrations() throws Exception {
        AuthenticatedUser admin = new AuthenticatedUser("admin-1", "admin@studentcentral.com", "ADMIN");
        RegistrationResponse regResponse = new RegistrationResponse(
                "reg-doc-1", "SC-REG-2026-00001", "SC20260001", "c-ml-1", "CS501",
                "Machine Learning", 5, "2026-27", 4, RegistrationStatus.REGISTERED, Instant.now(), Instant.now()
        );

        when(registrationService.listAllRegistrations(any(), any(), any()))
                .thenReturn(List.of(regResponse));

        mockMvc.perform(get("/api/registrations")
                        .with(user(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].registrationId").value("SC-REG-2026-00001"));
    }

    @Test
    void shouldForbidStudentFromAdminListing() throws Exception {
        AuthenticatedUser student = new AuthenticatedUser("user-101", "student@example.com", "STUDENT");

        mockMvc.perform(get("/api/registrations")
                        .with(user(student)))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldRejectUnauthenticatedAccess() throws Exception {
        mockMvc.perform(get("/api/registrations/my"))
                .andExpect(status().isUnauthorized());
    }
}

package com.studentcentral.admission.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.studentcentral.admission.dto.*;
import com.studentcentral.admission.exception.ApplicationNotFoundException;
import com.studentcentral.admission.model.ApplicationStatus;
import com.studentcentral.admission.model.DocumentStatus;
import com.studentcentral.admission.model.DocumentType;
import com.studentcentral.admission.model.ReviewAction;
import com.studentcentral.admission.repository.AdmissionApplicationRepository;
import com.studentcentral.admission.repository.DocumentRepository;
import com.studentcentral.admission.security.AuthenticatedUser;
import com.studentcentral.admission.service.AdmissionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "eureka.client.enabled=false",
        "spring.data.mongodb.uri=mongodb://localhost:27017/test_admission_db"
})
@AutoConfigureMockMvc
class AdmissionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AdmissionService admissionService;

    @MockBean
    private AdmissionApplicationRepository applicationRepository;

    @MockBean
    private DocumentRepository documentRepository;

    @Test
    void shouldCreateApplicationSuccessfully() throws Exception {
        AuthenticatedUser student = new AuthenticatedUser("user-101", "student@example.com", "STUDENT");
        CreateApplicationRequest request = new CreateApplicationRequest(
                "B.Tech Computer Science", "Computer Science",
                new AcademicDetailsRequest("High School", "CBSE", 92.0, 9.2, 2022)
        );

        ApplicationResponse response = new ApplicationResponse(
                "mongo-1", "SC-ADM-2026-00001", "SC20260001", "user-101",
                "B.Tech Computer Science", "Computer Science",
                new AcademicDetailsResponse("High School", "CBSE", 92.0, 9.2, 2022),
                LocalDate.now(), ApplicationStatus.DRAFT, null, null, null, null,
                List.of(), Instant.now(), Instant.now()
        );

        when(admissionService.createApplication(any(AuthenticatedUser.class), any(CreateApplicationRequest.class), any()))
                .thenReturn(response);

        mockMvc.perform(post("/api/admissions/apply")
                        .with(user(student))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.applicationId").value("SC-ADM-2026-00001"))
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andExpect(jsonPath("$.program").value("B.Tech Computer Science"));
    }

    @Test
    void shouldRejectApplicationWithValidationErrors() throws Exception {
        AuthenticatedUser student = new AuthenticatedUser("user-101", "student@example.com", "STUDENT");
        CreateApplicationRequest invalidRequest = new CreateApplicationRequest("", "", null);

        mockMvc.perform(post("/api/admissions/apply")
                        .with(user(student))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.validationErrors.program").exists())
                .andExpect(jsonPath("$.validationErrors.department").exists());
    }

    @Test
    void shouldGetOwnApplicationSuccessfully() throws Exception {
        AuthenticatedUser student = new AuthenticatedUser("user-101", "student@example.com", "STUDENT");
        ApplicationResponse response = new ApplicationResponse(
                "mongo-1", "SC-ADM-2026-00001", "SC20260001", "user-101",
                "B.Tech Computer Science", "Computer Science",
                new AcademicDetailsResponse("High School", "CBSE", 92.0, 9.2, 2022),
                LocalDate.now(), ApplicationStatus.DRAFT, null, null, null, null,
                List.of(), Instant.now(), Instant.now()
        );

        when(admissionService.getOwnApplication("user-101")).thenReturn(response);

        mockMvc.perform(get("/api/admissions/my-application")
                        .with(user(student)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.applicationId").value("SC-ADM-2026-00001"));
    }

    @Test
    void shouldReturn404WhenOwnApplicationNotFound() throws Exception {
        AuthenticatedUser student = new AuthenticatedUser("user-404", "student@example.com", "STUDENT");

        when(admissionService.getOwnApplication("user-404"))
                .thenThrow(new ApplicationNotFoundException("No admission application found for current student"));

        mockMvc.perform(get("/api/admissions/my-application")
                        .with(user(student)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("APPLICATION_NOT_FOUND"));
    }

    @Test
    void shouldSubmitApplicationSuccessfully() throws Exception {
        AuthenticatedUser student = new AuthenticatedUser("user-101", "student@example.com", "STUDENT");
        ApplicationResponse response = new ApplicationResponse(
                "mongo-1", "SC-ADM-2026-00001", "SC20260001", "user-101",
                "B.Tech Computer Science", "Computer Science",
                new AcademicDetailsResponse("High School", "CBSE", 92.0, 9.2, 2022),
                LocalDate.now(), ApplicationStatus.SUBMITTED, Instant.now(), null, null, null,
                List.of(), Instant.now(), Instant.now()
        );

        when(admissionService.submitApplication("user-101", "SC-ADM-2026-00001")).thenReturn(response);

        mockMvc.perform(post("/api/admissions/SC-ADM-2026-00001/submit")
                        .with(user(student)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUBMITTED"));
    }

    @Test
    void shouldUploadDocumentSuccessfully() throws Exception {
        AuthenticatedUser student = new AuthenticatedUser("user-101", "student@example.com", "STUDENT");
        UploadDocumentRequest request = new UploadDocumentRequest(
                DocumentType.ID_PROOF, "passport.pdf", "/url"
        );
        DocumentResponse response = new DocumentResponse(
                "doc-1", "SC-ADM-2026-00001", "SC20260001", DocumentType.ID_PROOF,
                "passport.pdf", "/url", Instant.now(), DocumentStatus.UPLOADED, null
        );

        when(admissionService.uploadDocument(eq("user-101"), eq("SC-ADM-2026-00001"), any(UploadDocumentRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/admissions/SC-ADM-2026-00001/documents")
                        .with(user(student))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.fileName").value("passport.pdf"))
                .andExpect(jsonPath("$.type").value("ID_PROOF"));
    }

    @Test
    void shouldDeleteDocumentSuccessfully() throws Exception {
        AuthenticatedUser student = new AuthenticatedUser("user-101", "student@example.com", "STUDENT");
        doNothing().when(admissionService).deleteDocument("user-101", "SC-ADM-2026-00001", "doc-1");

        mockMvc.perform(delete("/api/admissions/SC-ADM-2026-00001/documents/doc-1")
                        .with(user(student)))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldAllowAdminToListApplications() throws Exception {
        AuthenticatedUser admin = new AuthenticatedUser("admin-1", "admin@studentcentral.com", "ADMIN");
        ApplicationResponse app = new ApplicationResponse(
                "mongo-1", "SC-ADM-2026-00001", "SC20260001", "user-101",
                "B.Tech Computer Science", "Computer Science", null,
                LocalDate.now(), ApplicationStatus.SUBMITTED, Instant.now(), null, null, null,
                List.of(), Instant.now(), Instant.now()
        );

        when(admissionService.listApplications(eq("Computer Science"), eq(ApplicationStatus.SUBMITTED)))
                .thenReturn(List.of(app));

        mockMvc.perform(get("/api/admissions")
                        .param("department", "Computer Science")
                        .param("status", "SUBMITTED")
                        .with(user(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].applicationId").value("SC-ADM-2026-00001"));
    }

    @Test
    void shouldForbidStudentFromListingApplications() throws Exception {
        AuthenticatedUser student = new AuthenticatedUser("user-101", "student@example.com", "STUDENT");

        mockMvc.perform(get("/api/admissions")
                        .with(user(student)))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldAllowAdminToReviewApplication() throws Exception {
        AuthenticatedUser admin = new AuthenticatedUser("admin-1", "admin@studentcentral.com", "ADMIN");
        ApplicationReviewRequest request = new ApplicationReviewRequest(ReviewAction.APPROVE, "All verified");
        ApplicationResponse response = new ApplicationResponse(
                "mongo-1", "SC-ADM-2026-00001", "SC20260001", "user-101",
                "B.Tech Computer Science", "Computer Science", null,
                LocalDate.now(), ApplicationStatus.APPROVED, Instant.now(), Instant.now(), "admin-1", "All verified",
                List.of(), Instant.now(), Instant.now()
        );

        when(admissionService.reviewApplication(eq("admin-1"), eq("SC-ADM-2026-00001"), any(ApplicationReviewRequest.class)))
                .thenReturn(response);

        mockMvc.perform(put("/api/admissions/SC-ADM-2026-00001/review")
                        .with(user(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"))
                .andExpect(jsonPath("$.remarks").value("All verified"));
    }

    @Test
    void shouldForbidStudentFromReviewingApplication() throws Exception {
        AuthenticatedUser student = new AuthenticatedUser("user-101", "student@example.com", "STUDENT");
        ApplicationReviewRequest request = new ApplicationReviewRequest(ReviewAction.APPROVE, "Self approval attempt");

        mockMvc.perform(put("/api/admissions/SC-ADM-2026-00001/review")
                        .with(user(student))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldAllowAdminToReviewDocument() throws Exception {
        AuthenticatedUser admin = new AuthenticatedUser("admin-1", "admin@studentcentral.com", "ADMIN");
        DocumentReviewRequest request = new DocumentReviewRequest(DocumentStatus.VERIFIED, "Marks card verified");
        DocumentResponse response = new DocumentResponse(
                "doc-1", "SC-ADM-2026-00001", "SC20260001", DocumentType.MARKS_CERTIFICATE,
                "marks.pdf", "/url", Instant.now(), DocumentStatus.VERIFIED, "Marks card verified"
        );

        when(admissionService.reviewDocument(eq("admin-1"), eq("SC-ADM-2026-00001"), eq("doc-1"), any(DocumentReviewRequest.class)))
                .thenReturn(response);

        mockMvc.perform(put("/api/admissions/SC-ADM-2026-00001/documents/doc-1/review")
                        .with(user(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("VERIFIED"));
    }

    @Test
    void shouldRejectUnauthenticatedRequest() throws Exception {
        mockMvc.perform(get("/api/admissions/my-application"))
                .andExpect(status().isUnauthorized());
    }
}

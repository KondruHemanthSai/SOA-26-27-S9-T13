package com.studentcentral.admission.service;

import com.studentcentral.admission.client.StudentServiceClient;
import com.studentcentral.admission.dto.*;
import com.studentcentral.admission.exception.*;
import com.studentcentral.admission.model.*;
import com.studentcentral.admission.repository.AdmissionApplicationRepository;
import com.studentcentral.admission.repository.DocumentRepository;
import com.studentcentral.admission.security.AuthenticatedUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdmissionServiceTest {

    @Mock
    private AdmissionApplicationRepository applicationRepository;

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private ApplicationIdGenerator applicationIdGenerator;

    @Mock
    private StudentServiceClient studentServiceClient;

    @InjectMocks
    private AdmissionService admissionService;

    private AuthenticatedUser studentUser;
    private AuthenticatedUser otherStudentUser;
    private AuthenticatedUser adminUser;
    private AdmissionApplication draftApp;

    @BeforeEach
    void setUp() {
        studentUser = new AuthenticatedUser("user-101", "student@example.com", "STUDENT");
        otherStudentUser = new AuthenticatedUser("user-999", "other@example.com", "STUDENT");
        adminUser = new AuthenticatedUser("admin-1", "admin@studentcentral.com", "ADMIN");

        draftApp = new AdmissionApplication(
                "SC-ADM-2026-00001",
                "SC20260001",
                "user-101",
                "B.Tech Computer Science",
                "Computer Science",
                new AcademicDetails("High School", "CBSE", 90.0, 9.0, 2022)
        );
        draftApp.setId("app-mongo-1");
    }

    @Test
    void shouldCreateApplicationSuccessfullyInDraftStatus() {
        CreateApplicationRequest request = new CreateApplicationRequest(
                "B.Tech Computer Science", "Computer Science",
                new AcademicDetailsRequest("High School", "CBSE", 90.0, 9.0, 2022)
        );

        when(applicationRepository.existsByUserId("user-101")).thenReturn(false);
        when(studentServiceClient.resolveStudentId(eq("user-101"), any())).thenReturn("SC20260001");
        when(applicationIdGenerator.generateApplicationId()).thenReturn("SC-ADM-2026-00001");
        when(applicationRepository.save(any(AdmissionApplication.class))).thenAnswer(i -> i.getArgument(0));

        ApplicationResponse response = admissionService.createApplication(studentUser, request, "Bearer token");

        assertNotNull(response);
        assertEquals("SC-ADM-2026-00001", response.getApplicationId());
        assertEquals("SC20260001", response.getStudentId());
        assertEquals("user-101", response.getUserId());
        assertEquals(ApplicationStatus.DRAFT, response.getStatus());
        assertEquals("B.Tech Computer Science", response.getProgram());
    }

    @Test
    void shouldRejectDuplicateApplicationCreation() {
        CreateApplicationRequest request = new CreateApplicationRequest("B.Tech", "CSE", null);
        when(applicationRepository.existsByUserId("user-101")).thenReturn(true);

        assertThrows(ApplicationAlreadyExistsException.class, () ->
                admissionService.createApplication(studentUser, request, "Bearer token"));
    }

    @Test
    void shouldGetOwnApplicationSuccessfully() {
        when(applicationRepository.findByUserId("user-101")).thenReturn(Optional.of(draftApp));
        when(documentRepository.findByApplicationId("SC-ADM-2026-00001")).thenReturn(List.of());

        ApplicationResponse response = admissionService.getOwnApplication("user-101");

        assertNotNull(response);
        assertEquals("SC-ADM-2026-00001", response.getApplicationId());
    }

    @Test
    void shouldThrowWhenOwnApplicationNotFound() {
        when(applicationRepository.findByUserId("user-404")).thenReturn(Optional.empty());

        assertThrows(ApplicationNotFoundException.class, () ->
                admissionService.getOwnApplication("user-404"));
    }

    @Test
    void shouldUpdateDraftApplicationSuccessfully() {
        UpdateApplicationRequest request = new UpdateApplicationRequest();
        request.setProgram("B.Tech Artificial Intelligence");

        when(applicationRepository.findByApplicationId("SC-ADM-2026-00001")).thenReturn(Optional.of(draftApp));
        when(applicationRepository.save(any(AdmissionApplication.class))).thenAnswer(i -> i.getArgument(0));
        when(documentRepository.findByApplicationId("SC-ADM-2026-00001")).thenReturn(List.of());

        ApplicationResponse response = admissionService.updateOwnApplication("user-101", "SC-ADM-2026-00001", request);

        assertEquals("B.Tech Artificial Intelligence", response.getProgram());
    }

    @Test
    void shouldRejectUpdateWhenApplicationIsApproved() {
        draftApp.setStatus(ApplicationStatus.APPROVED);
        when(applicationRepository.findByApplicationId("SC-ADM-2026-00001")).thenReturn(Optional.of(draftApp));

        UpdateApplicationRequest request = new UpdateApplicationRequest("B.Tech AI", "CSE", null);

        assertThrows(ApplicationNotEditableException.class, () ->
                admissionService.updateOwnApplication("user-101", "SC-ADM-2026-00001", request));
    }

    @Test
    void shouldRejectUpdateWhenUserIsNotOwner() {
        when(applicationRepository.findByApplicationId("SC-ADM-2026-00001")).thenReturn(Optional.of(draftApp));
        UpdateApplicationRequest request = new UpdateApplicationRequest("B.Tech AI", "CSE", null);

        assertThrows(UnauthorizedApplicationAccessException.class, () ->
                admissionService.updateOwnApplication("user-999", "SC-ADM-2026-00001", request));
    }

    @Test
    void shouldSubmitValidApplicationWhenAllRequiredDocumentsPresent() {
        when(applicationRepository.findByApplicationId("SC-ADM-2026-00001")).thenReturn(Optional.of(draftApp));
        when(documentRepository.existsByApplicationIdAndType("SC-ADM-2026-00001", DocumentType.ID_PROOF)).thenReturn(true);
        when(documentRepository.existsByApplicationIdAndType("SC-ADM-2026-00001", DocumentType.MARKS_CERTIFICATE)).thenReturn(true);
        when(documentRepository.existsByApplicationIdAndType("SC-ADM-2026-00001", DocumentType.TRANSFER_CERTIFICATE)).thenReturn(true);
        when(applicationRepository.save(any(AdmissionApplication.class))).thenAnswer(i -> i.getArgument(0));
        when(documentRepository.findByApplicationId("SC-ADM-2026-00001")).thenReturn(List.of());

        ApplicationResponse response = admissionService.submitApplication("user-101", "SC-ADM-2026-00001");

        assertEquals(ApplicationStatus.SUBMITTED, response.getStatus());
        assertNotNull(response.getSubmissionDate());
    }

    @Test
    void shouldRejectSubmissionWhenRequiredDocumentsAreMissing() {
        when(applicationRepository.findByApplicationId("SC-ADM-2026-00001")).thenReturn(Optional.of(draftApp));
        when(documentRepository.existsByApplicationIdAndType("SC-ADM-2026-00001", DocumentType.ID_PROOF)).thenReturn(true);
        when(documentRepository.existsByApplicationIdAndType("SC-ADM-2026-00001", DocumentType.MARKS_CERTIFICATE)).thenReturn(false);
        when(documentRepository.existsByApplicationIdAndType("SC-ADM-2026-00001", DocumentType.TRANSFER_CERTIFICATE)).thenReturn(true);

        assertThrows(RequiredDocumentMissingException.class, () ->
                admissionService.submitApplication("user-101", "SC-ADM-2026-00001"));
    }

    @Test
    void shouldUploadDocumentSuccessfully() {
        UploadDocumentRequest request = new UploadDocumentRequest(
                DocumentType.ID_PROOF, "passport.pdf", "http://storage/passport.pdf"
        );

        when(applicationRepository.findByApplicationId("SC-ADM-2026-00001")).thenReturn(Optional.of(draftApp));
        when(documentRepository.save(any(Document.class))).thenAnswer(i -> {
            Document doc = i.getArgument(0);
            doc.setId("doc-1");
            return doc;
        });

        DocumentResponse response = admissionService.uploadDocument("user-101", "SC-ADM-2026-00001", request);

        assertNotNull(response);
        assertEquals("doc-1", response.getId());
        assertEquals(DocumentType.ID_PROOF, response.getType());
        assertEquals("passport.pdf", response.getFileName());
    }

    @Test
    void shouldDeleteDocumentWhenApplicationInDraftStatus() {
        Document doc = new Document("SC-ADM-2026-00001", "SC20260001", DocumentType.ID_PROOF, "id.pdf", "/url");
        doc.setId("doc-1");

        when(applicationRepository.findByApplicationId("SC-ADM-2026-00001")).thenReturn(Optional.of(draftApp));
        when(documentRepository.findByIdAndApplicationId("doc-1", "SC-ADM-2026-00001")).thenReturn(Optional.of(doc));

        admissionService.deleteDocument("user-101", "SC-ADM-2026-00001", "doc-1");

        verify(documentRepository, times(1)).delete(doc);
    }

    @Test
    void shouldPreventDocumentDeletionWhenApplicationIsApproved() {
        draftApp.setStatus(ApplicationStatus.APPROVED);
        when(applicationRepository.findByApplicationId("SC-ADM-2026-00001")).thenReturn(Optional.of(draftApp));

        assertThrows(ApplicationNotEditableException.class, () ->
                admissionService.deleteDocument("user-101", "SC-ADM-2026-00001", "doc-1"));
    }

    // ==========================================
    // State Transition & Admin Review Tests
    // ==========================================

    @Test
    void shouldAllowAdminToStartReviewOnSubmittedApplication() {
        draftApp.setStatus(ApplicationStatus.SUBMITTED);
        when(applicationRepository.findByApplicationId("SC-ADM-2026-00001")).thenReturn(Optional.of(draftApp));
        when(applicationRepository.save(any(AdmissionApplication.class))).thenAnswer(i -> i.getArgument(0));
        when(documentRepository.findByApplicationId("SC-ADM-2026-00001")).thenReturn(List.of());

        ApplicationReviewRequest request = new ApplicationReviewRequest(ReviewAction.START_REVIEW, "Beginning review");
        ApplicationResponse response = admissionService.reviewApplication("admin-1", "SC-ADM-2026-00001", request);

        assertEquals(ApplicationStatus.UNDER_REVIEW, response.getStatus());
        assertEquals("admin-1", response.getReviewedBy());
    }

    @Test
    void shouldAllowAdminToApproveApplicationUnderReview() {
        draftApp.setStatus(ApplicationStatus.UNDER_REVIEW);
        when(applicationRepository.findByApplicationId("SC-ADM-2026-00001")).thenReturn(Optional.of(draftApp));
        when(applicationRepository.save(any(AdmissionApplication.class))).thenAnswer(i -> i.getArgument(0));
        when(documentRepository.findByApplicationId("SC-ADM-2026-00001")).thenReturn(List.of());

        ApplicationReviewRequest request = new ApplicationReviewRequest(ReviewAction.APPROVE, "All credentials verified");
        ApplicationResponse response = admissionService.reviewApplication("admin-1", "SC-ADM-2026-00001", request);

        assertEquals(ApplicationStatus.APPROVED, response.getStatus());
    }

    @Test
    void shouldAllowAdminToRejectApplication() {
        draftApp.setStatus(ApplicationStatus.UNDER_REVIEW);
        when(applicationRepository.findByApplicationId("SC-ADM-2026-00001")).thenReturn(Optional.of(draftApp));
        when(applicationRepository.save(any(AdmissionApplication.class))).thenAnswer(i -> i.getArgument(0));
        when(documentRepository.findByApplicationId("SC-ADM-2026-00001")).thenReturn(List.of());

        ApplicationReviewRequest request = new ApplicationReviewRequest(ReviewAction.REJECT, "Did not meet criteria");
        ApplicationResponse response = admissionService.reviewApplication("admin-1", "SC-ADM-2026-00001", request);

        assertEquals(ApplicationStatus.REJECTED, response.getStatus());
    }

    @Test
    void shouldAllowAdminToRequestChanges() {
        draftApp.setStatus(ApplicationStatus.UNDER_REVIEW);
        when(applicationRepository.findByApplicationId("SC-ADM-2026-00001")).thenReturn(Optional.of(draftApp));
        when(applicationRepository.save(any(AdmissionApplication.class))).thenAnswer(i -> i.getArgument(0));
        when(documentRepository.findByApplicationId("SC-ADM-2026-00001")).thenReturn(List.of());

        ApplicationReviewRequest request = new ApplicationReviewRequest(ReviewAction.REQUEST_CHANGES, "Please re-upload marks card");
        ApplicationResponse response = admissionService.reviewApplication("admin-1", "SC-ADM-2026-00001", request);

        assertEquals(ApplicationStatus.CHANGES_REQUESTED, response.getStatus());
    }

    @Test
    void shouldRejectDirectApprovalOfDraftApplication() {
        draftApp.setStatus(ApplicationStatus.DRAFT);
        when(applicationRepository.findByApplicationId("SC-ADM-2026-00001")).thenReturn(Optional.of(draftApp));

        ApplicationReviewRequest request = new ApplicationReviewRequest(ReviewAction.APPROVE, "Premature approval");

        assertThrows(InvalidApplicationStatusException.class, () ->
                admissionService.reviewApplication("admin-1", "SC-ADM-2026-00001", request));
    }

    @Test
    void shouldRejectModifyingAlreadyApprovedApplication() {
        draftApp.setStatus(ApplicationStatus.APPROVED);
        when(applicationRepository.findByApplicationId("SC-ADM-2026-00001")).thenReturn(Optional.of(draftApp));

        ApplicationReviewRequest request = new ApplicationReviewRequest(ReviewAction.REJECT, "Cannot reject approved");

        assertThrows(InvalidApplicationStatusException.class, () ->
                admissionService.reviewApplication("admin-1", "SC-ADM-2026-00001", request));
    }

    @Test
    void shouldAllowAdminToReviewDocument() {
        Document doc = new Document("SC-ADM-2026-00001", "SC20260001", DocumentType.ID_PROOF, "id.pdf", "/url");
        doc.setId("doc-1");

        when(applicationRepository.findByApplicationId("SC-ADM-2026-00001")).thenReturn(Optional.of(draftApp));
        when(documentRepository.findByIdAndApplicationId("doc-1", "SC-ADM-2026-00001")).thenReturn(Optional.of(doc));
        when(documentRepository.save(any(Document.class))).thenAnswer(i -> i.getArgument(0));

        DocumentReviewRequest request = new DocumentReviewRequest(DocumentStatus.VERIFIED, "ID is valid");
        DocumentResponse response = admissionService.reviewDocument("admin-1", "SC-ADM-2026-00001", "doc-1", request);

        assertEquals(DocumentStatus.VERIFIED, response.getStatus());
        assertEquals("ID is valid", response.getRemarks());
    }

    @Test
    void shouldFilterApplicationsByDepartmentAndStatus() {
        when(applicationRepository.findByDepartmentIgnoreCaseAndStatus("CSE", ApplicationStatus.SUBMITTED))
                .thenReturn(List.of(draftApp));
        when(documentRepository.findByApplicationId("SC-ADM-2026-00001")).thenReturn(List.of());

        List<ApplicationResponse> results = admissionService.listApplications("CSE", ApplicationStatus.SUBMITTED);

        assertEquals(1, results.size());
        assertEquals("SC-ADM-2026-00001", results.get(0).getApplicationId());
    }
}

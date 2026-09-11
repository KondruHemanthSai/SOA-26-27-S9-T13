package com.studentcentral.admission.controller;

import com.studentcentral.admission.dto.*;
import com.studentcentral.admission.model.ApplicationStatus;
import com.studentcentral.admission.security.AuthenticatedUser;
import com.studentcentral.admission.service.AdmissionService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admissions")
public class AdmissionController {

    private static final Logger log = LoggerFactory.getLogger(AdmissionController.class);
    private final AdmissionService admissionService;

    public AdmissionController(AdmissionService admissionService) {
        this.admissionService = admissionService;
    }

    /**
     * Student: Create a new admission application (starts in DRAFT status).
     */
    @PostMapping("/apply")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApplicationResponse> apply(
            @AuthenticationPrincipal AuthenticatedUser user,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @Valid @RequestBody CreateApplicationRequest request) {
        log.info("REST request to create admission application for user: {}", user.getUserId());
        ApplicationResponse response = admissionService.createApplication(user, request, authHeader);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Student: Get own admission application.
     */
    @GetMapping("/my-application")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApplicationResponse> getMyApplication(
            @AuthenticationPrincipal AuthenticatedUser user) {
        log.info("REST request to get own admission application for user: {}", user.getUserId());
        ApplicationResponse response = admissionService.getOwnApplication(user.getUserId());
        return ResponseEntity.ok(response);
    }

    /**
     * Student: Update own draft/changes_requested admission application.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApplicationResponse> updateOwnApplication(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable String id,
            @Valid @RequestBody UpdateApplicationRequest request) {
        log.info("REST request to update application {} by user: {}", id, user.getUserId());
        ApplicationResponse response = admissionService.updateOwnApplication(user.getUserId(), id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Student: Submit application for review (transitions to SUBMITTED).
     */
    @PostMapping("/{id}/submit")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApplicationResponse> submitApplication(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable String id) {
        log.info("REST request to submit application {} by user: {}", id, user.getUserId());
        ApplicationResponse response = admissionService.submitApplication(user.getUserId(), id);
        return ResponseEntity.ok(response);
    }

    /**
     * Student: Upload document metadata for an application.
     */
    @PostMapping("/{id}/documents")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<DocumentResponse> uploadDocument(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable String id,
            @Valid @RequestBody UploadDocumentRequest request) {
        log.info("REST request to upload document for application {} by user: {}", id, user.getUserId());
        DocumentResponse response = admissionService.uploadDocument(user.getUserId(), id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Student or Admin: Retrieve documents for an application.
     */
    @GetMapping("/{id}/documents")
    public ResponseEntity<List<DocumentResponse>> getDocuments(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable String id) {
        log.info("REST request to get documents for application {} by user: {}", id, user.getUserId());
        List<DocumentResponse> responses = admissionService.getDocuments(user, id);
        return ResponseEntity.ok(responses);
    }

    /**
     * Student: Delete document while application is in DRAFT or CHANGES_REQUESTED.
     */
    @DeleteMapping("/{id}/documents/{documentId}")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<Void> deleteDocument(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable String id,
            @PathVariable String documentId) {
        log.info("REST request to delete document {} from application {} by user: {}", documentId, id, user.getUserId());
        admissionService.deleteDocument(user.getUserId(), id, documentId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Admin: List all applications with optional department and status filtering.
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ApplicationResponse>> listApplications(
            @RequestParam(required = false) String department,
            @RequestParam(required = false) ApplicationStatus status) {
        log.info("REST request by admin to list applications (department: {}, status: {})", department, status);
        List<ApplicationResponse> responses = admissionService.listApplications(department, status);
        return ResponseEntity.ok(responses);
    }

    /**
     * Admin: Get application details by ID or applicationId.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApplicationResponse> getApplicationById(
            @PathVariable String id) {
        log.info("REST request by admin to get application {}", id);
        ApplicationResponse response = admissionService.getApplicationById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Admin: Execute review action (START_REVIEW, APPROVE, REJECT, REQUEST_CHANGES).
     */
    @PutMapping("/{id}/review")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApplicationResponse> reviewApplication(
            @AuthenticationPrincipal AuthenticatedUser adminUser,
            @PathVariable String id,
            @Valid @RequestBody ApplicationReviewRequest request) {
        log.info("REST request by admin {} to review application {}: action={}", adminUser.getUserId(), id, request.getAction());
        ApplicationResponse response = admissionService.reviewApplication(adminUser.getUserId(), id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Admin: Review an individual document.
     */
    @PutMapping("/{applicationId}/documents/{documentId}/review")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DocumentResponse> reviewDocument(
            @AuthenticationPrincipal AuthenticatedUser adminUser,
            @PathVariable String applicationId,
            @PathVariable String documentId,
            @Valid @RequestBody DocumentReviewRequest request) {
        log.info("REST request by admin {} to review document {} for application {}: status={}",
                adminUser.getUserId(), documentId, applicationId, request.getStatus());
        DocumentResponse response = admissionService.reviewDocument(adminUser.getUserId(), applicationId, documentId, request);
        return ResponseEntity.ok(response);
    }
}

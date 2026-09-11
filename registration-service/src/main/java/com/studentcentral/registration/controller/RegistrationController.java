package com.studentcentral.registration.controller;

import com.studentcentral.registration.dto.*;
import com.studentcentral.registration.model.RegistrationStatus;
import com.studentcentral.registration.security.AuthenticatedUser;
import com.studentcentral.registration.service.RegistrationService;
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
@RequestMapping("/api/registrations")
public class RegistrationController {

    private static final Logger log = LoggerFactory.getLogger(RegistrationController.class);

    private final RegistrationService registrationService;

    public RegistrationController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    /**
     * Student: Enroll in a course through the validation pipeline.
     */
    @PostMapping
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<RegistrationSuccessResponse> registerCourse(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody CreateRegistrationRequest request) {
        log.info("REST request to register course {} for user {}", request.getCourseId(), user.getUserId());
        RegistrationSuccessResponse response = registrationService.registerCourse(user, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Student: Retrieve own course registrations.
     */
    @GetMapping("/my")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<RegistrationListResponse> getMyRegistrations(
            @AuthenticationPrincipal AuthenticatedUser user) {
        log.info("REST request to get all registrations for user {}", user.getUserId());
        RegistrationListResponse response = registrationService.getMyRegistrations(user);
        return ResponseEntity.ok(response);
    }

    /**
     * Student: Retrieve active (REGISTERED) course enrollments.
     */
    @GetMapping("/my/active")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<RegistrationListResponse> getMyActiveRegistrations(
            @AuthenticationPrincipal AuthenticatedUser user) {
        log.info("REST request to get active registrations for user {}", user.getUserId());
        RegistrationListResponse response = registrationService.getMyActiveRegistrations(user);
        return ResponseEntity.ok(response);
    }

    /**
     * Student: Retrieve complete registration history.
     */
    @GetMapping("/my/history")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<RegistrationListResponse> getMyRegistrationHistory(
            @AuthenticationPrincipal AuthenticatedUser user) {
        log.info("REST request to get registration history for user {}", user.getUserId());
        RegistrationListResponse response = registrationService.getMyRegistrationHistory(user);
        return ResponseEntity.ok(response);
    }

    /**
     * Authenticated: Retrieve a single registration by ID or registrationId.
     */
    @GetMapping("/{registrationId}")
    public ResponseEntity<RegistrationResponse> getRegistrationById(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable String registrationId) {
        log.info("REST request to get registration {} by user {}", registrationId, user.getUserId());
        RegistrationResponse response = registrationService.getRegistrationById(user, registrationId);
        return ResponseEntity.ok(response);
    }

    /**
     * Student: Drop a registered course.
     */
    @DeleteMapping("/{registrationId}")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<RegistrationResponse> dropCourse(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable String registrationId) {
        log.info("REST request to drop course registration {} by user {}", registrationId, user.getUserId());
        RegistrationResponse response = registrationService.dropCourse(user, registrationId);
        return ResponseEntity.ok(response);
    }

    /**
     * Admin: List all registrations with optional query filtering.
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<RegistrationResponse>> listAllRegistrations(
            @RequestParam(required = false) String courseId,
            @RequestParam(required = false) Integer semester,
            @RequestParam(required = false) RegistrationStatus status) {
        log.info("REST request by admin to list all registrations (course={}, sem={}, status={})", courseId, semester, status);
        List<RegistrationResponse> responses = registrationService.listAllRegistrations(courseId, semester, status);
        return ResponseEntity.ok(responses);
    }

    /**
     * Admin: List registrations for a specific student.
     */
    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<RegistrationResponse>> getRegistrationsByStudentId(
            @PathVariable String studentId) {
        log.info("REST request by admin to get registrations for student {}", studentId);
        List<RegistrationResponse> responses = registrationService.getRegistrationsByStudentId(studentId);
        return ResponseEntity.ok(responses);
    }
}

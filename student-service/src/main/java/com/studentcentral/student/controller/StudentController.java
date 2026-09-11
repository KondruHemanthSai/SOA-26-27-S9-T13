package com.studentcentral.student.controller;

import com.studentcentral.student.dto.*;
import com.studentcentral.student.exception.UnauthorizedAccessException;
import com.studentcentral.student.model.AdmissionStatus;
import com.studentcentral.student.security.AuthenticatedUser;
import com.studentcentral.student.service.StudentService;
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
@RequestMapping("/api/students")
public class StudentController {

    private static final Logger log = LoggerFactory.getLogger(StudentController.class);

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    /**
     * Create student profile for the currently authenticated user.
     */
    @PostMapping("/profile")
    public ResponseEntity<StudentResponse> createProfile(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @Valid @RequestBody CreateStudentRequest request
    ) {
        validateUser(currentUser);
        log.info("Creating student profile for userId: {}", currentUser.getUserId());
        StudentResponse response = studentService.createProfile(currentUser, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Retrieve own student profile for the currently authenticated user.
     */
    @GetMapping("/profile")
    public ResponseEntity<StudentResponse> getOwnProfile(
            @AuthenticationPrincipal AuthenticatedUser currentUser
    ) {
        validateUser(currentUser);
        log.debug("Fetching student profile for userId: {}", currentUser.getUserId());
        StudentResponse response = studentService.getOwnProfile(currentUser.getUserId());
        return ResponseEntity.ok(response);
    }

    /**
     * Update own student profile for the currently authenticated user.
     */
    @PutMapping("/profile")
    public ResponseEntity<StudentResponse> updateOwnProfile(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @Valid @RequestBody UpdateStudentRequest request
    ) {
        validateUser(currentUser);
        log.info("Updating student profile for userId: {}", currentUser.getUserId());
        StudentResponse response = studentService.updateOwnProfile(currentUser.getUserId(), request);
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieve own academic record for the currently authenticated user.
     */
    @GetMapping("/profile/academic-record")
    public ResponseEntity<AcademicRecordResponse> getOwnAcademicRecord(
            @AuthenticationPrincipal AuthenticatedUser currentUser
    ) {
        validateUser(currentUser);
        log.debug("Fetching academic record for userId: {}", currentUser.getUserId());
        AcademicRecordResponse response = studentService.getOwnAcademicRecord(currentUser.getUserId());
        return ResponseEntity.ok(response);
    }

    /**
     * Update own academic record for the currently authenticated user.
     */
    @PutMapping("/profile/academic-record")
    public ResponseEntity<AcademicRecordResponse> updateOwnAcademicRecord(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @Valid @RequestBody AcademicRecordRequest request
    ) {
        validateUser(currentUser);
        log.info("Updating academic record for userId: {}", currentUser.getUserId());
        AcademicRecordResponse response = studentService.updateOwnAcademicRecord(currentUser.getUserId(), request);
        return ResponseEntity.ok(response);
    }

    /**
     * Get student by ID, studentId, or userId.
     * Accessible by ADMIN or the owning student.
     */
    @GetMapping("/{id}")
    public ResponseEntity<StudentResponse> getStudentById(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable("id") String id
    ) {
        validateUser(currentUser);
        log.debug("Looking up student with identifier: {} by user: {}", id, currentUser.getUserId());
        StudentResponse response = studentService.getStudentById(currentUser, id);
        return ResponseEntity.ok(response);
    }

    /**
     * List students with optional filtering by department and/or admission status.
     * Admin only endpoint.
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<StudentResponse>> listStudents(
            @RequestParam(value = "department", required = false) String department,
            @RequestParam(value = "status", required = false) AdmissionStatus status
    ) {
        log.debug("Admin listing students with department='{}', status='{}'", department, status);
        List<StudentResponse> response = studentService.listStudents(department, status);
        return ResponseEntity.ok(response);
    }

    private void validateUser(AuthenticatedUser currentUser) {
        if (currentUser == null || currentUser.getUserId() == null) {
            throw new UnauthorizedAccessException("Authentication required");
        }
    }
}

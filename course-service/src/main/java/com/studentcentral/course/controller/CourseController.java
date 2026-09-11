package com.studentcentral.course.controller;

import com.studentcentral.course.dto.*;
import com.studentcentral.course.model.CourseStatus;
import com.studentcentral.course.model.CourseType;
import com.studentcentral.course.security.AuthenticatedUser;
import com.studentcentral.course.service.CourseService;
import com.studentcentral.course.service.PrerequisiteService;
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
@RequestMapping("/api/courses")
public class CourseController {

    private static final Logger log = LoggerFactory.getLogger(CourseController.class);

    private final CourseService courseService;
    private final PrerequisiteService prerequisiteService;

    public CourseController(CourseService courseService, PrerequisiteService prerequisiteService) {
        this.courseService = courseService;
        this.prerequisiteService = prerequisiteService;
    }

    /**
     * Admin: Create a new course.
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CourseResponse> createCourse(
            @Valid @RequestBody CreateCourseRequest request) {
        log.info("REST request to create course: {}", request.getCourseCode());
        CourseResponse response = courseService.createCourse(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Authenticated: List courses with search and filtering options.
     */
    @GetMapping
    public ResponseEntity<List<CourseResponse>> listCourses(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) Integer semester,
            @RequestParam(required = false) CourseType courseType,
            @RequestParam(required = false) CourseStatus status,
            @RequestParam(required = false) Boolean available,
            @AuthenticationPrincipal AuthenticatedUser user) {
        log.info("REST request to list courses (search={}, dept={}, sem={}, type={}, status={}, avail={})",
                search, department, semester, courseType, status, available);
        List<CourseResponse> responses = courseService.listCourses(
                search, department, semester, courseType, status, available, user);
        return ResponseEntity.ok(responses);
    }

    /**
     * Authenticated: Get course details and prerequisites by ID or courseCode.
     */
    @GetMapping("/{courseId}")
    public ResponseEntity<CourseResponse> getCourse(
            @PathVariable String courseId) {
        log.info("REST request to get course: {}", courseId);
        CourseResponse response = courseService.getCourse(courseId);
        return ResponseEntity.ok(response);
    }

    /**
     * Admin: Update course details and capacity.
     */
    @PutMapping("/{courseId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CourseResponse> updateCourse(
            @PathVariable String courseId,
            @Valid @RequestBody UpdateCourseRequest request) {
        log.info("REST request to update course: {}", courseId);
        CourseResponse response = courseService.updateCourse(courseId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Admin: Deactivate / soft-delete course.
     */
    @DeleteMapping("/{courseId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCourse(
            @PathVariable String courseId) {
        log.info("REST request to deactivate course: {}", courseId);
        courseService.deactivateCourse(courseId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Admin: Explicitly activate a course.
     */
    @PutMapping("/{courseId}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CourseResponse> activateCourse(
            @PathVariable String courseId) {
        log.info("REST request to activate course: {}", courseId);
        CourseResponse response = courseService.activateCourse(courseId);
        return ResponseEntity.ok(response);
    }

    /**
     * Admin: Explicitly deactivate a course.
     */
    @PutMapping("/{courseId}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CourseResponse> deactivateCourse(
            @PathVariable String courseId) {
        log.info("REST request to deactivate course: {}", courseId);
        CourseResponse response = courseService.deactivateCourse(courseId);
        return ResponseEntity.ok(response);
    }

    /**
     * Authenticated: Check seat availability for a course.
     */
    @GetMapping("/{courseId}/availability")
    public ResponseEntity<CourseAvailabilityResponse> getAvailability(
            @PathVariable String courseId) {
        log.info("REST request to get availability for course: {}", courseId);
        CourseAvailabilityResponse response = courseService.getAvailability(courseId);
        return ResponseEntity.ok(response);
    }

    /**
     * Authenticated: Get prerequisites list for a course.
     */
    @GetMapping("/{courseId}/prerequisites")
    public ResponseEntity<PrerequisiteResponse> getPrerequisites(
            @PathVariable String courseId) {
        log.info("REST request to get prerequisites for course: {}", courseId);
        PrerequisiteResponse response = prerequisiteService.getPrerequisites(courseId);
        return ResponseEntity.ok(response);
    }

    /**
     * Admin: Add prerequisite to a course.
     */
    @PostMapping("/{courseId}/prerequisites")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PrerequisiteResponse> addPrerequisite(
            @PathVariable String courseId,
            @Valid @RequestBody AddPrerequisiteRequest request) {
        log.info("REST request to add prerequisite {} to course {}", request.getPrerequisiteCourseId(), courseId);
        PrerequisiteResponse response = prerequisiteService.addPrerequisite(courseId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Admin: Delete prerequisite from a course.
     */
    @DeleteMapping("/{courseId}/prerequisites/{prerequisiteId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deletePrerequisite(
            @PathVariable String courseId,
            @PathVariable String prerequisiteId) {
        log.info("REST request to delete prerequisite {} from course {}", prerequisiteId, courseId);
        prerequisiteService.deletePrerequisite(courseId, prerequisiteId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Internal/Service: Reserve a seat in a course.
     */
    @PostMapping("/{courseId}/reserve-seat")
    public ResponseEntity<CourseAvailabilityResponse> reserveSeat(
            @PathVariable String courseId) {
        log.info("REST request to reserve seat for course: {}", courseId);
        courseService.reserveSeat(courseId);
        CourseAvailabilityResponse response = courseService.getAvailability(courseId);
        return ResponseEntity.ok(response);
    }

    /**
     * Internal/Service: Release a seat in a course.
     */
    @PostMapping("/{courseId}/release-seat")
    public ResponseEntity<CourseAvailabilityResponse> releaseSeat(
            @PathVariable String courseId) {
        log.info("REST request to release seat for course: {}", courseId);
        courseService.releaseSeat(courseId);
        CourseAvailabilityResponse response = courseService.getAvailability(courseId);
        return ResponseEntity.ok(response);
    }
}


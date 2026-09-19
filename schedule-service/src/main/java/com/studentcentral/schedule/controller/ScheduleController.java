package com.studentcentral.schedule.controller;

import com.studentcentral.schedule.dto.*;
import com.studentcentral.schedule.model.DayOfWeek;
import com.studentcentral.schedule.security.AuthenticatedUser;
import com.studentcentral.schedule.service.ScheduleService;
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
@RequestMapping("/api/schedules")
public class ScheduleController {

    private static final Logger log = LoggerFactory.getLogger(ScheduleController.class);

    private final ScheduleService scheduleService;

    public ScheduleController(ScheduleService scheduleService) {
        this.scheduleService = scheduleService;
    }

    /**
     * Admin: Create a new timetable schedule slot.
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ScheduleResponse> createSchedule(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody CreateScheduleRequest request) {
        log.info("REST request by admin {} to create schedule for course {}", user.getUserId(), request.getCourseId());
        ScheduleResponse response = scheduleService.createSchedule(user, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Admin: Update an existing timetable schedule slot.
     */
    @PutMapping("/{scheduleId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ScheduleResponse> updateSchedule(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable String scheduleId,
            @RequestBody UpdateScheduleRequest request) {
        log.info("REST request by admin {} to update schedule {}", user.getUserId(), scheduleId);
        ScheduleResponse response = scheduleService.updateSchedule(user, scheduleId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Admin: Delete an existing timetable schedule slot.
     */
    @DeleteMapping("/{scheduleId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteSchedule(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable String scheduleId) {
        log.info("REST request by admin {} to delete schedule {}", user.getUserId(), scheduleId);
        scheduleService.deleteSchedule(user, scheduleId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Authenticated (Admin / Student): Retrieve a single schedule by ID.
     */
    @GetMapping("/{scheduleId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ScheduleResponse> getScheduleById(@PathVariable String scheduleId) {
        log.info("REST request to get schedule by ID: {}", scheduleId);
        ScheduleResponse response = scheduleService.getScheduleById(scheduleId);
        return ResponseEntity.ok(response);
    }

    /**
     * Authenticated (Admin / Student): Retrieve timetable slots for a specific course.
     */
    @GetMapping("/course/{courseId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ScheduleResponse>> getSchedulesByCourse(@PathVariable String courseId) {
        log.info("REST request to get timetable for course: {}", courseId);
        List<ScheduleResponse> responses = scheduleService.getSchedulesByCourse(courseId);
        return ResponseEntity.ok(responses);
    }

    /**
     * Authenticated (Admin / Student): List / filter timetable entries.
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ScheduleResponse>> listSchedules(
            @RequestParam(required = false) DayOfWeek dayOfWeek,
            @RequestParam(required = false) String courseId,
            @RequestParam(required = false) Integer semester,
            @RequestParam(required = false) String academicYear,
            @RequestParam(required = false) String classroom,
            @RequestParam(required = false) String faculty) {
        log.info("REST request to list schedules (day={}, courseId={}, sem={}, year={}, room={}, faculty={})",
                dayOfWeek, courseId, semester, academicYear, classroom, faculty);
        List<ScheduleResponse> responses = scheduleService.listSchedules(dayOfWeek, courseId, semester, academicYear, classroom, faculty);
        return ResponseEntity.ok(responses);
    }

    /**
     * Student: Retrieve personalized weekly timetable based on active course enrollments.
     */
    @GetMapping("/my")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<List<ScheduleResponse>> getMyTimetable(@AuthenticationPrincipal AuthenticatedUser user) {
        log.info("REST request to get personalized timetable for student {}", user.getUserId());
        List<ScheduleResponse> responses = scheduleService.getMyTimetable(user);
        return ResponseEntity.ok(responses);
    }

    /**
     * Internal Service / Authenticated: Validate if requested course creates timetable overlap for student.
     */
    @PostMapping("/check-conflict")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ScheduleConflictResponse> checkConflict(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody ScheduleConflictRequest request) {
        String token = user != null ? user.getToken() : null;
        log.info("REST request to check timetable conflict for student {} and course {}", request.getStudentId(), request.getCourseId());
        ScheduleConflictResponse response = scheduleService.checkStudentScheduleConflict(request, token);
        return ResponseEntity.ok(response);
    }
}

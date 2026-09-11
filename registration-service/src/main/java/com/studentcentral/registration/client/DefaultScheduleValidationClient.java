package com.studentcentral.registration.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Default adapter implementation for Schedule Validation.
 * Validates timetable sanity and acts as the extension point for the Schedule Microservice in Phase 7.
 */
@Component
public class DefaultScheduleValidationClient implements ScheduleValidationClient {

    private static final Logger log = LoggerFactory.getLogger(DefaultScheduleValidationClient.class);

    @Override
    public boolean hasScheduleConflict(String studentId, String courseId, Integer semester) {
        log.debug("Checking schedule conflict for student {} and course {} (semester {})", studentId, courseId, semester);
        // Default validation allows registration; Phase 7 will query schedule-service for real timetable slots
        return false;
    }
}

package com.studentcentral.registration.client;

import com.studentcentral.registration.client.dto.ScheduleConflictResult;

/**
 * Interface abstraction for timetable and schedule conflict validation.
 * Integrates directly with the Schedule Microservice in Phase 7.
 */
public interface ScheduleValidationClient {

    /**
     * Checks if enrolling in the course creates any timetable slot overlap for the student.
     *
     * @param studentId Student identifier
     * @param courseId  Course identifier
     * @param semester  Semester number
     * @param jwtToken  Authentication token
     * @return ScheduleConflictResult with conflict flag and conflicting course metadata
     */
    ScheduleConflictResult checkScheduleConflict(String studentId, String courseId, Integer semester, String jwtToken);

    /**
     * Default convenience check for backwards compatibility.
     */
    default boolean hasScheduleConflict(String studentId, String courseId, Integer semester) {
        return checkScheduleConflict(studentId, courseId, semester, null).isConflict();
    }
}

package com.studentcentral.registration.client;

/**
 * Interface abstraction for timetable and schedule conflict validation.
 * Allows clean plugging of Schedule Service in Phase 7 without altering the registration engine.
 */
public interface ScheduleValidationClient {

    /**
     * Checks if enrolling in the course creates any timetable slot overlap for the student.
     *
     * @param studentId Student identifier
     * @param courseId  Course identifier
     * @param semester  Semester number
     * @return true if there is a conflict, false if the schedule is clear
     */
    boolean hasScheduleConflict(String studentId, String courseId, Integer semester);
}

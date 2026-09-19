package com.studentcentral.registration.client.dto;

public class ScheduleConflictResult {

    private final boolean conflict;
    private final ConflictingCourseDto conflictingCourse;

    public ScheduleConflictResult(boolean conflict, ConflictingCourseDto conflictingCourse) {
        this.conflict = conflict;
        this.conflictingCourse = conflictingCourse;
    }

    public boolean isConflict() {
        return conflict;
    }

    public ConflictingCourseDto getConflictingCourse() {
        return conflictingCourse;
    }
}

package com.studentcentral.registration.client.dto;

public class ScheduleConflictCheckResponse {

    private boolean conflict;
    private ConflictingCourseDto conflictingCourse;

    public ScheduleConflictCheckResponse() {
    }

    public ScheduleConflictCheckResponse(boolean conflict, ConflictingCourseDto conflictingCourse) {
        this.conflict = conflict;
        this.conflictingCourse = conflictingCourse;
    }

    public boolean isConflict() {
        return conflict;
    }

    public void setConflict(boolean conflict) {
        this.conflict = conflict;
    }

    public ConflictingCourseDto getConflictingCourse() {
        return conflictingCourse;
    }

    public void setConflictingCourse(ConflictingCourseDto conflictingCourse) {
        this.conflictingCourse = conflictingCourse;
    }
}

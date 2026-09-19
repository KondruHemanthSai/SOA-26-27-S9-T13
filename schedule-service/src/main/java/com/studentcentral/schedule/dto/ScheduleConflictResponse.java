package com.studentcentral.schedule.dto;

public class ScheduleConflictResponse {

    private boolean conflict;
    private ConflictingCourseResponse conflictingCourse;

    public ScheduleConflictResponse() {
    }

    public ScheduleConflictResponse(boolean conflict, ConflictingCourseResponse conflictingCourse) {
        this.conflict = conflict;
        this.conflictingCourse = conflictingCourse;
    }

    public boolean isConflict() {
        return conflict;
    }

    public void setConflict(boolean conflict) {
        this.conflict = conflict;
    }

    public ConflictingCourseResponse getConflictingCourse() {
        return conflictingCourse;
    }

    public void setConflictingCourse(ConflictingCourseResponse conflictingCourse) {
        this.conflictingCourse = conflictingCourse;
    }
}

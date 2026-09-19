package com.studentcentral.schedule.dto;

import jakarta.validation.constraints.NotBlank;

public class ScheduleConflictRequest {

    @NotBlank(message = "Student ID is required")
    private String studentId;

    @NotBlank(message = "Course ID is required")
    private String courseId;

    public ScheduleConflictRequest() {
    }

    public ScheduleConflictRequest(String studentId, String courseId) {
        this.studentId = studentId;
        this.courseId = courseId;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }
}

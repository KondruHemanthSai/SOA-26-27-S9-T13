package com.studentcentral.registration.dto;

import jakarta.validation.constraints.NotBlank;

public class CreateRegistrationRequest {

    @NotBlank(message = "Course identifier (ID or Course Code) is required")
    private String courseId;

    private Integer semester;

    private String academicYear;

    public CreateRegistrationRequest() {
    }

    public CreateRegistrationRequest(String courseId) {
        this.courseId = courseId;
    }

    public CreateRegistrationRequest(String courseId, Integer semester, String academicYear) {
        this.courseId = courseId;
        this.semester = semester;
        this.academicYear = academicYear;
    }

    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    public Integer getSemester() {
        return semester;
    }

    public void setSemester(Integer semester) {
        this.semester = semester;
    }

    public String getAcademicYear() {
        return academicYear;
    }

    public void setAcademicYear(String academicYear) {
        this.academicYear = academicYear;
    }
}

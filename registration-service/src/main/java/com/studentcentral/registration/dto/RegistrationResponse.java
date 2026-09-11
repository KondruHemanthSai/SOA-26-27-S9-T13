package com.studentcentral.registration.dto;

import com.studentcentral.registration.model.Registration;
import com.studentcentral.registration.model.RegistrationStatus;

import java.time.Instant;

public class RegistrationResponse {

    private String id;
    private String registrationId;
    private String studentId;
    private String courseId;
    private String courseCode;
    private String courseName;
    private Integer semester;
    private String academicYear;
    private Integer credits;
    private RegistrationStatus status;
    private Instant registeredAt;
    private Instant updatedAt;

    public RegistrationResponse() {
    }

    public RegistrationResponse(String id, String registrationId, String studentId, String courseId, String courseCode, String courseName, Integer semester, String academicYear, Integer credits, RegistrationStatus status, Instant registeredAt, Instant updatedAt) {
        this.id = id;
        this.registrationId = registrationId;
        this.studentId = studentId;
        this.courseId = courseId;
        this.courseCode = courseCode;
        this.courseName = courseName;
        this.semester = semester;
        this.academicYear = academicYear;
        this.credits = credits;
        this.status = status;
        this.registeredAt = registeredAt;
        this.updatedAt = updatedAt;
    }

    public static RegistrationResponse fromModel(Registration registration) {
        if (registration == null) {
            return null;
        }
        return new RegistrationResponse(
                registration.getId(),
                registration.getRegistrationId(),
                registration.getStudentId(),
                registration.getCourseId(),
                registration.getCourseCode(),
                registration.getCourseName(),
                registration.getSemester(),
                registration.getAcademicYear(),
                registration.getCredits(),
                registration.getStatus(),
                registration.getRegisteredAt(),
                registration.getUpdatedAt()
        );
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRegistrationId() {
        return registrationId;
    }

    public void setRegistrationId(String registrationId) {
        this.registrationId = registrationId;
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

    public String getCourseCode() {
        return courseCode;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
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

    public Integer getCredits() {
        return credits;
    }

    public void setCredits(Integer credits) {
        this.credits = credits;
    }

    public RegistrationStatus getStatus() {
        return status;
    }

    public void setStatus(RegistrationStatus status) {
        this.status = status;
    }

    public Instant getRegisteredAt() {
        return registeredAt;
    }

    public void setRegisteredAt(Instant registeredAt) {
        this.registeredAt = registeredAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}

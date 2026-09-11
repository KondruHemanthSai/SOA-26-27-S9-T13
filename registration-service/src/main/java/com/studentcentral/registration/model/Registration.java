package com.studentcentral.registration.model;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/**
 * MongoDB document representing a student's course enrollment.
 * Collection: registrations
 * Database: student_central_registration
 */
@Document(collection = "registrations")
public class Registration {

    @Id
    private String id;

    @Indexed(unique = true)
    private String registrationId;

    @Indexed
    private String studentId;

    @Indexed
    private String userId;

    @Indexed
    private String courseId;

    private String courseCode;
    private String courseName;

    @Indexed
    private Integer semester;

    @Indexed
    private String academicYear;

    private Integer credits;

    @Indexed
    private RegistrationStatus status = RegistrationStatus.REGISTERED;

    @CreatedDate
    private Instant registeredAt;

    @LastModifiedDate
    private Instant updatedAt;

    public Registration() {
    }

    public Registration(String registrationId, String studentId, String userId, String courseId, String courseCode, String courseName, Integer semester, String academicYear, Integer credits) {
        this.registrationId = registrationId;
        this.studentId = studentId;
        this.userId = userId;
        this.courseId = courseId;
        this.courseCode = courseCode != null ? courseCode.toUpperCase().trim() : null;
        this.courseName = courseName;
        this.semester = semester;
        this.academicYear = academicYear != null ? academicYear.trim() : "2026-27";
        this.credits = credits;
        this.status = RegistrationStatus.REGISTERED;
        this.registeredAt = Instant.now();
        this.updatedAt = Instant.now();
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

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
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
        this.courseCode = courseCode != null ? courseCode.toUpperCase().trim() : null;
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
        this.academicYear = academicYear != null ? academicYear.trim() : "2026-27";
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

    @Override
    public String toString() {
        return "Registration{" +
                "id='" + id + '\'' +
                ", registrationId='" + registrationId + '\'' +
                ", studentId='" + studentId + '\'' +
                ", courseCode='" + courseCode + '\'' +
                ", semester=" + semester +
                ", credits=" + credits +
                ", status=" + status +
                '}';
    }
}

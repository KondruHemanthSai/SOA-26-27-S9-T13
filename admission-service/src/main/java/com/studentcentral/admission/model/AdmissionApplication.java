package com.studentcentral.admission.model;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.time.LocalDate;

/**
 * MongoDB document representing an admission application.
 * Collection: applications
 * Database: student_central_admission
 */
@Document(collection = "applications")
public class AdmissionApplication {

    @Id
    private String id;

    @Indexed(unique = true)
    private String applicationId;

    @Indexed
    private String studentId;

    @Indexed(unique = true)
    private String userId;

    private String program;
    private String department;
    private AcademicDetails academicDetails;

    private LocalDate applicationDate;

    private ApplicationStatus status = ApplicationStatus.DRAFT;

    private Instant submissionDate;
    private Instant reviewDate;
    private String reviewedBy;
    private String remarks;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    public AdmissionApplication() {
    }

    public AdmissionApplication(String applicationId, String studentId, String userId, String program, String department, AcademicDetails academicDetails) {
        this.applicationId = applicationId;
        this.studentId = studentId;
        this.userId = userId;
        this.program = program;
        this.department = department;
        this.academicDetails = academicDetails;
        this.applicationDate = LocalDate.now();
        this.status = ApplicationStatus.DRAFT;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
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

    public String getProgram() {
        return program;
    }

    public void setProgram(String program) {
        this.program = program;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public AcademicDetails getAcademicDetails() {
        return academicDetails;
    }

    public void setAcademicDetails(AcademicDetails academicDetails) {
        this.academicDetails = academicDetails;
    }

    public LocalDate getApplicationDate() {
        return applicationDate;
    }

    public void setApplicationDate(LocalDate applicationDate) {
        this.applicationDate = applicationDate;
    }

    public ApplicationStatus getStatus() {
        return status;
    }

    public void setStatus(ApplicationStatus status) {
        this.status = status;
    }

    public Instant getSubmissionDate() {
        return submissionDate;
    }

    public void setSubmissionDate(Instant submissionDate) {
        this.submissionDate = submissionDate;
    }

    public Instant getReviewDate() {
        return reviewDate;
    }

    public void setReviewDate(Instant reviewDate) {
        this.reviewDate = reviewDate;
    }

    public String getReviewedBy() {
        return reviewedBy;
    }

    public void setReviewedBy(String reviewedBy) {
        this.reviewedBy = reviewedBy;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "AdmissionApplication{" +
                "id='" + id + '\'' +
                ", applicationId='" + applicationId + '\'' +
                ", studentId='" + studentId + '\'' +
                ", userId='" + userId + '\'' +
                ", program='" + program + '\'' +
                ", department='" + department + '\'' +
                ", status=" + status +
                ", applicationDate=" + applicationDate +
                '}';
    }
}

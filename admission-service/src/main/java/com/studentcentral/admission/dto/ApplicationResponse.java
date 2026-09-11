package com.studentcentral.admission.dto;

import com.studentcentral.admission.model.AdmissionApplication;
import com.studentcentral.admission.model.ApplicationStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public class ApplicationResponse {

    private String id;
    private String applicationId;
    private String studentId;
    private String userId;
    private String program;
    private String department;
    private AcademicDetailsResponse academicDetails;
    private LocalDate applicationDate;
    private ApplicationStatus status;
    private Instant submissionDate;
    private Instant reviewDate;
    private String reviewedBy;
    private String remarks;
    private List<DocumentResponse> documents;
    private Instant createdAt;
    private Instant updatedAt;

    public ApplicationResponse() {
    }

    public ApplicationResponse(String id, String applicationId, String studentId, String userId, String program, String department, AcademicDetailsResponse academicDetails, LocalDate applicationDate, ApplicationStatus status, Instant submissionDate, Instant reviewDate, String reviewedBy, String remarks, List<DocumentResponse> documents, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.applicationId = applicationId;
        this.studentId = studentId;
        this.userId = userId;
        this.program = program;
        this.department = department;
        this.academicDetails = academicDetails;
        this.applicationDate = applicationDate;
        this.status = status;
        this.submissionDate = submissionDate;
        this.reviewDate = reviewDate;
        this.reviewedBy = reviewedBy;
        this.remarks = remarks;
        this.documents = documents;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static ApplicationResponse fromModel(AdmissionApplication app, List<DocumentResponse> documents) {
        if (app == null) {
            return null;
        }
        return new ApplicationResponse(
                app.getId(),
                app.getApplicationId(),
                app.getStudentId(),
                app.getUserId(),
                app.getProgram(),
                app.getDepartment(),
                AcademicDetailsResponse.fromModel(app.getAcademicDetails()),
                app.getApplicationDate(),
                app.getStatus(),
                app.getSubmissionDate(),
                app.getReviewDate(),
                app.getReviewedBy(),
                app.getRemarks(),
                documents,
                app.getCreatedAt(),
                app.getUpdatedAt()
        );
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

    public AcademicDetailsResponse getAcademicDetails() {
        return academicDetails;
    }

    public void setAcademicDetails(AcademicDetailsResponse academicDetails) {
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

    public List<DocumentResponse> getDocuments() {
        return documents;
    }

    public void setDocuments(List<DocumentResponse> documents) {
        this.documents = documents;
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
}

package com.studentcentral.admission.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.Instant;

/**
 * MongoDB document representing an uploaded admission verification document.
 * Collection: documents
 * Database: student_central_admission
 */
@org.springframework.data.mongodb.core.mapping.Document(collection = "documents")
public class Document {

    @Id
    private String id;

    @Indexed
    private String applicationId;

    @Indexed
    private String studentId;

    private DocumentType type;
    private String fileName;
    private String fileUrl;
    private Instant uploadedAt;
    private DocumentStatus status = DocumentStatus.UPLOADED;
    private String remarks;
    private String reviewedBy;
    private Instant reviewedAt;

    public Document() {
    }

    public Document(String applicationId, String studentId, DocumentType type, String fileName, String fileUrl) {
        this.applicationId = applicationId;
        this.studentId = studentId;
        this.type = type;
        this.fileName = fileName;
        this.fileUrl = fileUrl;
        this.uploadedAt = Instant.now();
        this.status = DocumentStatus.UPLOADED;
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

    public DocumentType getType() {
        return type;
    }

    public void setType(DocumentType type) {
        this.type = type;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public Instant getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(Instant uploadedAt) {
        this.uploadedAt = uploadedAt;
    }

    public DocumentStatus getStatus() {
        return status;
    }

    public void setStatus(DocumentStatus status) {
        this.status = status;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getReviewedBy() {
        return reviewedBy;
    }

    public void setReviewedBy(String reviewedBy) {
        this.reviewedBy = reviewedBy;
    }

    public Instant getReviewedAt() {
        return reviewedAt;
    }

    public void setReviewedAt(Instant reviewedAt) {
        this.reviewedAt = reviewedAt;
    }

    @Override
    public String toString() {
        return "Document{" +
                "id='" + id + '\'' +
                ", applicationId='" + applicationId + '\'' +
                ", studentId='" + studentId + '\'' +
                ", type=" + type +
                ", fileName='" + fileName + '\'' +
                ", status=" + status +
                '}';
    }
}

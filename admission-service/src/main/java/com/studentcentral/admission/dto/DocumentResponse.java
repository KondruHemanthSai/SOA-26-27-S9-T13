package com.studentcentral.admission.dto;

import com.studentcentral.admission.model.Document;
import com.studentcentral.admission.model.DocumentStatus;
import com.studentcentral.admission.model.DocumentType;

import java.time.Instant;

public class DocumentResponse {

    private String id;
    private String applicationId;
    private String studentId;
    private DocumentType type;
    private String fileName;
    private String fileUrl;
    private Instant uploadedAt;
    private DocumentStatus status;
    private String remarks;

    public DocumentResponse() {
    }

    public DocumentResponse(String id, String applicationId, String studentId, DocumentType type, String fileName, String fileUrl, Instant uploadedAt, DocumentStatus status, String remarks) {
        this.id = id;
        this.applicationId = applicationId;
        this.studentId = studentId;
        this.type = type;
        this.fileName = fileName;
        this.fileUrl = fileUrl;
        this.uploadedAt = uploadedAt;
        this.status = status;
        this.remarks = remarks;
    }

    public static DocumentResponse fromModel(Document doc) {
        if (doc == null) {
            return null;
        }
        return new DocumentResponse(
                doc.getId(),
                doc.getApplicationId(),
                doc.getStudentId(),
                doc.getType(),
                doc.getFileName(),
                doc.getFileUrl(),
                doc.getUploadedAt(),
                doc.getStatus(),
                doc.getRemarks()
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
}

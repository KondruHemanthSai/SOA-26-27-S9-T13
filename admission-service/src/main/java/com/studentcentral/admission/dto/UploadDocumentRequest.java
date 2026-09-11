package com.studentcentral.admission.dto;

import com.studentcentral.admission.model.DocumentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class UploadDocumentRequest {

    @NotNull(message = "Document type is required")
    private DocumentType type;

    @NotBlank(message = "File name is required")
    @Size(max = 255, message = "File name cannot exceed 255 characters")
    private String fileName;

    @Size(max = 500, message = "File URL cannot exceed 500 characters")
    private String fileUrl;

    public UploadDocumentRequest() {
    }

    public UploadDocumentRequest(DocumentType type, String fileName, String fileUrl) {
        this.type = type;
        this.fileName = fileName;
        this.fileUrl = fileUrl;
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
}

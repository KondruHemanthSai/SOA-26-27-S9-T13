package com.studentcentral.admission.dto;

import com.studentcentral.admission.model.DocumentStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class DocumentReviewRequest {

    @NotNull(message = "Document status is required")
    private DocumentStatus status;

    @Size(max = 500, message = "Remarks cannot exceed 500 characters")
    private String remarks;

    public DocumentReviewRequest() {
    }

    public DocumentReviewRequest(DocumentStatus status, String remarks) {
        this.status = status;
        this.remarks = remarks;
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

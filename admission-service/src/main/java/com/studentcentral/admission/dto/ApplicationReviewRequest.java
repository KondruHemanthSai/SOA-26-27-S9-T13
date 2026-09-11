package com.studentcentral.admission.dto;

import com.studentcentral.admission.model.ReviewAction;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class ApplicationReviewRequest {

    @NotNull(message = "Review action is required")
    private ReviewAction action;

    @Size(max = 1000, message = "Remarks cannot exceed 1000 characters")
    private String remarks;

    public ApplicationReviewRequest() {
    }

    public ApplicationReviewRequest(ReviewAction action, String remarks) {
        this.action = action;
        this.remarks = remarks;
    }

    public ReviewAction getAction() {
        return action;
    }

    public void setAction(ReviewAction action) {
        this.action = action;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
}

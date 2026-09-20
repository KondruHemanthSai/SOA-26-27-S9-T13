package com.studentcentral.ai.model;

public class AiInsightItem {

    private String type; // COURSE_LOAD, SCHEDULE_PRESSURE, PROFILE_INCOMPLETE, etc.
    private String severity; // LOW, MEDIUM, HIGH
    private String title;
    private String message;
    private String suggestedAction;

    public AiInsightItem() {
    }

    public AiInsightItem(String type, String severity, String title, String message, String suggestedAction) {
        this.type = type;
        this.severity = severity;
        this.title = title;
        this.message = message;
        this.suggestedAction = suggestedAction;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSuggestedAction() {
        return suggestedAction;
    }

    public void setSuggestedAction(String suggestedAction) {
        this.suggestedAction = suggestedAction;
    }
}

package com.studentcentral.ai.model;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "ai_audit_logs")
public class AiAuditLog {

    @Id
    private String id;

    @Indexed
    private String userId;

    private String feature;
    private List<String> toolCalls = new ArrayList<>();
    private boolean success;
    private String provider;
    private Long latencyMs;
    private String errorMessage;

    @CreatedDate
    private Instant timestamp;

    public AiAuditLog() {
        this.timestamp = Instant.now();
    }

    public AiAuditLog(String userId, String feature, List<String> toolCalls, boolean success,
                      String provider, Long latencyMs, String errorMessage) {
        this.userId = userId;
        this.feature = feature;
        if (toolCalls != null) this.toolCalls = toolCalls;
        this.success = success;
        this.provider = provider;
        this.latencyMs = latencyMs;
        this.errorMessage = errorMessage;
        this.timestamp = Instant.now();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getFeature() {
        return feature;
    }

    public void setFeature(String feature) {
        this.feature = feature;
    }

    public List<String> getToolCalls() {
        return toolCalls;
    }

    public void setToolCalls(List<String> toolCalls) {
        this.toolCalls = toolCalls;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public Long getLatencyMs() {
        return latencyMs;
    }

    public void setLatencyMs(Long latencyMs) {
        this.latencyMs = latencyMs;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
}

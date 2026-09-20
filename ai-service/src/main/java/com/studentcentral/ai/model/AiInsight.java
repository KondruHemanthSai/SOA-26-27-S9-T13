package com.studentcentral.ai.model;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "ai_insights")
public class AiInsight {

    @Id
    private String id;

    @Indexed(unique = true)
    private String insightId;

    @Indexed
    private String userId;

    private String studentId;

    private List<AiInsightItem> insights = new ArrayList<>();

    @CreatedDate
    private Instant evaluatedAt;

    public AiInsight() {
        this.evaluatedAt = Instant.now();
    }

    public AiInsight(String insightId, String userId, String studentId, List<AiInsightItem> insights) {
        this.insightId = insightId;
        this.userId = userId;
        this.studentId = studentId;
        this.insights = insights != null ? insights : new ArrayList<>();
        this.evaluatedAt = Instant.now();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getInsightId() {
        return insightId;
    }

    public void setInsightId(String insightId) {
        this.insightId = insightId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public List<AiInsightItem> getInsights() {
        return insights;
    }

    public void setInsights(List<AiInsightItem> insights) {
        this.insights = insights;
    }

    public Instant getEvaluatedAt() {
        return evaluatedAt;
    }

    public void setEvaluatedAt(Instant evaluatedAt) {
        this.evaluatedAt = evaluatedAt;
    }
}

package com.studentcentral.ai.model;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "ai_recommendations")
public class AiRecommendation {

    @Id
    private String id;

    @Indexed(unique = true)
    private String recommendationId;

    @Indexed
    private String userId;

    private String studentId;

    private List<CourseRecommendationItem> recommendations = new ArrayList<>();

    @CreatedDate
    private Instant generatedAt;

    public AiRecommendation() {
        this.generatedAt = Instant.now();
    }

    public AiRecommendation(String recommendationId, String userId, String studentId, List<CourseRecommendationItem> recommendations) {
        this.recommendationId = recommendationId;
        this.userId = userId;
        this.studentId = studentId;
        this.recommendations = recommendations != null ? recommendations : new ArrayList<>();
        this.generatedAt = Instant.now();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRecommendationId() {
        return recommendationId;
    }

    public void setRecommendationId(String recommendationId) {
        this.recommendationId = recommendationId;
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

    public List<CourseRecommendationItem> getRecommendations() {
        return recommendations;
    }

    public void setRecommendations(List<CourseRecommendationItem> recommendations) {
        this.recommendations = recommendations;
    }

    public Instant getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(Instant generatedAt) {
        this.generatedAt = generatedAt;
    }
}

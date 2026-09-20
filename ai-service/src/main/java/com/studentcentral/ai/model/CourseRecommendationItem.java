package com.studentcentral.ai.model;

public class CourseRecommendationItem {

    private String courseId;
    private String courseCode;
    private String courseName;
    private Integer credits;
    private Integer availableSeats;
    private String department;
    private String reason;
    private Double confidence;

    public CourseRecommendationItem() {
    }

    public CourseRecommendationItem(String courseId, String courseCode, String courseName, Integer credits,
                                  Integer availableSeats, String department, String reason, Double confidence) {
        this.courseId = courseId;
        this.courseCode = courseCode;
        this.courseName = courseName;
        this.credits = credits;
        this.availableSeats = availableSeats;
        this.department = department;
        this.reason = reason;
        this.confidence = confidence;
    }

    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public Integer getCredits() {
        return credits;
    }

    public void setCredits(Integer credits) {
        this.credits = credits;
    }

    public Integer getAvailableSeats() {
        return availableSeats;
    }

    public void setAvailableSeats(Integer availableSeats) {
        this.availableSeats = availableSeats;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Double getConfidence() {
        return confidence;
    }

    public void setConfidence(Double confidence) {
        this.confidence = confidence;
    }
}

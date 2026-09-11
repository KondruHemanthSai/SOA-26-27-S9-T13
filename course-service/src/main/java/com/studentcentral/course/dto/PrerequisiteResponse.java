package com.studentcentral.course.dto;

import java.util.List;

public class PrerequisiteResponse {

    private String courseId;
    private String courseCode;
    private List<PrerequisiteItemResponse> prerequisites;

    public PrerequisiteResponse() {
    }

    public PrerequisiteResponse(String courseId, String courseCode, List<PrerequisiteItemResponse> prerequisites) {
        this.courseId = courseId;
        this.courseCode = courseCode;
        this.prerequisites = prerequisites;
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

    public List<PrerequisiteItemResponse> getPrerequisites() {
        return prerequisites;
    }

    public void setPrerequisites(List<PrerequisiteItemResponse> prerequisites) {
        this.prerequisites = prerequisites;
    }
}

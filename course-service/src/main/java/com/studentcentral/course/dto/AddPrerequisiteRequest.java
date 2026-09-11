package com.studentcentral.course.dto;

import jakarta.validation.constraints.NotBlank;

public class AddPrerequisiteRequest {

    @NotBlank(message = "Prerequisite course identifier (ID or code) is required")
    private String prerequisiteCourseId;

    public AddPrerequisiteRequest() {
    }

    public AddPrerequisiteRequest(String prerequisiteCourseId) {
        this.prerequisiteCourseId = prerequisiteCourseId;
    }

    public String getPrerequisiteCourseId() {
        return prerequisiteCourseId;
    }

    public void setPrerequisiteCourseId(String prerequisiteCourseId) {
        this.prerequisiteCourseId = prerequisiteCourseId;
    }
}

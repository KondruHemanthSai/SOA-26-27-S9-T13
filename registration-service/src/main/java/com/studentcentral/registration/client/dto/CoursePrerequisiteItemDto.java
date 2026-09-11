package com.studentcentral.registration.client.dto;

public class CoursePrerequisiteItemDto {

    private String id;
    private String courseId;
    private String courseCode;
    private String courseName;
    private Integer credits;

    public CoursePrerequisiteItemDto() {
    }

    public CoursePrerequisiteItemDto(String id, String courseId, String courseCode, String courseName, Integer credits) {
        this.id = id;
        this.courseId = courseId;
        this.courseCode = courseCode;
        this.courseName = courseName;
        this.credits = credits;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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
}

package com.studentcentral.schedule.dto;

public class ConflictingCourseResponse {

    private String courseId;
    private String courseCode;
    private String courseName;
    private String dayOfWeek;
    private String startTime;
    private String endTime;

    public ConflictingCourseResponse() {
    }

    public ConflictingCourseResponse(String courseId, String courseCode, String courseName,
                                     String dayOfWeek, String startTime, String endTime) {
        this.courseId = courseId;
        this.courseCode = courseCode;
        this.courseName = courseName;
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
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

    public String getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(String dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }
}

package com.studentcentral.course.dto;

public class CourseAvailabilityResponse {

    private String courseId;
    private String courseCode;
    private Integer capacity;
    private Integer availableSeats;
    private Boolean isAvailable;

    public CourseAvailabilityResponse() {
    }

    public CourseAvailabilityResponse(String courseId, String courseCode, Integer capacity, Integer availableSeats, Boolean isAvailable) {
        this.courseId = courseId;
        this.courseCode = courseCode;
        this.capacity = capacity;
        this.availableSeats = availableSeats;
        this.isAvailable = isAvailable;
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

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public Integer getAvailableSeats() {
        return availableSeats;
    }

    public void setAvailableSeats(Integer availableSeats) {
        this.availableSeats = availableSeats;
    }

    public Boolean getIsAvailable() {
        return isAvailable;
    }

    public void setIsAvailable(Boolean isAvailable) {
        this.isAvailable = isAvailable;
    }
}

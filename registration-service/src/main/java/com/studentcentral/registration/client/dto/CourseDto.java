package com.studentcentral.registration.client.dto;

import java.util.List;

public class CourseDto {

    private String id;
    private String courseCode;
    private String courseName;
    private Integer credits;
    private Integer capacity;
    private Integer availableSeats;
    private String status;
    private Integer semester;
    private String department;
    private List<CoursePrerequisiteItemDto> prerequisites;

    public CourseDto() {
    }

    public CourseDto(String id, String courseCode, String courseName, Integer credits, Integer capacity, Integer availableSeats, String status, Integer semester, String department, List<CoursePrerequisiteItemDto> prerequisites) {
        this.id = id;
        this.courseCode = courseCode;
        this.courseName = courseName;
        this.credits = credits;
        this.capacity = capacity;
        this.availableSeats = availableSeats;
        this.status = status;
        this.semester = semester;
        this.department = department;
        this.prerequisites = prerequisites;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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
        return credits != null ? credits : 0;
    }

    public void setCredits(Integer credits) {
        this.credits = credits;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getSemester() {
        return semester;
    }

    public void setSemester(Integer semester) {
        this.semester = semester;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public List<CoursePrerequisiteItemDto> getPrerequisites() {
        return prerequisites != null ? prerequisites : List.of();
    }

    public void setPrerequisites(List<CoursePrerequisiteItemDto> prerequisites) {
        this.prerequisites = prerequisites;
    }
}

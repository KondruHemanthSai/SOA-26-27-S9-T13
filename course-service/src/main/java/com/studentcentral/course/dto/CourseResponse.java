package com.studentcentral.course.dto;

import com.studentcentral.course.model.Course;
import com.studentcentral.course.model.CourseStatus;
import com.studentcentral.course.model.CourseType;

import java.time.Instant;
import java.util.List;

public class CourseResponse {

    private String id;
    private String courseCode;
    private String courseName;
    private String description;
    private String department;
    private Integer semester;
    private Integer credits;
    private Integer capacity;
    private Integer availableSeats;
    private CourseType courseType;
    private CourseStatus status;
    private String faculty;
    private List<PrerequisiteItemResponse> prerequisites;
    private Instant createdAt;
    private Instant updatedAt;

    public CourseResponse() {
    }

    public CourseResponse(String id, String courseCode, String courseName, String description, String department, Integer semester, Integer credits, Integer capacity, Integer availableSeats, CourseType courseType, CourseStatus status, String faculty, List<PrerequisiteItemResponse> prerequisites, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.courseCode = courseCode;
        this.courseName = courseName;
        this.description = description;
        this.department = department;
        this.semester = semester;
        this.credits = credits;
        this.capacity = capacity;
        this.availableSeats = availableSeats;
        this.courseType = courseType;
        this.status = status;
        this.faculty = faculty;
        this.prerequisites = prerequisites;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static CourseResponse fromModel(Course course, List<PrerequisiteItemResponse> prerequisites) {
        if (course == null) {
            return null;
        }
        return new CourseResponse(
                course.getId(),
                course.getCourseCode(),
                course.getCourseName(),
                course.getDescription(),
                course.getDepartment(),
                course.getSemester(),
                course.getCredits(),
                course.getCapacity(),
                course.getAvailableSeats(),
                course.getCourseType(),
                course.getStatus(),
                course.getFaculty(),
                prerequisites,
                course.getCreatedAt(),
                course.getUpdatedAt()
        );
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public Integer getSemester() {
        return semester;
    }

    public void setSemester(Integer semester) {
        this.semester = semester;
    }

    public Integer getCredits() {
        return credits;
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

    public CourseType getCourseType() {
        return courseType;
    }

    public void setCourseType(CourseType courseType) {
        this.courseType = courseType;
    }

    public CourseStatus getStatus() {
        return status;
    }

    public void setStatus(CourseStatus status) {
        this.status = status;
    }

    public String getFaculty() {
        return faculty;
    }

    public void setFaculty(String faculty) {
        this.faculty = faculty;
    }

    public List<PrerequisiteItemResponse> getPrerequisites() {
        return prerequisites;
    }

    public void setPrerequisites(List<PrerequisiteItemResponse> prerequisites) {
        this.prerequisites = prerequisites;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}

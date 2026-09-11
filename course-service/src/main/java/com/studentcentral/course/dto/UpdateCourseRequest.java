package com.studentcentral.course.dto;

import com.studentcentral.course.model.CourseStatus;
import com.studentcentral.course.model.CourseType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public class UpdateCourseRequest {

    @Size(min = 2, max = 150, message = "Course name must be between 2 and 150 characters")
    private String courseName;

    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;

    @Size(min = 2, max = 100, message = "Department must be between 2 and 100 characters")
    private String department;

    @Min(value = 1, message = "Semester must be at least 1")
    @Max(value = 12, message = "Semester cannot exceed 12")
    private Integer semester;

    @Min(value = 1, message = "Credits must be at least 1")
    @Max(value = 10, message = "Credits cannot exceed 10")
    private Integer credits;

    @Min(value = 1, message = "Capacity must be greater than zero")
    @Max(value = 500, message = "Capacity cannot exceed 500")
    private Integer capacity;

    private CourseType courseType;

    private CourseStatus status;

    @Size(max = 100, message = "Faculty name cannot exceed 100 characters")
    private String faculty;

    public UpdateCourseRequest() {
    }

    public UpdateCourseRequest(String courseName, String description, String department, Integer semester, Integer credits, Integer capacity, CourseType courseType, CourseStatus status, String faculty) {
        this.courseName = courseName;
        this.description = description;
        this.department = department;
        this.semester = semester;
        this.credits = credits;
        this.capacity = capacity;
        this.courseType = courseType;
        this.status = status;
        this.faculty = faculty;
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
}

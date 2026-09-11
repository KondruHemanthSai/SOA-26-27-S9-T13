package com.studentcentral.course.model;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/**
 * MongoDB document representing an academic course.
 * Collection: courses
 * Database: student_central_course
 */
@Document(collection = "courses")
public class Course {

    @Id
    private String id;

    @Indexed(unique = true)
    private String courseCode;

    private String courseName;
    private String description;

    @Indexed
    private String department;

    @Indexed
    private Integer semester;

    private Integer credits;
    private Integer capacity;
    private Integer availableSeats;

    private CourseType courseType = CourseType.CORE;

    @Indexed
    private CourseStatus status = CourseStatus.ACTIVE;

    private String faculty;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    public Course() {
    }

    public Course(String courseCode, String courseName, String description, String department, Integer semester, Integer credits, Integer capacity, CourseType courseType, String faculty) {
        this.courseCode = courseCode != null ? courseCode.toUpperCase().trim() : null;
        this.courseName = courseName;
        this.description = description;
        this.department = department;
        this.semester = semester;
        this.credits = credits;
        this.capacity = capacity;
        this.availableSeats = capacity;
        this.courseType = courseType != null ? courseType : CourseType.CORE;
        this.status = CourseStatus.ACTIVE;
        this.faculty = faculty;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
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
        this.courseCode = courseCode != null ? courseCode.toUpperCase().trim() : null;
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

    @Override
    public String toString() {
        return "Course{" +
                "id='" + id + '\'' +
                ", courseCode='" + courseCode + '\'' +
                ", courseName='" + courseName + '\'' +
                ", department='" + department + '\'' +
                ", semester=" + semester +
                ", credits=" + credits +
                ", capacity=" + capacity +
                ", availableSeats=" + availableSeats +
                ", status=" + status +
                '}';
    }
}

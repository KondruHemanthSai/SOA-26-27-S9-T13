package com.studentcentral.course.model;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/**
 * MongoDB document representing a prerequisite course requirement.
 * Collection: prerequisites
 * Database: student_central_course
 */
@Document(collection = "prerequisites")
public class Prerequisite {

    @Id
    private String id;

    @Indexed
    private String courseId;

    @Indexed
    private String prerequisiteCourseId;

    @CreatedDate
    private Instant createdAt;

    public Prerequisite() {
    }

    public Prerequisite(String courseId, String prerequisiteCourseId) {
        this.courseId = courseId;
        this.prerequisiteCourseId = prerequisiteCourseId;
        this.createdAt = Instant.now();
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

    public String getPrerequisiteCourseId() {
        return prerequisiteCourseId;
    }

    public void setPrerequisiteCourseId(String prerequisiteCourseId) {
        this.prerequisiteCourseId = prerequisiteCourseId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Prerequisite{" +
                "id='" + id + '\'' +
                ", courseId='" + courseId + '\'' +
                ", prerequisiteCourseId='" + prerequisiteCourseId + '\'' +
                '}';
    }
}

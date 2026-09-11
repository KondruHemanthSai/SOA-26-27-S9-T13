package com.studentcentral.student.model;

/**
 * Embedded model representing an academically completed course with earned grade and credits.
 */
public class CompletedCourse {

    private String courseId;
    private String courseCode;
    private String courseName;
    private String grade;
    private Integer completedSemester;

    public CompletedCourse() {
    }

    public CompletedCourse(String courseId, String courseCode, String courseName, String grade, Integer completedSemester) {
        this.courseId = courseId;
        this.courseCode = courseCode != null ? courseCode.toUpperCase().trim() : null;
        this.courseName = courseName;
        this.grade = grade;
        this.completedSemester = completedSemester;
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
        this.courseCode = courseCode != null ? courseCode.toUpperCase().trim() : null;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public Integer getCompletedSemester() {
        return completedSemester;
    }

    public void setCompletedSemester(Integer completedSemester) {
        this.completedSemester = completedSemester;
    }
}

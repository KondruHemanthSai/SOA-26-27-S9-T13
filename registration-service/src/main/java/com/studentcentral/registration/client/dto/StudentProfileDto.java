package com.studentcentral.registration.client.dto;

import java.util.List;

public class StudentProfileDto {

    private String id;
    private String userId;
    private String studentId;
    private String firstName;
    private String lastName;
    private String email;
    private String department;
    private Integer semester;
    private String admissionStatus;
    private List<CompletedCourseDto> completedCourses;

    public StudentProfileDto() {
    }

    public StudentProfileDto(String id, String userId, String studentId, String firstName, String lastName, String email, String department, Integer semester, String admissionStatus, List<CompletedCourseDto> completedCourses) {
        this.id = id;
        this.userId = userId;
        this.studentId = studentId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.department = department;
        this.semester = semester;
        this.admissionStatus = admissionStatus;
        this.completedCourses = completedCourses;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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

    public String getAdmissionStatus() {
        return admissionStatus;
    }

    public void setAdmissionStatus(String admissionStatus) {
        this.admissionStatus = admissionStatus;
    }

    public List<CompletedCourseDto> getCompletedCourses() {
        return completedCourses != null ? completedCourses : List.of();
    }

    public void setCompletedCourses(List<CompletedCourseDto> completedCourses) {
        this.completedCourses = completedCourses;
    }
}

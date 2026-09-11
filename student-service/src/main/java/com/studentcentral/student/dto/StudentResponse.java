package com.studentcentral.student.dto;

import com.studentcentral.student.model.AdmissionStatus;
import com.studentcentral.student.model.Student;

import java.time.Instant;
import java.time.LocalDate;

public class StudentResponse {

    private String id;
    private String userId;
    private String studentId;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private LocalDate dateOfBirth;
    private String address;
    private String program;
    private String department;
    private Integer enrollmentYear;
    private Integer semester;
    private AdmissionStatus admissionStatus;
    private boolean profileCompleted;
    private AcademicRecordResponse academicRecord;
    private java.util.List<com.studentcentral.student.model.CompletedCourse> completedCourses;
    private Instant createdAt;
    private Instant updatedAt;

    public StudentResponse() {
    }

    public StudentResponse(String id, String userId, String studentId, String firstName, String lastName, String email, String phone, LocalDate dateOfBirth, String address, String program, String department, Integer enrollmentYear, Integer semester, AdmissionStatus admissionStatus, boolean profileCompleted, AcademicRecordResponse academicRecord, Instant createdAt, Instant updatedAt) {
        this(id, userId, studentId, firstName, lastName, email, phone, dateOfBirth, address, program, department, enrollmentYear, semester, admissionStatus, profileCompleted, academicRecord, java.util.List.of(), createdAt, updatedAt);
    }

    public StudentResponse(String id, String userId, String studentId, String firstName, String lastName, String email, String phone, LocalDate dateOfBirth, String address, String program, String department, Integer enrollmentYear, Integer semester, AdmissionStatus admissionStatus, boolean profileCompleted, AcademicRecordResponse academicRecord, java.util.List<com.studentcentral.student.model.CompletedCourse> completedCourses, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.userId = userId;
        this.studentId = studentId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.dateOfBirth = dateOfBirth;
        this.address = address;
        this.program = program;
        this.department = department;
        this.enrollmentYear = enrollmentYear;
        this.semester = semester;
        this.admissionStatus = admissionStatus;
        this.profileCompleted = profileCompleted;
        this.academicRecord = academicRecord;
        this.completedCourses = completedCourses;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static StudentResponse fromStudent(Student student) {
        if (student == null) {
            return null;
        }
        return new StudentResponse(
                student.getId(),
                student.getUserId(),
                student.getStudentId(),
                student.getFirstName(),
                student.getLastName(),
                student.getEmail(),
                student.getPhone(),
                student.getDateOfBirth(),
                student.getAddress(),
                student.getProgram(),
                student.getDepartment(),
                student.getEnrollmentYear(),
                student.getSemester(),
                student.getAdmissionStatus(),
                student.isProfileCompleted(),
                AcademicRecordResponse.fromModel(student.getAcademicRecord()),
                student.getCompletedCourses(),
                student.getCreatedAt(),
                student.getUpdatedAt()
        );
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

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getProgram() {
        return program;
    }

    public void setProgram(String program) {
        this.program = program;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public Integer getEnrollmentYear() {
        return enrollmentYear;
    }

    public void setEnrollmentYear(Integer enrollmentYear) {
        this.enrollmentYear = enrollmentYear;
    }

    public Integer getSemester() {
        return semester;
    }

    public void setSemester(Integer semester) {
        this.semester = semester;
    }

    public AdmissionStatus getAdmissionStatus() {
        return admissionStatus;
    }

    public void setAdmissionStatus(AdmissionStatus admissionStatus) {
        this.admissionStatus = admissionStatus;
    }

    public boolean isProfileCompleted() {
        return profileCompleted;
    }

    public void setProfileCompleted(boolean profileCompleted) {
        this.profileCompleted = profileCompleted;
    }

    public AcademicRecordResponse getAcademicRecord() {
        return academicRecord;
    }

    public void setAcademicRecord(AcademicRecordResponse academicRecord) {
        this.academicRecord = academicRecord;
    }

    public java.util.List<com.studentcentral.student.model.CompletedCourse> getCompletedCourses() {
        return completedCourses;
    }

    public void setCompletedCourses(java.util.List<com.studentcentral.student.model.CompletedCourse> completedCourses) {
        this.completedCourses = completedCourses;
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

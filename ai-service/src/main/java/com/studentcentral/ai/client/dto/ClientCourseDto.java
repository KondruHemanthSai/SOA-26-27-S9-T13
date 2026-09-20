package com.studentcentral.ai.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ClientCourseDto {
    private String id;
    private String courseCode;
    private String courseName;
    private String description;
    private String department;
    private Integer semester;
    private Integer credits;
    private Integer capacity;
    private Integer availableSeats;
    private String status;
    private String faculty;
    private List<String> prerequisiteCodes = new ArrayList<>();

    public ClientCourseDto() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getCourseCode() { return courseCode; }
    public void setCourseCode(String courseCode) { this.courseCode = courseCode; }
    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public Integer getSemester() { return semester; }
    public void setSemester(Integer semester) { this.semester = semester; }
    public Integer getCredits() { return credits; }
    public void setCredits(Integer credits) { this.credits = credits; }
    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }
    public Integer getAvailableSeats() { return availableSeats; }
    public void setAvailableSeats(Integer availableSeats) { this.availableSeats = availableSeats; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getFaculty() { return faculty; }
    public void setFaculty(String faculty) { this.faculty = faculty; }
    public List<String> getPrerequisiteCodes() { return prerequisiteCodes; }
    public void setPrerequisiteCodes(List<String> prerequisiteCodes) { this.prerequisiteCodes = prerequisiteCodes; }
}

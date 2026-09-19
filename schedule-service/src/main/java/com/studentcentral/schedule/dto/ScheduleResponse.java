package com.studentcentral.schedule.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.studentcentral.schedule.model.DayOfWeek;
import com.studentcentral.schedule.model.Schedule;
import com.studentcentral.schedule.model.ScheduleType;

import java.time.Instant;
import java.time.LocalTime;

public class ScheduleResponse {

    private String id;
    private String courseId;
    private String courseCode;
    private String courseName;
    private String section;
    private DayOfWeek dayOfWeek;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime startTime;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime endTime;

    private String classroom;
    private String building;
    private String faculty;
    private Integer semester;
    private String academicYear;
    private ScheduleType scheduleType;
    private Instant createdAt;
    private Instant updatedAt;

    public ScheduleResponse() {
    }

    public static ScheduleResponse fromModel(Schedule schedule) {
        if (schedule == null) {
            return null;
        }
        ScheduleResponse response = new ScheduleResponse();
        response.setId(schedule.getId());
        response.setCourseId(schedule.getCourseId());
        response.setCourseCode(schedule.getCourseCode());
        response.setCourseName(schedule.getCourseName());
        response.setSection(schedule.getSection());
        response.setDayOfWeek(schedule.getDayOfWeek());
        response.setStartTime(schedule.getStartTime());
        response.setEndTime(schedule.getEndTime());
        response.setClassroom(schedule.getClassroom());
        response.setBuilding(schedule.getBuilding());
        response.setFaculty(schedule.getFaculty());
        response.setSemester(schedule.getSemester());
        response.setAcademicYear(schedule.getAcademicYear());
        response.setScheduleType(schedule.getScheduleType());
        response.setCreatedAt(schedule.getCreatedAt());
        response.setUpdatedAt(schedule.getUpdatedAt());
        return response;
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

    public String getSection() {
        return section;
    }

    public void setSection(String section) {
        this.section = section;
    }

    public DayOfWeek getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(DayOfWeek dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public String getClassroom() {
        return classroom;
    }

    public void setClassroom(String classroom) {
        this.classroom = classroom;
    }

    public String getBuilding() {
        return building;
    }

    public void setBuilding(String building) {
        this.building = building;
    }

    public String getFaculty() {
        return faculty;
    }

    public void setFaculty(String faculty) {
        this.faculty = faculty;
    }

    public Integer getSemester() {
        return semester;
    }

    public void setSemester(Integer semester) {
        this.semester = semester;
    }

    public String getAcademicYear() {
        return academicYear;
    }

    public void setAcademicYear(String academicYear) {
        this.academicYear = academicYear;
    }

    public ScheduleType getScheduleType() {
        return scheduleType;
    }

    public void setScheduleType(ScheduleType scheduleType) {
        this.scheduleType = scheduleType;
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

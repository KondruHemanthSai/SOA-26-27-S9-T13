package com.studentcentral.schedule.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.time.LocalTime;

@Document(collection = "schedules")
@CompoundIndexes({
        @CompoundIndex(name = "course_day_idx", def = "{'courseId': 1, 'dayOfWeek': 1}"),
        @CompoundIndex(name = "classroom_day_idx", def = "{'classroom': 1, 'dayOfWeek': 1}"),
        @CompoundIndex(name = "faculty_day_idx", def = "{'faculty': 1, 'dayOfWeek': 1}")
})
public class Schedule {

    @Id
    private String id;

    @Indexed
    private String courseId;

    @Indexed
    private String courseCode;

    private String courseName;
    private String section;

    @Indexed
    private DayOfWeek dayOfWeek;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime startTime;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime endTime;

    @Indexed
    private String classroom;

    private String building;

    @Indexed
    private String faculty;

    @Indexed
    private Integer semester;

    @Indexed
    private String academicYear;

    private ScheduleType scheduleType;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    public Schedule() {
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public Schedule(String courseId, String courseCode, String courseName, String section,
                    DayOfWeek dayOfWeek, LocalTime startTime, LocalTime endTime,
                    String classroom, String building, String faculty,
                    Integer semester, String academicYear, ScheduleType scheduleType) {
        this.courseId = courseId;
        this.courseCode = courseCode;
        this.courseName = courseName;
        this.section = section != null && !section.isBlank() ? section.trim() : "A";
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
        this.classroom = classroom;
        this.building = building;
        this.faculty = faculty;
        this.semester = semester;
        this.academicYear = academicYear;
        this.scheduleType = scheduleType != null ? scheduleType : ScheduleType.LECTURE;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
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

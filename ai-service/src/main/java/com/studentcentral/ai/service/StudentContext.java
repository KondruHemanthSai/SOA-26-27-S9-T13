package com.studentcentral.ai.service;

import com.studentcentral.ai.client.dto.*;

import java.util.ArrayList;
import java.util.List;

public class StudentContext {

    private ClientStudentDto profile;
    private ClientAdmissionDto admission;
    private List<ClientRegistrationDto> registrations = new ArrayList<>();
    private List<ClientCourseDto> availableCourses = new ArrayList<>();
    private List<ClientScheduleDto> schedule = new ArrayList<>();
    private List<ClientNotificationDto> notifications = new ArrayList<>();
    private long unreadNotificationCount;

    public StudentContext() {
    }

    public ClientStudentDto getProfile() {
        return profile;
    }

    public void setProfile(ClientStudentDto profile) {
        this.profile = profile;
    }

    public ClientAdmissionDto getAdmission() {
        return admission;
    }

    public void setAdmission(ClientAdmissionDto admission) {
        this.admission = admission;
    }

    public List<ClientRegistrationDto> getRegistrations() {
        return registrations;
    }

    public void setRegistrations(List<ClientRegistrationDto> registrations) {
        this.registrations = registrations;
    }

    public List<ClientCourseDto> getAvailableCourses() {
        return availableCourses;
    }

    public void setAvailableCourses(List<ClientCourseDto> availableCourses) {
        this.availableCourses = availableCourses;
    }

    public List<ClientScheduleDto> getSchedule() {
        return schedule;
    }

    public void setSchedule(List<ClientScheduleDto> schedule) {
        this.schedule = schedule;
    }

    public List<ClientNotificationDto> getNotifications() {
        return notifications;
    }

    public void setNotifications(List<ClientNotificationDto> notifications) {
        this.notifications = notifications;
    }

    public long getUnreadNotificationCount() {
        return unreadNotificationCount;
    }

    public void setUnreadNotificationCount(long unreadNotificationCount) {
        this.unreadNotificationCount = unreadNotificationCount;
    }

    /**
     * Produces a clean, factual textual summary of the student's current system state
     * for prompt grounding without any private secrets.
     */
    public String toFactualSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("--- STUDENT FACTS FROM SYSTEM ---\n");
        if (profile != null) {
            sb.append("Student ID: ").append(profile.getStudentId() != null ? profile.getStudentId() : "N/A").append("\n");
            sb.append("Name: ").append(profile.getFirstName() != null ? profile.getFirstName() + " " + profile.getLastName() : "N/A").append("\n");
            sb.append("Program: ").append(profile.getProgram() != null ? profile.getProgram() : "N/A").append("\n");
            sb.append("Department: ").append(profile.getDepartment() != null ? profile.getDepartment() : "N/A").append("\n");
            sb.append("Semester: ").append(profile.getSemester() != null ? profile.getSemester() : "1").append("\n");
            sb.append("GPA: ").append(profile.getGpa() != null ? profile.getGpa() : "Not recorded").append("\n");
            sb.append("Completed Credits: ").append(profile.getCompletedCredits() != null ? profile.getCompletedCredits() : 0).append("\n");
        } else {
            sb.append("Profile: Incomplete or not yet created.\n");
        }

        if (admission != null) {
            sb.append("Admission Status: ").append(admission.getStatus() != null ? admission.getStatus() : "N/A").append("\n");
            sb.append("Admission Program: ").append(admission.getProgram() != null ? admission.getProgram() : "N/A").append("\n");
            if (admission.getRemarks() != null && !admission.getRemarks().isBlank()) {
                sb.append("Admission Remarks: ").append(admission.getRemarks()).append("\n");
            }
        }

        sb.append("Active Registrations (").append(registrations.size()).append(" courses):\n");
        int currentCredits = 0;
        for (ClientRegistrationDto r : registrations) {
            sb.append(" - ").append(r.getCourseCode()).append(": ").append(r.getCourseName())
              .append(" (").append(r.getCredits()).append(" credits, status: ").append(r.getStatus()).append(")\n");
            if ("REGISTERED".equalsIgnoreCase(r.getStatus()) && r.getCredits() != null) {
                currentCredits += r.getCredits();
            }
        }
        sb.append("Total Current Enrolled Credits: ").append(currentCredits).append(" (Max limit: 18 credits)\n");

        sb.append("Weekly Schedule Sessions (").append(schedule.size()).append(" classes):\n");
        for (ClientScheduleDto s : schedule) {
            sb.append(" - ").append(s.getDayOfWeek()).append(" ").append(s.getStartTime()).append("-").append(s.getEndTime())
              .append(": ").append(s.getCourseCode()).append(" in ").append(s.getClassroom())
              .append(" (Faculty: ").append(s.getFaculty()).append(")\n");
        }

        sb.append("Unread Notifications: ").append(unreadNotificationCount).append("\n");
        sb.append("---------------------------------\n");
        return sb.toString();
    }
}

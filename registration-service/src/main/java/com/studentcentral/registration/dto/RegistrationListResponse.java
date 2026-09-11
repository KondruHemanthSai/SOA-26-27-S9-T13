package com.studentcentral.registration.dto;

import java.util.List;

public class RegistrationListResponse {

    private String studentId;
    private List<RegistrationResponse> registrations;

    public RegistrationListResponse() {
    }

    public RegistrationListResponse(String studentId, List<RegistrationResponse> registrations) {
        this.studentId = studentId;
        this.registrations = registrations;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public List<RegistrationResponse> getRegistrations() {
        return registrations;
    }

    public void setRegistrations(List<RegistrationResponse> registrations) {
        this.registrations = registrations;
    }
}

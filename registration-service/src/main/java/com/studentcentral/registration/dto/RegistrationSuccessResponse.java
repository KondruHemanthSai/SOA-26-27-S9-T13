package com.studentcentral.registration.dto;

public class RegistrationSuccessResponse {

    private boolean success;
    private String message;
    private RegistrationResponse registration;

    public RegistrationSuccessResponse() {
    }

    public RegistrationSuccessResponse(boolean success, String message, RegistrationResponse registration) {
        this.success = success;
        this.message = message;
        this.registration = registration;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public RegistrationResponse getRegistration() {
        return registration;
    }

    public void setRegistration(RegistrationResponse registration) {
        this.registration = registration;
    }
}

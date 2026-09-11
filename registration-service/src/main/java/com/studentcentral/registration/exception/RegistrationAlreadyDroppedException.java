package com.studentcentral.registration.exception;

public class RegistrationAlreadyDroppedException extends RuntimeException {
    public RegistrationAlreadyDroppedException(String message) {
        super(message);
    }
}

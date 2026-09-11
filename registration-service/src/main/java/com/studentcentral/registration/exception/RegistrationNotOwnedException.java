package com.studentcentral.registration.exception;

public class RegistrationNotOwnedException extends RuntimeException {
    public RegistrationNotOwnedException(String message) {
        super(message);
    }
}

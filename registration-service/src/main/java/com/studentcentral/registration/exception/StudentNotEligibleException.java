package com.studentcentral.registration.exception;

public class StudentNotEligibleException extends RuntimeException {
    public StudentNotEligibleException(String message) {
        super(message);
    }
}

package com.studentcentral.admission.exception;

public class UnauthorizedApplicationAccessException extends RuntimeException {
    public UnauthorizedApplicationAccessException(String message) {
        super(message);
    }
}

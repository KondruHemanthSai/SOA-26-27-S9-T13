package com.studentcentral.admission.exception;

public class ApplicationNotEditableException extends RuntimeException {
    public ApplicationNotEditableException(String message) {
        super(message);
    }
}

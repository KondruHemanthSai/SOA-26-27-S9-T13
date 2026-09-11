package com.studentcentral.course.exception;

public class PrerequisiteNotFoundException extends RuntimeException {
    public PrerequisiteNotFoundException(String message) {
        super(message);
    }
}

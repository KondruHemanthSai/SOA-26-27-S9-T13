package com.studentcentral.registration.exception;

public class CourseInactiveException extends RuntimeException {
    public CourseInactiveException(String message) {
        super(message);
    }
}

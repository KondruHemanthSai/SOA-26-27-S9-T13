package com.studentcentral.course.exception;

public class CircularPrerequisiteException extends RuntimeException {
    public CircularPrerequisiteException(String message) {
        super(message);
    }
}

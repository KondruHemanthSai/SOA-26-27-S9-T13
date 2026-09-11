package com.studentcentral.course.exception;

public class DuplicatePrerequisiteException extends RuntimeException {
    public DuplicatePrerequisiteException(String message) {
        super(message);
    }
}

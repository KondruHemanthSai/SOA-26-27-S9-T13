package com.studentcentral.course.exception;

public class CourseInactiveException extends RuntimeException {
    public CourseInactiveException(String message) {
        super(message);
    }
}

package com.studentcentral.course.exception;

public class InvalidSeatCountException extends RuntimeException {
    public InvalidSeatCountException(String message) {
        super(message);
    }
}

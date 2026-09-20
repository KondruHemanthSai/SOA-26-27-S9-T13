package com.studentcentral.notification.exception;

public class NotificationNotOwnedException extends RuntimeException {
    public NotificationNotOwnedException(String message) {
        super(message);
    }
}

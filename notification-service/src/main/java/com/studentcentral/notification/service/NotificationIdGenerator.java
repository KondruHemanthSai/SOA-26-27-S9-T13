package com.studentcentral.notification.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class NotificationIdGenerator {

    private static final Logger log = LoggerFactory.getLogger(NotificationIdGenerator.class);

    /**
     * Generates a unique notification ID in the format NOTIF-XXXXXXXX.
     * Uses the first 8 hex characters of a UUID for uniqueness.
     */
    public String generateNotificationId() {
        String uuid = UUID.randomUUID().toString().replace("-", "");
        String notificationId = "NOTIF-" + uuid.substring(0, 8).toUpperCase();
        log.debug("Generated notification ID: {}", notificationId);
        return notificationId;
    }
}

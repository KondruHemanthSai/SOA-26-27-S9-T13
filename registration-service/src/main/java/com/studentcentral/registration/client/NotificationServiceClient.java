package com.studentcentral.registration.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * REST client for sending best-effort notifications to the Notification Service.
 * Notification failures are logged but never propagate to callers — core registration
 * operations must never fail because the Notification Service is unavailable.
 */
@Component
public class NotificationServiceClient {

    private static final Logger log = LoggerFactory.getLogger(NotificationServiceClient.class);
    private static final String NOTIFICATION_SERVICE_URL = "http://notification-service/api/notifications/internal";

    private final RestTemplate restTemplate;

    public NotificationServiceClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Sends a registration-successful notification (best-effort).
     */
    public void sendRegistrationNotification(String userId, String courseCode, String courseName, String registrationId) {
        try {
            Map<String, Object> request = new HashMap<>();
            request.put("userId", userId);
            request.put("title", "Registration Successful");
            request.put("message", "You have successfully registered for " + courseCode + " - " + courseName + ".");
            request.put("type", "REGISTRATION");
            request.put("priority", "NORMAL");
            request.put("relatedEntityType", "REGISTRATION");
            request.put("relatedEntityId", registrationId);
            request.put("createdBy", "registration-service");

            HttpEntity<Map<String, Object>> entity = createJsonEntity(request);
            restTemplate.postForEntity(NOTIFICATION_SERVICE_URL, entity, String.class);
            log.info("Registration notification sent for userId={}, courseCode={}", userId, courseCode);
        } catch (Exception e) {
            log.warn("Failed to send registration notification for userId={}, courseCode={}: {}",
                    userId, courseCode, e.getMessage());
        }
    }

    /**
     * Sends a course-dropped notification (best-effort).
     */
    public void sendDropNotification(String userId, String courseCode, String courseName, String registrationId) {
        try {
            Map<String, Object> request = new HashMap<>();
            request.put("userId", userId);
            request.put("title", "Course Dropped");
            request.put("message", "You have dropped the course " + courseCode + " - " + courseName + ".");
            request.put("type", "REGISTRATION");
            request.put("priority", "NORMAL");
            request.put("relatedEntityType", "REGISTRATION");
            request.put("relatedEntityId", registrationId);
            request.put("createdBy", "registration-service");

            HttpEntity<Map<String, Object>> entity = createJsonEntity(request);
            restTemplate.postForEntity(NOTIFICATION_SERVICE_URL, entity, String.class);
            log.info("Drop notification sent for userId={}, courseCode={}", userId, courseCode);
        } catch (Exception e) {
            log.warn("Failed to send drop notification for userId={}, courseCode={}: {}",
                    userId, courseCode, e.getMessage());
        }
    }

    private HttpEntity<Map<String, Object>> createJsonEntity(Map<String, Object> body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(body, headers);
    }
}

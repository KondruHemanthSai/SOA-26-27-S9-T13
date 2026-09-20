package com.studentcentral.admission.client;

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
 * Notification failures are logged but never propagate to callers — core admission
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
     * Sends an admission-approved notification (best-effort).
     */
    public void sendAdmissionApprovedNotification(String userId, String applicationId) {
        sendAdmissionNotification(userId, applicationId,
                "Admission Approved",
                "Your admission application has been approved.",
                "HIGH");
    }

    /**
     * Sends an admission-rejected notification (best-effort).
     */
    public void sendAdmissionRejectedNotification(String userId, String applicationId) {
        sendAdmissionNotification(userId, applicationId,
                "Admission Application Update",
                "Your admission application has been rejected. Please review the application status for details.",
                "HIGH");
    }

    /**
     * Sends an admission-changes-requested notification (best-effort).
     */
    public void sendAdmissionChangesRequestedNotification(String userId, String applicationId) {
        sendAdmissionNotification(userId, applicationId,
                "Action Required: Admission Application",
                "Changes have been requested for your admission application.",
                "HIGH");
    }

    private void sendAdmissionNotification(String userId, String applicationId,
                                           String title, String message, String priority) {
        try {
            Map<String, Object> request = new HashMap<>();
            request.put("userId", userId);
            request.put("title", title);
            request.put("message", message);
            request.put("type", "ADMISSION");
            request.put("priority", priority);
            request.put("relatedEntityType", "ADMISSION");
            request.put("relatedEntityId", applicationId);
            request.put("createdBy", "admission-service");

            HttpEntity<Map<String, Object>> entity = createJsonEntity(request);
            restTemplate.postForEntity(NOTIFICATION_SERVICE_URL, entity, String.class);
            log.info("Admission notification '{}' sent for userId={}, applicationId={}", title, userId, applicationId);
        } catch (Exception e) {
            log.warn("Failed to send admission notification '{}' for userId={}, applicationId={}: {}",
                    title, userId, applicationId, e.getMessage());
        }
    }

    private HttpEntity<Map<String, Object>> createJsonEntity(Map<String, Object> body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(body, headers);
    }
}

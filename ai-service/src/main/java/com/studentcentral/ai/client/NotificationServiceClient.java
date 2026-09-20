package com.studentcentral.ai.client;

import com.studentcentral.ai.client.dto.ClientNotificationDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class NotificationServiceClient {

    private static final Logger log = LoggerFactory.getLogger(NotificationServiceClient.class);
    private static final String BASE_URL = "http://notification-service/api/notifications";

    private final RestTemplate restTemplate;

    public NotificationServiceClient(@Qualifier("loadBalancedRestTemplate") RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public List<ClientNotificationDto> getUnreadNotifications(String userId, String jwtToken) {
        try {
            HttpHeaders headers = new HttpHeaders();
            if (jwtToken != null && !jwtToken.isBlank()) {
                headers.set(HttpHeaders.AUTHORIZATION, jwtToken.startsWith("Bearer ") ? jwtToken : "Bearer " + jwtToken);
            }
            if (userId != null) {
                headers.set("X-User-Id", userId);
            }
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<List<ClientNotificationDto>> response = restTemplate.exchange(
                    BASE_URL + "/my/unread",
                    HttpMethod.GET,
                    entity,
                    new ParameterizedTypeReference<List<ClientNotificationDto>>() {}
            );
            return response.getBody() != null ? response.getBody() : new ArrayList<>();
        } catch (Exception e) {
            log.warn("Failed to fetch unread notifications for user {}: {}", userId, e.getMessage());
            return new ArrayList<>();
        }
    }

    public long getUnreadCount(String userId, String jwtToken) {
        try {
            HttpHeaders headers = new HttpHeaders();
            if (jwtToken != null && !jwtToken.isBlank()) {
                headers.set(HttpHeaders.AUTHORIZATION, jwtToken.startsWith("Bearer ") ? jwtToken : "Bearer " + jwtToken);
            }
            if (userId != null) {
                headers.set("X-User-Id", userId);
            }
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    BASE_URL + "/my/unread-count",
                    HttpMethod.GET,
                    entity,
                    Map.class
            );
            if (response.getBody() != null && response.getBody().containsKey("unreadCount")) {
                return ((Number) response.getBody().get("unreadCount")).longValue();
            }
            return 0;
        } catch (Exception e) {
            log.warn("Failed to fetch unread count for user {}: {}", userId, e.getMessage());
            return 0;
        }
    }
}

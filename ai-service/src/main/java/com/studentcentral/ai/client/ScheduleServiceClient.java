package com.studentcentral.ai.client;

import com.studentcentral.ai.client.dto.ClientScheduleDto;
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

@Component
public class ScheduleServiceClient {

    private static final Logger log = LoggerFactory.getLogger(ScheduleServiceClient.class);
    private static final String BASE_URL = "http://schedule-service/api/schedules";

    private final RestTemplate restTemplate;

    public ScheduleServiceClient(@Qualifier("loadBalancedRestTemplate") RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public List<ClientScheduleDto> getMyTimetable(String userId, String jwtToken) {
        try {
            HttpHeaders headers = new HttpHeaders();
            if (jwtToken != null && !jwtToken.isBlank()) {
                headers.set(HttpHeaders.AUTHORIZATION, jwtToken.startsWith("Bearer ") ? jwtToken : "Bearer " + jwtToken);
            }
            if (userId != null) {
                headers.set("X-User-Id", userId);
            }
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<List<ClientScheduleDto>> response = restTemplate.exchange(
                    BASE_URL + "/my-timetable",
                    HttpMethod.GET,
                    entity,
                    new ParameterizedTypeReference<List<ClientScheduleDto>>() {}
            );
            return response.getBody() != null ? response.getBody() : new ArrayList<>();
        } catch (Exception e) {
            log.warn("Failed to fetch timetable for user {}: {}", userId, e.getMessage());
            return new ArrayList<>();
        }
    }
}

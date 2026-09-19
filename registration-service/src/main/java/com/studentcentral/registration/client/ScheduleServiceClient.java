package com.studentcentral.registration.client;

import com.studentcentral.registration.client.dto.ScheduleConflictCheckRequest;
import com.studentcentral.registration.client.dto.ScheduleConflictCheckResponse;
import com.studentcentral.registration.client.dto.ScheduleConflictResult;
import com.studentcentral.registration.exception.DependencyServiceUnavailableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

@Component
@Primary
public class ScheduleServiceClient implements ScheduleValidationClient {

    private static final Logger log = LoggerFactory.getLogger(ScheduleServiceClient.class);
    private static final String SCHEDULE_SERVICE_URL = "http://schedule-service/api/schedules/check-conflict";

    private final RestTemplate restTemplate;

    public ScheduleServiceClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public ScheduleConflictResult checkScheduleConflict(String studentId, String courseId, Integer semester, String jwtToken) {
        log.debug("Sending schedule conflict check request to Schedule Service for student {} and course {}", studentId, courseId);
        try {
            HttpHeaders headers = new HttpHeaders();
            if (jwtToken != null && !jwtToken.isBlank()) {
                headers.set(HttpHeaders.AUTHORIZATION, jwtToken.startsWith("Bearer ") ? jwtToken : "Bearer " + jwtToken);
            }
            ScheduleConflictCheckRequest requestBody = new ScheduleConflictCheckRequest(studentId, courseId);
            HttpEntity<ScheduleConflictCheckRequest> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<ScheduleConflictCheckResponse> response = restTemplate.exchange(
                    SCHEDULE_SERVICE_URL,
                    HttpMethod.POST,
                    entity,
                    ScheduleConflictCheckResponse.class
            );

            ScheduleConflictCheckResponse body = response.getBody();
            if (body != null) {
                return new ScheduleConflictResult(body.isConflict(), body.getConflictingCourse());
            }
            return new ScheduleConflictResult(false, null);

        } catch (HttpClientErrorException.NotFound e) {
            log.warn("Schedule conflict check: Schedule service returned 404 for student {} / course {}", studentId, courseId);
            return new ScheduleConflictResult(false, null);
        } catch (ResourceAccessException e) {
            log.error("Schedule Service unreachable during conflict check: {}", e.getMessage());
            throw new DependencyServiceUnavailableException("Schedule Service is currently unavailable");
        } catch (Exception e) {
            log.error("Error communicating with Schedule Service during conflict check: {}", e.getMessage());
            throw new DependencyServiceUnavailableException("Failed to validate schedule conflict with Schedule Service: " + e.getMessage());
        }
    }
}

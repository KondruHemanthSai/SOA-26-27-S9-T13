package com.studentcentral.admission.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
public class StudentServiceClient {

    private static final Logger log = LoggerFactory.getLogger(StudentServiceClient.class);
    private final RestTemplate restTemplate;

    public StudentServiceClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Resolves student institutional ID from student profile via Student Service.
     */
    public String resolveStudentId(String userId, String authHeader) {
        try {
            HttpHeaders headers = new HttpHeaders();
            if (authHeader != null && !authHeader.isBlank()) {
                headers.set("Authorization", authHeader);
            }
            headers.set("X-User-Id", userId);
            headers.set("X-User-Role", "STUDENT");

            HttpEntity<Void> entity = new HttpEntity<>(headers);
            ResponseEntity<Map> response = restTemplate.exchange(
                    "http://student-service/api/students/profile",
                    HttpMethod.GET,
                    entity,
                    Map.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Object studentId = response.getBody().get("studentId");
                if (studentId != null) {
                    return studentId.toString();
                }
            }
        } catch (HttpClientErrorException.NotFound e) {
            log.info("Student profile not found in student-service for userId: {}", userId);
        } catch (Exception e) {
            log.warn("Could not contact student-service for userId {}: {}", userId, e.getMessage());
        }
        return null;
    }
}

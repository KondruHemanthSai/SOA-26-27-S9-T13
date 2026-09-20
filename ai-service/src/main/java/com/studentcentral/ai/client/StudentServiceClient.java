package com.studentcentral.ai.client;

import com.studentcentral.ai.client.dto.ClientStudentDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

@Component
public class StudentServiceClient {

    private static final Logger log = LoggerFactory.getLogger(StudentServiceClient.class);
    private static final String BASE_URL = "http://student-service/api/students";

    private final RestTemplate restTemplate;

    public StudentServiceClient(@Qualifier("loadBalancedRestTemplate") RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public Optional<ClientStudentDto> getProfile(String userId, String jwtToken) {
        try {
            HttpHeaders headers = new HttpHeaders();
            if (jwtToken != null && !jwtToken.isBlank()) {
                headers.set(HttpHeaders.AUTHORIZATION, jwtToken.startsWith("Bearer ") ? jwtToken : "Bearer " + jwtToken);
            }
            if (userId != null) {
                headers.set("X-User-Id", userId);
            }
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<ClientStudentDto> response = restTemplate.exchange(
                    BASE_URL + "/profile",
                    HttpMethod.GET,
                    entity,
                    ClientStudentDto.class
            );
            return Optional.ofNullable(response.getBody());
        } catch (Exception e) {
            log.warn("Failed to fetch student profile for userId {}: {}", userId, e.getMessage());
            return Optional.empty();
        }
    }
}

package com.studentcentral.ai.client;

import com.studentcentral.ai.client.dto.ClientCourseDto;
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
import java.util.Optional;

@Component
public class CourseServiceClient {

    private static final Logger log = LoggerFactory.getLogger(CourseServiceClient.class);
    private static final String BASE_URL = "http://course-service/api/courses";

    private final RestTemplate restTemplate;

    public CourseServiceClient(@Qualifier("loadBalancedRestTemplate") RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public List<ClientCourseDto> getAllCourses(String jwtToken) {
        try {
            HttpHeaders headers = new HttpHeaders();
            if (jwtToken != null && !jwtToken.isBlank()) {
                headers.set(HttpHeaders.AUTHORIZATION, jwtToken.startsWith("Bearer ") ? jwtToken : "Bearer " + jwtToken);
            }
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<List<ClientCourseDto>> response = restTemplate.exchange(
                    BASE_URL,
                    HttpMethod.GET,
                    entity,
                    new ParameterizedTypeReference<List<ClientCourseDto>>() {}
            );
            return response.getBody() != null ? response.getBody() : new ArrayList<>();
        } catch (Exception e) {
            log.warn("Failed to fetch courses: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    public Optional<ClientCourseDto> getCourseByCode(String courseCode, String jwtToken) {
        try {
            List<ClientCourseDto> all = getAllCourses(jwtToken);
            return all.stream()
                    .filter(c -> c.getCourseCode() != null && c.getCourseCode().equalsIgnoreCase(courseCode))
                    .findFirst();
        } catch (Exception e) {
            log.warn("Failed to find course by code {}: {}", courseCode, e.getMessage());
            return Optional.empty();
        }
    }
}

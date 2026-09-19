package com.studentcentral.schedule.client;

import com.studentcentral.schedule.client.dto.CourseDto;
import com.studentcentral.schedule.exception.DependencyServiceUnavailableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

@Component
public class CourseServiceClient {

    private static final Logger log = LoggerFactory.getLogger(CourseServiceClient.class);
    private static final String COURSE_SERVICE_URL = "http://course-service/api/courses";

    private final RestTemplate restTemplate;

    public CourseServiceClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Retrieves course information from Course Service.
     */
    public Optional<CourseDto> getCourse(String courseIdOrCode, String jwtToken) {
        try {
            HttpHeaders headers = new HttpHeaders();
            if (jwtToken != null && !jwtToken.isBlank()) {
                headers.set(HttpHeaders.AUTHORIZATION, jwtToken.startsWith("Bearer ") ? jwtToken : "Bearer " + jwtToken);
            }
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<CourseDto> response = restTemplate.exchange(
                    COURSE_SERVICE_URL + "/" + courseIdOrCode,
                    HttpMethod.GET,
                    entity,
                    CourseDto.class
            );
            return Optional.ofNullable(response.getBody());
        } catch (HttpClientErrorException.NotFound e) {
            log.warn("Course not found in Course Service: {}", courseIdOrCode);
            return Optional.empty();
        } catch (ResourceAccessException e) {
            log.error("Course Service unreachable: {}", e.getMessage());
            throw new DependencyServiceUnavailableException("Course Service is currently unavailable");
        } catch (Exception e) {
            log.error("Error communicating with Course Service: {}", e.getMessage());
            return Optional.empty();
        }
    }
}

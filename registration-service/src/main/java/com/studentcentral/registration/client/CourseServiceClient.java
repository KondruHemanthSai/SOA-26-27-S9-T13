package com.studentcentral.registration.client;

import com.studentcentral.registration.client.dto.CourseAvailabilityDto;
import com.studentcentral.registration.client.dto.CourseDto;
import com.studentcentral.registration.exception.CourseNotFoundException;
import com.studentcentral.registration.exception.DependencyServiceUnavailableException;
import com.studentcentral.registration.exception.SeatReservationFailedException;
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
     * Retrieves course information and prerequisite metadata from Course Service.
     */
    public Optional<CourseDto> getCourse(String courseIdOrCode, String jwtToken) {
        try {
            HttpEntity<Void> entity = createAuthEntity(jwtToken);
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

    /**
     * Retrieves seat availability from Course Service.
     */
    public Optional<CourseAvailabilityDto> getAvailability(String courseIdOrCode, String jwtToken) {
        try {
            HttpEntity<Void> entity = createAuthEntity(jwtToken);
            ResponseEntity<CourseAvailabilityDto> response = restTemplate.exchange(
                    COURSE_SERVICE_URL + "/" + courseIdOrCode + "/availability",
                    HttpMethod.GET,
                    entity,
                    CourseAvailabilityDto.class
            );
            return Optional.ofNullable(response.getBody());
        } catch (HttpClientErrorException.NotFound e) {
            log.warn("Course not found for availability: {}", courseIdOrCode);
            return Optional.empty();
        } catch (ResourceAccessException e) {
            log.error("Course Service unreachable: {}", e.getMessage());
            throw new DependencyServiceUnavailableException("Course Service is currently unavailable");
        } catch (Exception e) {
            log.error("Error fetching availability: {}", e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Atomically reserves a seat in Course Service.
     */
    public boolean reserveSeat(String courseIdOrCode, String jwtToken) {
        try {
            HttpEntity<Void> entity = createAuthEntity(jwtToken);
            ResponseEntity<CourseAvailabilityDto> response = restTemplate.exchange(
                    COURSE_SERVICE_URL + "/" + courseIdOrCode + "/reserve-seat",
                    HttpMethod.POST,
                    entity,
                    CourseAvailabilityDto.class
            );
            return response.getStatusCode().is2xxSuccessful();
        } catch (HttpClientErrorException.BadRequest | HttpClientErrorException.Conflict e) {
            log.warn("Seat reservation rejected for course {}: {}", courseIdOrCode, e.getMessage());
            return false;
        } catch (ResourceAccessException e) {
            log.error("Course Service unreachable during seat reservation: {}", e.getMessage());
            throw new DependencyServiceUnavailableException("Course Service is currently unavailable");
        } catch (Exception e) {
            log.error("Error reserving seat in Course Service: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Releases a seat in Course Service (e.g. on course drop or compensating rollback).
     */
    public void releaseSeat(String courseIdOrCode, String jwtToken) {
        try {
            HttpEntity<Void> entity = createAuthEntity(jwtToken);
            restTemplate.exchange(
                    COURSE_SERVICE_URL + "/" + courseIdOrCode + "/release-seat",
                    HttpMethod.POST,
                    entity,
                    CourseAvailabilityDto.class
            );
            log.info("Successfully released seat for course {}", courseIdOrCode);
        } catch (Exception e) {
            log.error("Failed to release seat for course {}: {}", courseIdOrCode, e.getMessage());
        }
    }

    private HttpEntity<Void> createAuthEntity(String jwtToken) {
        HttpHeaders headers = new HttpHeaders();
        if (jwtToken != null && !jwtToken.isBlank()) {
            headers.set(HttpHeaders.AUTHORIZATION, jwtToken.startsWith("Bearer ") ? jwtToken : "Bearer " + jwtToken);
        }
        return new HttpEntity<>(headers);
    }
}

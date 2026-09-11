package com.studentcentral.registration.client;

import com.studentcentral.registration.client.dto.StudentProfileDto;
import com.studentcentral.registration.exception.DependencyServiceUnavailableException;
import com.studentcentral.registration.exception.StudentNotFoundException;
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
public class StudentServiceClient {

    private static final Logger log = LoggerFactory.getLogger(StudentServiceClient.class);
    private static final String STUDENT_SERVICE_URL = "http://student-service/api/students";

    private final RestTemplate restTemplate;

    public StudentServiceClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Resolves student profile by userId or studentId.
     */
    public Optional<StudentProfileDto> getStudentProfile(String idOrUserId, String jwtToken) {
        try {
            HttpHeaders headers = new HttpHeaders();
            if (jwtToken != null && !jwtToken.isBlank()) {
                headers.set(HttpHeaders.AUTHORIZATION, jwtToken.startsWith("Bearer ") ? jwtToken : "Bearer " + jwtToken);
            }
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<StudentProfileDto> response = restTemplate.exchange(
                    STUDENT_SERVICE_URL + "/" + idOrUserId,
                    HttpMethod.GET,
                    entity,
                    StudentProfileDto.class
            );

            return Optional.ofNullable(response.getBody());
        } catch (HttpClientErrorException.NotFound e) {
            log.warn("Student not found in Student Service: {}", idOrUserId);
            return Optional.empty();
        } catch (HttpClientErrorException.Forbidden e) {
            // If viewing by own profile
            try {
                HttpHeaders headers = new HttpHeaders();
                if (jwtToken != null && !jwtToken.isBlank()) {
                    headers.set(HttpHeaders.AUTHORIZATION, jwtToken.startsWith("Bearer ") ? jwtToken : "Bearer " + jwtToken);
                }
                HttpEntity<Void> entity = new HttpEntity<>(headers);
                ResponseEntity<StudentProfileDto> response = restTemplate.exchange(
                        STUDENT_SERVICE_URL + "/profile",
                        HttpMethod.GET,
                        entity,
                        StudentProfileDto.class
                );
                return Optional.ofNullable(response.getBody());
            } catch (Exception ex) {
                log.warn("Failed fallback to /api/students/profile for {}: {}", idOrUserId, ex.getMessage());
                return Optional.empty();
            }
        } catch (ResourceAccessException e) {
            log.error("Student Service unreachable: {}", e.getMessage());
            throw new DependencyServiceUnavailableException("Student Service is currently unavailable");
        } catch (Exception e) {
            log.error("Error communicating with Student Service: {}", e.getMessage());
            return Optional.empty();
        }
    }
}

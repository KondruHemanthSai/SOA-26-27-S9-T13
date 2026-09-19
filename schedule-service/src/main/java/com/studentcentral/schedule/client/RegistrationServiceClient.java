package com.studentcentral.schedule.client;

import com.studentcentral.schedule.exception.DependencyServiceUnavailableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;

@Component
public class RegistrationServiceClient {

    private static final Logger log = LoggerFactory.getLogger(RegistrationServiceClient.class);
    private static final String REGISTRATION_SERVICE_URL = "http://registration-service/api/registrations";

    private final RestTemplate restTemplate;

    public RegistrationServiceClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Retrieves active registered course IDs for a specific student.
     */
    public List<String> getActiveCourseIds(String studentId, String jwtToken) {
        try {
            HttpHeaders headers = new HttpHeaders();
            if (jwtToken != null && !jwtToken.isBlank()) {
                headers.set(HttpHeaders.AUTHORIZATION, jwtToken.startsWith("Bearer ") ? jwtToken : "Bearer " + jwtToken);
            }
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<List<String>> response = restTemplate.exchange(
                    REGISTRATION_SERVICE_URL + "/student/" + studentId + "/active-course-ids",
                    HttpMethod.GET,
                    entity,
                    new ParameterizedTypeReference<List<String>>() {}
            );

            return response.getBody() != null ? response.getBody() : Collections.emptyList();
        } catch (HttpClientErrorException.NotFound e) {
            log.warn("No active registrations or student not found in Registration Service: {}", studentId);
            return Collections.emptyList();
        } catch (ResourceAccessException e) {
            log.error("Registration Service unreachable: {}", e.getMessage());
            throw new DependencyServiceUnavailableException("Registration Service is currently unavailable");
        } catch (Exception e) {
            log.error("Error communicating with Registration Service: {}", e.getMessage());
            return Collections.emptyList();
        }
    }
}

package com.studentcentral.registration.client;

import com.studentcentral.registration.exception.DependencyServiceUnavailableException;
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

import java.util.Map;

@Component
public class AdmissionServiceClient {

    private static final Logger log = LoggerFactory.getLogger(AdmissionServiceClient.class);
    private static final String ADMISSION_SERVICE_URL = "http://admission-service/api/admissions";

    private final RestTemplate restTemplate;

    public AdmissionServiceClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Checks if student admission application is in APPROVED status.
     */
    public boolean isAdmissionApproved(String jwtToken) {
        try {
            HttpHeaders headers = new HttpHeaders();
            if (jwtToken != null && !jwtToken.isBlank()) {
                headers.set(HttpHeaders.AUTHORIZATION, jwtToken.startsWith("Bearer ") ? jwtToken : "Bearer " + jwtToken);
            }
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    ADMISSION_SERVICE_URL + "/my-application",
                    HttpMethod.GET,
                    entity,
                    Map.class
            );

            if (response.getBody() != null && response.getBody().containsKey("status")) {
                String status = String.valueOf(response.getBody().get("status"));
                return "APPROVED".equalsIgnoreCase(status);
            }
            return false;
        } catch (HttpClientErrorException.NotFound e) {
            log.warn("No admission application found for current student");
            return false;
        } catch (ResourceAccessException e) {
            log.error("Admission Service unreachable: {}", e.getMessage());
            throw new DependencyServiceUnavailableException("Admission Service is currently unavailable");
        } catch (Exception e) {
            log.error("Error communicating with Admission Service: {}", e.getMessage());
            return false;
        }
    }
}

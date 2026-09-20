package com.studentcentral.ai.provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Component
public class OpenAIProvider implements AIProvider {

    private static final Logger log = LoggerFactory.getLogger(OpenAIProvider.class);
    private static final String OPENAI_API_URL = "https://api.openai.com/v1/chat/completions";

    private final String apiKey;
    private final String model;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public OpenAIProvider(@Value("${ai.openai.api-key:}") String apiKey,
                          @Value("${ai.openai.model:gpt-4o-mini}") String model,
                          @Qualifier("externalRestTemplate") RestTemplate restTemplate) {
        this.apiKey = apiKey != null ? apiKey.trim() : "";
        this.model = model != null && !model.isBlank() ? model : "gpt-4o-mini";
        this.restTemplate = restTemplate;
    }

    @Override
    public String generateResponse(String systemInstruction, String userPrompt) {
        if (!isAvailable()) {
            throw new IllegalStateException("OpenAI API key is not configured.");
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            Map<String, Object> requestBody = Map.of(
                    "model", model,
                    "temperature", 0.2,
                    "messages", List.of(
                            Map.of("role", "system", "content", systemInstruction),
                            Map.of("role", "user", "content", userPrompt)
                    )
            );

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(OPENAI_API_URL, entity, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode root = objectMapper.readTree(response.getBody());
                JsonNode choices = root.path("choices");
                if (choices.isArray() && choices.size() > 0) {
                    return choices.get(0).path("message").path("content").asText().trim();
                }
            }
            throw new RuntimeException("Empty response from OpenAI API.");
        } catch (Exception e) {
            log.error("OpenAI API call failed: {}", e.getMessage());
            throw new RuntimeException("OpenAI provider call failed: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean isAvailable() {
        return apiKey != null && !apiKey.isBlank() && !apiKey.contains("your-key-here") && !apiKey.contains("your-openai-api-key");
    }

    @Override
    public String getProviderName() {
        return "OpenAI (" + model + ")";
    }
}

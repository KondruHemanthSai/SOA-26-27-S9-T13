package com.studentcentral.ai.provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component
@Primary
public class CompositeAIProvider implements AIProvider {

    private static final Logger log = LoggerFactory.getLogger(CompositeAIProvider.class);

    private final OpenAIProvider openAIProvider;
    private final RuleBasedFallbackProvider fallbackProvider;

    public CompositeAIProvider(OpenAIProvider openAIProvider, RuleBasedFallbackProvider fallbackProvider) {
        this.openAIProvider = openAIProvider;
        this.fallbackProvider = fallbackProvider;
    }

    @Override
    public String generateResponse(String systemInstruction, String userPrompt) {
        if (openAIProvider.isAvailable()) {
            try {
                log.info("Attempting AI response using {}", openAIProvider.getProviderName());
                return openAIProvider.generateResponse(systemInstruction, userPrompt);
            } catch (Exception e) {
                log.warn("OpenAI API call failed or timed out. Falling back to RuleBasedFallbackProvider: {}", e.getMessage());
                return fallbackProvider.generateResponse(systemInstruction, userPrompt);
            }
        } else {
            log.debug("OpenAI API key not provided or placeholder. Using RuleBasedFallbackProvider.");
            return fallbackProvider.generateResponse(systemInstruction, userPrompt);
        }
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public String getProviderName() {
        return openAIProvider.isAvailable() ? openAIProvider.getProviderName() : fallbackProvider.getProviderName();
    }
}

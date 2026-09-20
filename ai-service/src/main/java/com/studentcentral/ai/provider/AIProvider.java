package com.studentcentral.ai.provider;

public interface AIProvider {

    /**
     * Generates a natural language response given a system instruction and user prompt.
     */
    String generateResponse(String systemInstruction, String userPrompt);

    /**
     * Checks if this provider is actively configured and reachable.
     */
    boolean isAvailable();

    /**
     * Identifies the provider for audit logging.
     */
    String getProviderName();
}

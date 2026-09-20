package com.studentcentral.ai.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class AiChatMessage {

    private String role; // "user" or "assistant" or "system"
    private String content;
    private Instant timestamp;
    private List<String> suggestedActions = new ArrayList<>();
    private List<String> toolsUsed = new ArrayList<>();

    public AiChatMessage() {
        this.timestamp = Instant.now();
    }

    public AiChatMessage(String role, String content) {
        this.role = role;
        this.content = content;
        this.timestamp = Instant.now();
    }

    public AiChatMessage(String role, String content, List<String> suggestedActions, List<String> toolsUsed) {
        this.role = role;
        this.content = content;
        this.timestamp = Instant.now();
        if (suggestedActions != null) this.suggestedActions = suggestedActions;
        if (toolsUsed != null) this.toolsUsed = toolsUsed;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public List<String> getSuggestedActions() {
        return suggestedActions;
    }

    public void setSuggestedActions(List<String> suggestedActions) {
        this.suggestedActions = suggestedActions;
    }

    public List<String> getToolsUsed() {
        return toolsUsed;
    }

    public void setToolsUsed(List<String> toolsUsed) {
        this.toolsUsed = toolsUsed;
    }
}

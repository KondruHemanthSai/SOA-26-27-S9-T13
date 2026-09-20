package com.studentcentral.ai.dto;

import jakarta.validation.constraints.NotBlank;

public class GenerateAnnouncementRequest {

    @NotBlank(message = "Topic cannot be blank")
    private String topic;

    private String audience = "ALL_STUDENTS";
    private String tone = "PROFESSIONAL";

    public GenerateAnnouncementRequest() {
    }

    public GenerateAnnouncementRequest(String topic, String audience, String tone) {
        this.topic = topic;
        this.audience = audience;
        this.tone = tone;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getAudience() {
        return audience;
    }

    public void setAudience(String audience) {
        this.audience = audience;
    }

    public String getTone() {
        return tone;
    }

    public void setTone(String tone) {
        this.tone = tone;
    }
}

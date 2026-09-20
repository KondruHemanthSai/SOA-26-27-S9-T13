package com.studentcentral.ai.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ClientNotificationDto {
    private String id;
    private String notificationId;
    private String userId;
    private String title;
    private String message;
    private String type;
    private String priority;
    private boolean isRead;

    public ClientNotificationDto() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getNotificationId() { return notificationId; }
    public void setNotificationId(String notificationId) { this.notificationId = notificationId; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }
}

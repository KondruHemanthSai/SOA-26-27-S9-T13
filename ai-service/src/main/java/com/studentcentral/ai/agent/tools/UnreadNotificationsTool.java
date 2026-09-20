package com.studentcentral.ai.agent.tools;

import com.studentcentral.ai.agent.AgentTool;
import com.studentcentral.ai.client.NotificationServiceClient;
import com.studentcentral.ai.client.dto.ClientNotificationDto;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UnreadNotificationsTool implements AgentTool {

    private final NotificationServiceClient notificationServiceClient;

    public UnreadNotificationsTool(NotificationServiceClient notificationServiceClient) {
        this.notificationServiceClient = notificationServiceClient;
    }

    @Override
    public String getName() {
        return "getUnreadNotifications";
    }

    @Override
    public String getDescription() {
        return "Retrieves the student's unread system notifications, admission alerts, and campus announcements.";
    }

    @Override
    public String execute(String userId, String jwtToken, String parameter) {
        List<ClientNotificationDto> notifs = notificationServiceClient.getUnreadNotifications(userId, jwtToken);
        if (notifs.isEmpty()) {
            return "You have 0 unread notifications. You are completely up to date.";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Unread Notifications (").append(notifs.size()).append("):\n");
        for (ClientNotificationDto n : notifs) {
            sb.append(String.format("- [%s] %s: %s\n", n.getType(), n.getTitle(), n.getMessage()));
        }
        return sb.toString();
    }
}

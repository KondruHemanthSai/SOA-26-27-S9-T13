package com.studentcentral.ai.service;

import com.studentcentral.ai.model.AiAuditLog;
import com.studentcentral.ai.provider.AIProvider;
import com.studentcentral.ai.repository.AiAuditLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminAnnouncementService {

    private static final Logger log = LoggerFactory.getLogger(AdminAnnouncementService.class);

    private final AIProvider aiProvider;
    private final AiAuditLogRepository auditLogRepository;

    public AdminAnnouncementService(AIProvider aiProvider, AiAuditLogRepository auditLogRepository) {
        this.aiProvider = aiProvider;
        this.auditLogRepository = auditLogRepository;
    }

    public record AnnouncementDraftResult(String title, String message, String shortMessage, String audience, String tone) {}

    public AnnouncementDraftResult generateDraft(String adminUserId, String topic, String audience, String tone) {
        long startTime = System.currentTimeMillis();

        String safeTone = (tone != null && !tone.isBlank()) ? tone : "PROFESSIONAL";
        String safeAudience = (audience != null && !audience.isBlank()) ? audience : "ALL_STUDENTS";

        String title;
        String message;
        String shortMessage;

        // If OpenAI is available, generate via prompt; else use structured professional template
        if (aiProvider.isAvailable() && !"RuleBasedFallbackEngine".equalsIgnoreCase(aiProvider.getProviderName())) {
            try {
                String systemInstruction = "You are a university communications director. Draft a campus announcement for: " + topic +
                        ". Tone: " + safeTone + ". Target Audience: " + safeAudience +
                        ". Return your response in three clearly labeled sections: TITLE:, MESSAGE:, SHORT_VERSION:.";
                String prompt = "Generate announcement for topic: " + topic;
                String generated = aiProvider.generateResponse(systemInstruction, prompt);

                title = extractSection(generated, "TITLE:", "MESSAGE:", "Campus Announcement: " + topic);
                message = extractSection(generated, "MESSAGE:", "SHORT_VERSION:", generated);
                shortMessage = extractSection(generated, "SHORT_VERSION:", null, topic);
            } catch (Exception e) {
                log.warn("OpenAI announcement generation failed, falling back to structured template: {}", e.getMessage());
                title = "Important Notice: " + topic;
                message = "Dear Students and Campus Community,\n\nPlease be advised regarding " + topic +
                          ". Check your student dashboard for details and schedule updates.\n\nAcademic Administration";
                shortMessage = "Campus Notice: " + topic;
            }
        } else {
            title = "Campus Announcement: " + topic;
            message = "Dear Students,\n\nWe would like to bring your attention to: " + topic +
                      ". Please review your student portal for further instructions and upcoming deadlines.\n\nBest regards,\nOffice of Academic Affairs";
            shortMessage = "Important update regarding " + topic + ". Check your portal.";
        }

        try {
            auditLogRepository.save(new AiAuditLog(adminUserId, "GENERATE_ANNOUNCEMENT",
                    List.of(), true, aiProvider.getProviderName(), System.currentTimeMillis() - startTime, null));
        } catch (Exception e) {
            log.warn("Failed to save audit log: {}", e.getMessage());
        }

        return new AnnouncementDraftResult(title, message, shortMessage, safeAudience, safeTone);
    }

    private String extractSection(String text, String startMarker, String endMarker, String defaultVal) {
        int start = text.indexOf(startMarker);
        if (start == -1) return defaultVal;
        start += startMarker.length();

        if (endMarker != null) {
            int end = text.indexOf(endMarker, start);
            if (end != -1) {
                return text.substring(start, end).trim();
            }
        }
        return text.substring(start).trim();
    }
}

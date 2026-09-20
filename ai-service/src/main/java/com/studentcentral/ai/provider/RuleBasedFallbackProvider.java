package com.studentcentral.ai.provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class RuleBasedFallbackProvider implements AIProvider {

    private static final Logger log = LoggerFactory.getLogger(RuleBasedFallbackProvider.class);

    @Override
    public String generateResponse(String systemInstruction, String userPrompt) {
        log.info("Generating response via RuleBasedFallbackProvider");
        String promptLower = userPrompt.toLowerCase(Locale.ROOT);

        // Check if factual context was provided
        if (userPrompt.contains("--- STUDENT FACTS FROM SYSTEM ---")) {
            if (promptLower.contains("register") || promptLower.contains("enrolled") || promptLower.contains("taking") || promptLower.contains("course")) {
                return extractRegistrationSummary(userPrompt);
            }
            if (promptLower.contains("admission") || promptLower.contains("status") || promptLower.contains("apply")) {
                return extractAdmissionSummary(userPrompt);
            }
            if (promptLower.contains("schedule") || promptLower.contains("class") || promptLower.contains("timetable") || promptLower.contains("tomorrow") || promptLower.contains("when")) {
                return extractScheduleSummary(userPrompt);
            }
            if (promptLower.contains("notification") || promptLower.contains("alert") || promptLower.contains("unread")) {
                return extractNotificationSummary(userPrompt);
            }
        }

        // Generic helpful assistance
        return "I am your Student Central AI Assistant operating in offline resilience mode. " +
               "You can ask me about your course registrations, weekly timetable, admission status, or course recommendations. " +
               "All your academic records remain secure and accessible.";
    }

    private String extractRegistrationSummary(String text) {
        if (text.contains("Active Registrations (0 courses)")) {
            return "According to your records, you are not currently enrolled in any courses for this semester. You can explore the course catalog to register for available classes.";
        }
        int start = text.indexOf("Active Registrations");
        int end = text.indexOf("Weekly Schedule Sessions");
        if (start != -1 && end != -1 && end > start) {
            String sub = text.substring(start, end).trim();
            return "Here is your current course enrollment status:\n" + sub + "\nRemember the maximum credit cap is 18 credits.";
        }
        return "Your active course enrollments are recorded in your registration dashboard.";
    }

    private String extractAdmissionSummary(String text) {
        int start = text.indexOf("Admission Status:");
        if (start != -1) {
            int end = text.indexOf("\n", start);
            String status = end != -1 ? text.substring(start, end) : text.substring(start);
            return "Here is your current admission status:\n- " + status + "\nIf you need to upload verification documents, please visit the Admission section.";
        }
        return "Your admission record has been submitted and is currently being processed by the admissions committee.";
    }

    private String extractScheduleSummary(String text) {
        if (text.contains("Weekly Schedule Sessions (0 classes)")) {
            return "You do not have any classes scheduled right now. Once you register for active courses with published timetables, your classes will appear here.";
        }
        int start = text.indexOf("Weekly Schedule Sessions");
        int end = text.indexOf("Unread Notifications");
        if (start != -1 && end != -1 && end > start) {
            String sub = text.substring(start, end).trim();
            return "Here is your upcoming weekly timetable:\n" + sub;
        }
        return "Your weekly class timetable is available in the Timetable section.";
    }

    private String extractNotificationSummary(String text) {
        int start = text.indexOf("Unread Notifications:");
        if (start != -1) {
            int end = text.indexOf("\n", start);
            String count = end != -1 ? text.substring(start, end) : text.substring(start);
            return count + ". You can review all recent announcements and updates in the Notifications center.";
        }
        return "You have no critical pending alerts.";
    }

    @Override
    public boolean isAvailable() {
        return true; // Always available as fallback
    }

    @Override
    public String getProviderName() {
        return "RuleBasedFallbackEngine";
    }
}

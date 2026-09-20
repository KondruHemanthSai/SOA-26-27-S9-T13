package com.studentcentral.ai.service;

import com.studentcentral.ai.client.dto.*;
import com.studentcentral.ai.model.AiAuditLog;
import com.studentcentral.ai.model.AiInsight;
import com.studentcentral.ai.model.AiInsightItem;
import com.studentcentral.ai.provider.AIProvider;
import com.studentcentral.ai.repository.AiAuditLogRepository;
import com.studentcentral.ai.repository.AiInsightRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class StudentInsightService {

    private static final Logger log = LoggerFactory.getLogger(StudentInsightService.class);

    private final StudentContextService contextService;
    private final AiInsightRepository insightRepository;
    private final AiAuditLogRepository auditLogRepository;
    private final AIProvider aiProvider;

    public StudentInsightService(StudentContextService contextService,
                                 AiInsightRepository insightRepository,
                                 AiAuditLogRepository auditLogRepository,
                                 AIProvider aiProvider) {
        this.contextService = contextService;
        this.insightRepository = insightRepository;
        this.auditLogRepository = auditLogRepository;
        this.aiProvider = aiProvider;
    }

    public List<AiInsightItem> generateInsights(String userId, String jwtToken) {
        long startTime = System.currentTimeMillis();
        StudentContext context = contextService.buildFullContext(userId, jwtToken);

        List<AiInsightItem> insights = new ArrayList<>();

        ClientStudentDto profile = context.getProfile();
        ClientAdmissionDto admission = context.getAdmission();
        List<ClientRegistrationDto> registrations = context.getRegistrations();
        List<ClientScheduleDto> schedule = context.getSchedule();

        // 1. Check Course Load & Credit Limits
        int activeCredits = registrations.stream()
                .filter(r -> "REGISTERED".equalsIgnoreCase(r.getStatus()) && r.getCredits() != null)
                .mapToInt(ClientRegistrationDto::getCredits)
                .sum();

        if (activeCredits == 0) {
            insights.add(new AiInsightItem(
                    "COURSE_LOAD",
                    "MEDIUM",
                    "Semester Enrollment Pending",
                    "You have 0 active course registrations recorded for this semester. Review the course catalog before enrollment deadlines.",
                    "Browse Available Courses"
            ));
        } else if (activeCredits >= 15) {
            insights.add(new AiInsightItem(
                    "COURSE_LOAD",
                    "MEDIUM",
                    "High Credit Workload",
                    "You are enrolled in " + activeCredits + " credits, approaching the 18 credit institutional cap. Plan your study hours carefully to balance coursework.",
                    "Review Registered Courses"
            ));
        } else {
            insights.add(new AiInsightItem(
                    "COURSE_LOAD",
                    "LOW",
                    "Balanced Course Load",
                    "You are currently taking " + activeCredits + " credits with " + (18 - activeCredits) + " credits of remaining allowable capacity.",
                    "View Course Plan"
            ));
        }

        // 2. Profile Completeness Signal
        if (profile == null || !profile.isProfileCompleted()) {
            insights.add(new AiInsightItem(
                    "PROFILE_STATUS",
                    "MEDIUM",
                    "Incomplete Student Profile",
                    "Some contact or academic record fields in your student profile are missing. Complete your profile to ensure seamless graduation tracking.",
                    "Complete Student Profile"
            ));
        }

        // 3. Admission Lifecycle Signal
        if (admission != null) {
            String status = admission.getStatus();
            if ("CHANGES_REQUESTED".equalsIgnoreCase(status)) {
                insights.add(new AiInsightItem(
                        "ADMISSION_ACTION",
                        "HIGH",
                        "Admission Changes Requested",
                        "The admissions committee requested updates on your application. Review admin feedback and resubmit required documents.",
                        "View Admission Feedback"
                ));
            } else if ("DRAFT".equalsIgnoreCase(status) || "SUBMITTED".equalsIgnoreCase(status)) {
                insights.add(new AiInsightItem(
                        "ADMISSION_ACTION",
                        "LOW",
                        "Application Under Review",
                        "Your admission application is currently in " + status + " status. Monitor your in-app notifications for decisions.",
                        "Check Application Status"
                ));
            }
        }

        // 4. Timetable Session Density Signal
        if (schedule.size() >= 5) {
            insights.add(new AiInsightItem(
                    "SCHEDULE_DENSITY",
                    "LOW",
                    "Active Weekly Timetable",
                    "You have " + schedule.size() + " scheduled lecture sessions across the week. Verify your weekly timetable to avoid back-to-back room transitions.",
                    "View Weekly Timetable"
            ));
        }

        // 5. Unread Alerts Signal
        if (context.getUnreadNotificationCount() > 0) {
            insights.add(new AiInsightItem(
                    "PENDING_ALERTS",
                    "LOW",
                    "Unread Campus Notifications",
                    "You have " + context.getUnreadNotificationCount() + " unread notification(s) waiting in your inbox.",
                    "Open Notifications"
            ));
        }

        // Persist insights
        String insightId = "INS-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String studentId = (profile != null && profile.getStudentId() != null) ? profile.getStudentId() : "N/A";
        AiInsight aiInsight = new AiInsight(insightId, userId, studentId, insights);
        try {
            insightRepository.save(aiInsight);
            auditLogRepository.save(new AiAuditLog(userId, "STUDENT_INSIGHTS",
                    List.of("StudentService", "RegistrationService", "ScheduleService", "AdmissionService"),
                    true, aiProvider.getProviderName(), System.currentTimeMillis() - startTime, null));
        } catch (Exception e) {
            log.warn("Failed to persist insights or audit: {}", e.getMessage());
        }

        return insights;
    }
}

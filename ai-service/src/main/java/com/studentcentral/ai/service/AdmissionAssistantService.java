package com.studentcentral.ai.service;

import com.studentcentral.ai.client.AdmissionServiceClient;
import com.studentcentral.ai.client.dto.ClientAdmissionDto;
import com.studentcentral.ai.model.AiAuditLog;
import com.studentcentral.ai.provider.AIProvider;
import com.studentcentral.ai.repository.AiAuditLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AdmissionAssistantService {

    private static final Logger log = LoggerFactory.getLogger(AdmissionAssistantService.class);

    private final AdmissionServiceClient admissionServiceClient;
    private final AIProvider aiProvider;
    private final AiAuditLogRepository auditLogRepository;

    public AdmissionAssistantService(AdmissionServiceClient admissionServiceClient,
                                     AIProvider aiProvider,
                                     AiAuditLogRepository auditLogRepository) {
        this.admissionServiceClient = admissionServiceClient;
        this.aiProvider = aiProvider;
        this.auditLogRepository = auditLogRepository;
    }

    public record AdmissionGuidanceResult(String status, String explanation, List<String> nextSteps, String remarks) {}

    public AdmissionGuidanceResult provideGuidance(String userId, String jwtToken) {
        long startTime = System.currentTimeMillis();
        Optional<ClientAdmissionDto> appOpt = admissionServiceClient.getMyApplication(userId, jwtToken);

        if (appOpt.isEmpty()) {
            return new AdmissionGuidanceResult(
                    "NOT_FOUND",
                    "You have not submitted an admission application yet. You can apply for your desired academic program in the Admission section.",
                    List.of("Start Application", "Select Academic Program", "Prepare Transcripts"),
                    null
            );
        }

        ClientAdmissionDto app = appOpt.get();
        String status = app.getStatus() != null ? app.getStatus() : "UNKNOWN";
        String remarks = app.getRemarks() != null ? app.getRemarks() : "No reviewer remarks posted yet.";

        String explanation;
        List<String> nextSteps;

        switch (status.toUpperCase()) {
            case "DRAFT":
                explanation = "Your application is saved as a Draft. It has not yet been submitted to the admissions committee for review.";
                nextSteps = List.of("Verify Personal Details", "Upload Academic Transcripts", "Click Submit Application");
                break;
            case "SUBMITTED":
                explanation = "Your application has been received and is queued for verification by the admissions department.";
                nextSteps = List.of("Check status periodically", "Keep verification documents ready if requested");
                break;
            case "UNDER_REVIEW":
                explanation = "An admissions officer is currently evaluating your credentials, statement of purpose, and academic transcripts.";
                nextSteps = List.of("Monitor your notifications for decision updates");
                break;
            case "APPROVED":
                explanation = "Congratulations! Your admission has been approved by the academic committee. You are officially eligible for semester course enrollment.";
                nextSteps = List.of("Explore Course Catalog", "Select Courses for Semester", "Register Courses");
                break;
            case "CHANGES_REQUESTED":
                explanation = "The admissions officer reviewed your application and requested specific changes or document re-uploads: " + remarks;
                nextSteps = List.of("Review remarks carefully", "Update requested fields/documents", "Resubmit application");
                break;
            case "REJECTED":
                explanation = "Unfortunately, your application was not approved for this academic term. Reason: " + remarks;
                nextSteps = List.of("Contact Academic Admissions Office for appeals or future term details");
                break;
            default:
                explanation = "Your application is currently recorded with status: " + status;
                nextSteps = List.of("Check back soon for status updates");
                break;
        }

        try {
            auditLogRepository.save(new AiAuditLog(userId, "ADMISSION_ASSISTANT",
                    List.of("AdmissionService"), true, aiProvider.getProviderName(),
                    System.currentTimeMillis() - startTime, null));
        } catch (Exception e) {
            log.warn("Failed to save audit log: {}", e.getMessage());
        }

        return new AdmissionGuidanceResult(status, explanation, nextSteps, remarks);
    }
}

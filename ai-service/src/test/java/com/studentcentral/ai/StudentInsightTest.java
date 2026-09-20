package com.studentcentral.ai;

import com.studentcentral.ai.client.dto.ClientAdmissionDto;
import com.studentcentral.ai.client.dto.ClientRegistrationDto;
import com.studentcentral.ai.client.dto.ClientStudentDto;
import com.studentcentral.ai.model.AiInsightItem;
import com.studentcentral.ai.provider.RuleBasedFallbackProvider;
import com.studentcentral.ai.repository.AiAuditLogRepository;
import com.studentcentral.ai.repository.AiInsightRepository;
import com.studentcentral.ai.service.StudentContext;
import com.studentcentral.ai.service.StudentContextService;
import com.studentcentral.ai.service.StudentInsightService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

class StudentInsightTest {

    private StudentInsightService service;
    private StudentContextService contextService;
    private AiInsightRepository insightRepository;
    private AiAuditLogRepository auditLogRepository;

    @BeforeEach
    void setUp() {
        contextService = Mockito.mock(StudentContextService.class);
        insightRepository = Mockito.mock(AiInsightRepository.class);
        auditLogRepository = Mockito.mock(AiAuditLogRepository.class);
        RuleBasedFallbackProvider fallbackProvider = new RuleBasedFallbackProvider();

        service = new StudentInsightService(contextService, insightRepository, auditLogRepository, fallbackProvider);
    }

    @Test
    void identifiesIncompleteProfileAndPendingEnrollment() {
        StudentContext context = new StudentContext();

        ClientStudentDto profile = new ClientStudentDto();
        profile.setProfileCompleted(false); // Incomplete
        context.setProfile(profile);

        // 0 active registrations
        context.setRegistrations(List.of());

        ClientAdmissionDto admission = new ClientAdmissionDto();
        admission.setStatus("CHANGES_REQUESTED");
        admission.setRemarks("Please re-upload higher secondary marksheet.");
        context.setAdmission(admission);

        when(contextService.buildFullContext(eq("user-1"), any())).thenReturn(context);

        List<AiInsightItem> insights = service.generateInsights("user-1", "token");

        assertFalse(insights.isEmpty());
        assertTrue(insights.stream().anyMatch(i -> "COURSE_LOAD".equals(i.getType())), "Should detect 0 course enrollment");
        assertTrue(insights.stream().anyMatch(i -> "PROFILE_STATUS".equals(i.getType())), "Should detect incomplete profile");
        assertTrue(insights.stream().anyMatch(i -> "ADMISSION_ACTION".equals(i.getType()) && "HIGH".equals(i.getSeverity())),
                "Should detect HIGH severity admission changes requested");
    }
}

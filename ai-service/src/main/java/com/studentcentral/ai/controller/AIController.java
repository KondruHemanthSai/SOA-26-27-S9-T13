package com.studentcentral.ai.controller;

import com.studentcentral.ai.dto.ChatRequest;
import com.studentcentral.ai.dto.ChatResponse;
import com.studentcentral.ai.dto.GenerateAnnouncementRequest;
import com.studentcentral.ai.model.AiInsightItem;
import com.studentcentral.ai.model.CourseRecommendationItem;
import com.studentcentral.ai.provider.AIProvider;
import com.studentcentral.ai.security.AuthenticatedUser;
import com.studentcentral.ai.service.*;
import com.studentcentral.ai.service.AdminAnnouncementService.AnnouncementDraftResult;
import com.studentcentral.ai.service.AdmissionAssistantService.AdmissionGuidanceResult;
import com.studentcentral.ai.service.AiChatService.ChatResult;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class AIController {

    private static final Logger log = LoggerFactory.getLogger(AIController.class);

    private final AiChatService chatService;
    private final CourseRecommendationService recommendationService;
    private final StudentInsightService insightService;
    private final AdmissionAssistantService admissionAssistantService;
    private final AdminAnnouncementService announcementService;
    private final AIProvider aiProvider;

    public AIController(AiChatService chatService,
                        CourseRecommendationService recommendationService,
                        StudentInsightService insightService,
                        AdmissionAssistantService admissionAssistantService,
                        AdminAnnouncementService announcementService,
                        AIProvider aiProvider) {
        this.chatService = chatService;
        this.recommendationService = recommendationService;
        this.insightService = insightService;
        this.admissionAssistantService = admissionAssistantService;
        this.announcementService = announcementService;
        this.aiProvider = aiProvider;
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "ai-service",
                "provider", aiProvider.getProviderName(),
                "available", aiProvider.isAvailable()
        ));
    }

    @PostMapping("/chat")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ChatResponse> chat(
            @Valid @RequestBody ChatRequest request,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @AuthenticationPrincipal AuthenticatedUser user) {

        String userId = user != null ? user.getUserId() : "anonymous";
        ChatResult result = chatService.handleUserMessage(userId, authHeader, request.getMessage(), request.getConversationId());

        ChatResponse.ChatData data = new ChatResponse.ChatData(
                result.message(),
                result.suggestedActions(),
                result.toolsUsed(),
                result.remainingRateLimit()
        );
        return ResponseEntity.ok(new ChatResponse(result.success(), data));
    }

    @PostMapping("/course-recommendations")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> getCourseRecommendations(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @AuthenticationPrincipal AuthenticatedUser user) {

        String userId = user != null ? user.getUserId() : "anonymous";
        List<CourseRecommendationItem> recommendations = recommendationService.generateRecommendations(userId, authHeader);
        return ResponseEntity.ok(Map.of("recommendations", recommendations));
    }

    @PostMapping("/student-insights")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> getStudentInsights(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @AuthenticationPrincipal AuthenticatedUser user) {

        String userId = user != null ? user.getUserId() : "anonymous";
        List<AiInsightItem> insights = insightService.generateInsights(userId, authHeader);
        return ResponseEntity.ok(Map.of("insights", insights));
    }

    @PostMapping("/admission-assistant")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AdmissionGuidanceResult> getAdmissionGuidance(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @AuthenticationPrincipal AuthenticatedUser user) {

        String userId = user != null ? user.getUserId() : "anonymous";
        AdmissionGuidanceResult guidance = admissionAssistantService.provideGuidance(userId, authHeader);
        return ResponseEntity.ok(guidance);
    }

    @PostMapping("/generate-announcement")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AnnouncementDraftResult> generateAnnouncement(
            @Valid @RequestBody GenerateAnnouncementRequest request,
            @AuthenticationPrincipal AuthenticatedUser user) {

        String adminUserId = user != null ? user.getUserId() : "admin";
        AnnouncementDraftResult draft = announcementService.generateDraft(
                adminUserId,
                request.getTopic(),
                request.getAudience(),
                request.getTone()
        );
        return ResponseEntity.ok(draft);
    }
}

package com.studentcentral.ai.service;

import com.studentcentral.ai.agent.AgentToolRouter;
import com.studentcentral.ai.agent.AgentToolRouter.AgentExecutionResult;
import com.studentcentral.ai.model.AiAuditLog;
import com.studentcentral.ai.model.AiChatMessage;
import com.studentcentral.ai.model.AiConversation;
import com.studentcentral.ai.provider.AIProvider;
import com.studentcentral.ai.repository.AiAuditLogRepository;
import com.studentcentral.ai.repository.AiConversationRepository;
import com.studentcentral.ai.security.RateLimiterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class AiChatService {

    private static final Logger log = LoggerFactory.getLogger(AiChatService.class);

    private final AgentToolRouter toolRouter;
    private final RateLimiterService rateLimiterService;
    private final AiConversationRepository conversationRepository;
    private final AiAuditLogRepository auditLogRepository;
    private final AIProvider aiProvider;

    public AiChatService(AgentToolRouter toolRouter,
                         RateLimiterService rateLimiterService,
                         AiConversationRepository conversationRepository,
                         AiAuditLogRepository auditLogRepository,
                         AIProvider aiProvider) {
        this.toolRouter = toolRouter;
        this.rateLimiterService = rateLimiterService;
        this.conversationRepository = conversationRepository;
        this.auditLogRepository = auditLogRepository;
        this.aiProvider = aiProvider;
    }

    public record ChatResult(boolean success, String message, List<String> suggestedActions, List<String> toolsUsed, int remainingRateLimit) {}

    public ChatResult handleUserMessage(String userId, String jwtToken, String message, String conversationId) {
        long startTime = System.currentTimeMillis();

        // 1. Rate Limiting Check (Max 20 requests per hour)
        if (!rateLimiterService.isAllowed(userId)) {
            log.warn("Rate limit exceeded for user: {}", userId);
            recordAudit(userId, "STUDENT_CHAT", List.of(), false, aiProvider.getProviderName(),
                    System.currentTimeMillis() - startTime, "Rate limit exceeded (20 req/hour)");
            return new ChatResult(
                    false,
                    "You have reached the limit of 20 AI requests per hour. Please try again later or access your records directly from the campus dashboard.",
                    List.of("View Courses", "View Timetable"),
                    List.of(),
                    0
            );
        }

        int remainingRateLimit = rateLimiterService.getRemainingRequests(userId);

        try {
            // 2. Process via Agent Tool Router
            AgentExecutionResult agentResult = toolRouter.processQuery(userId, jwtToken, message);

            // 3. Persist in conversation history
            String convId = (conversationId != null && !conversationId.isBlank())
                    ? conversationId
                    : UUID.randomUUID().toString();

            AiConversation conversation = conversationRepository.findByConversationId(convId)
                    .orElseGet(() -> new AiConversation(convId, userId));

            conversation.addMessage(new AiChatMessage("user", message));
            conversation.addMessage(new AiChatMessage("assistant", agentResult.answer(), agentResult.suggestedActions(), agentResult.toolsUsed()));
            conversationRepository.save(conversation);

            // 4. Audit Log
            long latency = System.currentTimeMillis() - startTime;
            recordAudit(userId, "STUDENT_CHAT", agentResult.toolsUsed(), true, aiProvider.getProviderName(), latency, null);

            return new ChatResult(true, agentResult.answer(), agentResult.suggestedActions(), agentResult.toolsUsed(), remainingRateLimit);
        } catch (Exception e) {
            log.error("AI chat processing failed for user {}: {}", userId, e.getMessage(), e);
            long latency = System.currentTimeMillis() - startTime;
            recordAudit(userId, "STUDENT_CHAT", List.of(), false, aiProvider.getProviderName(), latency, e.getMessage());

            return new ChatResult(
                    true, // Return true with graceful fallback message so UX doesn't break
                    "AI assistance is temporarily operating in resilience mode. Your official student data is completely safe and accessible in the campus portal.",
                    List.of("Explore Courses", "View Timetable"),
                    List.of(),
                    remainingRateLimit
            );
        }
    }

    private void recordAudit(String userId, String feature, List<String> tools, boolean success,
                             String provider, long latency, String errorMessage) {
        try {
            AiAuditLog audit = new AiAuditLog(userId, feature, tools, success, provider, latency, errorMessage);
            auditLogRepository.save(audit);
        } catch (Exception e) {
            log.warn("Failed to write AI audit log: {}", e.getMessage());
        }
    }
}

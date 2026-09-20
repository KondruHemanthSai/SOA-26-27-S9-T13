package com.studentcentral.ai.service;

import com.studentcentral.ai.client.dto.ClientCourseDto;
import com.studentcentral.ai.client.dto.ClientRegistrationDto;
import com.studentcentral.ai.client.dto.ClientScheduleDto;
import com.studentcentral.ai.client.dto.ClientStudentDto;
import com.studentcentral.ai.model.AiAuditLog;
import com.studentcentral.ai.model.AiRecommendation;
import com.studentcentral.ai.model.CourseRecommendationItem;
import com.studentcentral.ai.provider.AIProvider;
import com.studentcentral.ai.repository.AiAuditLogRepository;
import com.studentcentral.ai.repository.AiRecommendationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class CourseRecommendationService {

    private static final Logger log = LoggerFactory.getLogger(CourseRecommendationService.class);

    private final StudentContextService contextService;
    private final AiRecommendationRepository recommendationRepository;
    private final AiAuditLogRepository auditLogRepository;
    private final AIProvider aiProvider;

    public CourseRecommendationService(StudentContextService contextService,
                                       AiRecommendationRepository recommendationRepository,
                                       AiAuditLogRepository auditLogRepository,
                                       AIProvider aiProvider) {
        this.contextService = contextService;
        this.recommendationRepository = recommendationRepository;
        this.auditLogRepository = auditLogRepository;
        this.aiProvider = aiProvider;
    }

    public List<CourseRecommendationItem> generateRecommendations(String userId, String jwtToken) {
        long startTime = System.currentTimeMillis();
        StudentContext context = contextService.buildCoursePathfinderContext(userId, jwtToken);

        ClientStudentDto profile = context.getProfile();
        List<ClientCourseDto> allCourses = context.getAvailableCourses();
        List<ClientRegistrationDto> currentRegistrations = context.getRegistrations();
        List<ClientScheduleDto> currentSchedules = context.getSchedule();

        String dept = (profile != null && profile.getDepartment() != null) ? profile.getDepartment() : "";
        Integer semester = (profile != null && profile.getSemester() != null) ? profile.getSemester() : 1;

        // Collect registered course codes
        Set<String> registeredCodes = currentRegistrations.stream()
                .filter(r -> "REGISTERED".equalsIgnoreCase(r.getStatus()))
                .map(r -> r.getCourseCode() != null ? r.getCourseCode().toUpperCase() : "")
                .collect(Collectors.toSet());

        // Calculate currently enrolled credits
        int currentCredits = currentRegistrations.stream()
                .filter(r -> "REGISTERED".equalsIgnoreCase(r.getStatus()) && r.getCredits() != null)
                .mapToInt(ClientRegistrationDto::getCredits)
                .sum();
        int remainingCreditCapacity = Math.max(0, 18 - currentCredits);

        List<CourseRecommendationItem> recommendations = new ArrayList<>();

        // Evaluate ONLY real courses from Course Service catalog
        for (ClientCourseDto course : allCourses) {
            String code = course.getCourseCode() != null ? course.getCourseCode().toUpperCase() : "";

            // Skip already registered courses
            if (registeredCodes.contains(code)) {
                continue;
            }

            // Skip courses with no seats
            if (course.getAvailableSeats() != null && course.getAvailableSeats() <= 0) {
                continue;
            }

            // Skip courses exceeding credit cap
            int courseCredits = course.getCredits() != null ? course.getCredits() : 3;
            if (courseCredits > remainingCreditCapacity) {
                continue;
            }

            // Calculate Pathfinder suitability score & compile factual explanation
            double confidence = 0.50;
            List<String> reasons = new ArrayList<>();

            // 1. Department match
            if (!dept.isBlank() && course.getDepartment() != null &&
                    course.getDepartment().toLowerCase().contains(dept.toLowerCase())) {
                confidence += 0.20;
                reasons.add("Matches your department (" + dept + ")");
            }

            // 2. Semester alignment
            if (course.getSemester() != null && Math.abs(course.getSemester() - semester) <= 1) {
                confidence += 0.15;
                reasons.add("Aligns with your academic level (Semester " + course.getSemester() + ")");
            }

            // 3. Prerequisites check
            List<String> prereqs = course.getPrerequisiteCodes();
            if (prereqs == null || prereqs.isEmpty()) {
                confidence += 0.10;
                reasons.add("No prerequisites required - immediate enrollment eligible");
            } else {
                reasons.add("Prerequisites required: " + String.join(", ", prereqs));
            }

            // 4. Seats availability
            if (course.getAvailableSeats() != null && course.getAvailableSeats() > 5) {
                confidence += 0.05;
                reasons.add(course.getAvailableSeats() + " open seats available");
            }

            // 5. Credit fitting
            reasons.add("Fits within your remaining credit allowance (" + courseCredits + " credits of " + remainingCreditCapacity + " available)");

            double finalScore = Math.min(0.98, Math.max(0.60, confidence));
            String factualReason = String.join("; ", reasons) + ".";

            recommendations.add(new CourseRecommendationItem(
                    course.getId(),
                    course.getCourseCode(),
                    course.getCourseName(),
                    courseCredits,
                    course.getAvailableSeats(),
                    course.getDepartment(),
                    factualReason,
                    Math.round(finalScore * 100.0) / 100.0
            ));
        }

        // Sort by confidence descending
        recommendations.sort(Comparator.comparingDouble(CourseRecommendationItem::getConfidence).reversed());

        // Limit to top 5 recommendations
        List<CourseRecommendationItem> topRecommendations = recommendations.stream().limit(5).collect(Collectors.toList());

        // Persist recommendations
        String recId = "REC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String studentId = (profile != null && profile.getStudentId() != null) ? profile.getStudentId() : "N/A";
        AiRecommendation aiRec = new AiRecommendation(recId, userId, studentId, topRecommendations);
        try {
            recommendationRepository.save(aiRec);
            auditLogRepository.save(new AiAuditLog(userId, "COURSE_RECOMMENDATIONS",
                    List.of("CourseService", "RegistrationService", "StudentService"),
                    true, aiProvider.getProviderName(), System.currentTimeMillis() - startTime, null));
        } catch (Exception e) {
            log.warn("Failed to save recommendations or audit log: {}", e.getMessage());
        }

        return topRecommendations;
    }
}

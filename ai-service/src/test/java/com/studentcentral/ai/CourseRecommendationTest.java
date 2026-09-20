package com.studentcentral.ai;

import com.studentcentral.ai.client.dto.ClientCourseDto;
import com.studentcentral.ai.client.dto.ClientRegistrationDto;
import com.studentcentral.ai.client.dto.ClientStudentDto;
import com.studentcentral.ai.model.CourseRecommendationItem;
import com.studentcentral.ai.provider.RuleBasedFallbackProvider;
import com.studentcentral.ai.repository.AiAuditLogRepository;
import com.studentcentral.ai.repository.AiRecommendationRepository;
import com.studentcentral.ai.service.CourseRecommendationService;
import com.studentcentral.ai.service.StudentContext;
import com.studentcentral.ai.service.StudentContextService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

class CourseRecommendationTest {

    private CourseRecommendationService service;
    private StudentContextService contextService;
    private AiRecommendationRepository recommendationRepository;
    private AiAuditLogRepository auditLogRepository;

    @BeforeEach
    void setUp() {
        contextService = Mockito.mock(StudentContextService.class);
        recommendationRepository = Mockito.mock(AiRecommendationRepository.class);
        auditLogRepository = Mockito.mock(AiAuditLogRepository.class);
        RuleBasedFallbackProvider fallbackProvider = new RuleBasedFallbackProvider();

        service = new CourseRecommendationService(contextService, recommendationRepository, auditLogRepository, fallbackProvider);
    }

    @Test
    void recommendsEligibleCoursesExcludingAlreadyRegistered() {
        StudentContext context = new StudentContext();

        ClientStudentDto profile = new ClientStudentDto();
        profile.setDepartment("Computer Science");
        profile.setSemester(2);
        context.setProfile(profile);

        ClientRegistrationDto reg = new ClientRegistrationDto();
        reg.setCourseCode("CS101");
        reg.setCredits(3);
        reg.setStatus("REGISTERED");
        context.setRegistrations(List.of(reg));

        ClientCourseDto c1 = new ClientCourseDto();
        c1.setId("1");
        c1.setCourseCode("CS101"); // Already registered
        c1.setCourseName("Intro to CS");
        c1.setDepartment("Computer Science");
        c1.setSemester(1);
        c1.setCredits(3);
        c1.setAvailableSeats(20);

        ClientCourseDto c2 = new ClientCourseDto();
        c2.setId("2");
        c2.setCourseCode("CS201"); // Eligible
        c2.setCourseName("Data Structures");
        c2.setDepartment("Computer Science");
        c2.setSemester(2);
        c2.setCredits(4);
        c2.setAvailableSeats(15);

        context.setAvailableCourses(List.of(c1, c2));

        when(contextService.buildCoursePathfinderContext(eq("user-1"), any())).thenReturn(context);

        List<CourseRecommendationItem> recs = service.generateRecommendations("user-1", "token");

        assertFalse(recs.isEmpty());
        assertEquals("CS201", recs.get(0).getCourseCode(), "Should recommend CS201");
        assertTrue(recs.stream().noneMatch(r -> "CS101".equals(r.getCourseCode())), "CS101 must be excluded since already registered");
        assertNotNull(recs.get(0).getReason());
        assertTrue(recs.get(0).getReason().contains("Computer Science"));
    }

    @Test
    void neverRecommendsCoursesWithZeroSeats() {
        StudentContext context = new StudentContext();

        ClientCourseDto cFull = new ClientCourseDto();
        cFull.setId("10");
        cFull.setCourseCode("CS401");
        cFull.setCourseName("Cloud Computing");
        cFull.setAvailableSeats(0); // Zero seats
        cFull.setCredits(3);

        context.setAvailableCourses(List.of(cFull));

        when(contextService.buildCoursePathfinderContext(eq("user-1"), any())).thenReturn(context);

        List<CourseRecommendationItem> recs = service.generateRecommendations("user-1", "token");
        assertTrue(recs.isEmpty(), "Courses with zero seats must not be recommended");
    }
}

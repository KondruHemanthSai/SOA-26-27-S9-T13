package com.studentcentral.course.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.studentcentral.course.dto.*;
import com.studentcentral.course.model.CourseStatus;
import com.studentcentral.course.model.CourseType;
import com.studentcentral.course.repository.CourseRepository;
import com.studentcentral.course.repository.PrerequisiteRepository;
import com.studentcentral.course.security.AuthenticatedUser;
import com.studentcentral.course.service.CourseService;
import com.studentcentral.course.service.PrerequisiteService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "eureka.client.enabled=false",
        "spring.data.mongodb.uri=mongodb://localhost:27017/test_course_db"
})
@AutoConfigureMockMvc
class CourseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CourseService courseService;

    @MockBean
    private PrerequisiteService prerequisiteService;

    @MockBean
    private CourseRepository courseRepository;

    @MockBean
    private PrerequisiteRepository prerequisiteRepository;

    @Test
    void shouldAllowAdminToCreateCourse() throws Exception {
        AuthenticatedUser admin = new AuthenticatedUser("admin-1", "admin@studentcentral.com", "ADMIN");
        CreateCourseRequest request = new CreateCourseRequest(
                "CS501", "Machine Learning", "ML intro", "CSE", 5, 4, 40, CourseType.CORE, "Dr. Rao"
        );

        CourseResponse response = new CourseResponse(
                "c-1", "CS501", "Machine Learning", "ML intro", "CSE", 5, 4, 40, 40,
                CourseType.CORE, CourseStatus.ACTIVE, "Dr. Rao", List.of(), Instant.now(), Instant.now()
        );

        when(courseService.createCourse(any(CreateCourseRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/courses")
                        .with(user(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.courseCode").value("CS501"))
                .andExpect(jsonPath("$.capacity").value(40))
                .andExpect(jsonPath("$.availableSeats").value(40));
    }

    @Test
    void shouldForbidStudentFromCreatingCourse() throws Exception {
        AuthenticatedUser student = new AuthenticatedUser("student-1", "student@example.com", "STUDENT");
        CreateCourseRequest request = new CreateCourseRequest(
                "CS501", "Machine Learning", "ML intro", "CSE", 5, 4, 40, CourseType.CORE, "Dr. Rao"
        );

        mockMvc.perform(post("/api/courses")
                        .with(user(student))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldRejectInvalidCourseCreationInput() throws Exception {
        AuthenticatedUser admin = new AuthenticatedUser("admin-1", "admin@studentcentral.com", "ADMIN");
        CreateCourseRequest invalid = new CreateCourseRequest("", "", null, "", 0, 0, 0, null, null);

        mockMvc.perform(post("/api/courses")
                        .with(user(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.validationErrors.courseCode").exists())
                .andExpect(jsonPath("$.validationErrors.courseName").exists())
                .andExpect(jsonPath("$.validationErrors.department").exists());
    }

    @Test
    void shouldAllowAuthenticatedUserToListCourses() throws Exception {
        AuthenticatedUser student = new AuthenticatedUser("student-1", "student@example.com", "STUDENT");
        CourseResponse response = new CourseResponse(
                "c-1", "CS501", "Machine Learning", "ML intro", "CSE", 5, 4, 40, 40,
                CourseType.CORE, CourseStatus.ACTIVE, "Dr. Rao", List.of(), Instant.now(), Instant.now()
        );

        when(courseService.listCourses(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(List.of(response));

        mockMvc.perform(get("/api/courses")
                        .param("department", "CSE")
                        .param("semester", "5")
                        .with(user(student)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].courseCode").value("CS501"));
    }

    @Test
    void shouldGetCourseDetails() throws Exception {
        AuthenticatedUser student = new AuthenticatedUser("student-1", "student@example.com", "STUDENT");
        CourseResponse response = new CourseResponse(
                "c-1", "CS501", "Machine Learning", "ML intro", "CSE", 5, 4, 40, 40,
                CourseType.CORE, CourseStatus.ACTIVE, "Dr. Rao", List.of(), Instant.now(), Instant.now()
        );

        when(courseService.getCourse("CS501")).thenReturn(response);

        mockMvc.perform(get("/api/courses/CS501")
                        .with(user(student)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.courseCode").value("CS501"));
    }

    @Test
    void shouldAllowAdminToUpdateCourse() throws Exception {
        AuthenticatedUser admin = new AuthenticatedUser("admin-1", "admin@studentcentral.com", "ADMIN");
        UpdateCourseRequest request = new UpdateCourseRequest();
        request.setCourseName("Advanced ML");

        CourseResponse response = new CourseResponse(
                "c-1", "CS501", "Advanced ML", "ML intro", "CSE", 5, 4, 40, 40,
                CourseType.CORE, CourseStatus.ACTIVE, "Dr. Rao", List.of(), Instant.now(), Instant.now()
        );

        when(courseService.updateCourse(eq("CS501"), any(UpdateCourseRequest.class)))
                .thenReturn(response);

        mockMvc.perform(put("/api/courses/CS501")
                        .with(user(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.courseName").value("Advanced ML"));
    }

    @Test
    void shouldGetAvailability() throws Exception {
        AuthenticatedUser student = new AuthenticatedUser("student-1", "student@example.com", "STUDENT");
        CourseAvailabilityResponse response = new CourseAvailabilityResponse("c-1", "CS501", 40, 25, true);

        when(courseService.getAvailability("CS501")).thenReturn(response);

        mockMvc.perform(get("/api/courses/CS501/availability")
                        .with(user(student)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.availableSeats").value(25))
                .andExpect(jsonPath("$.isAvailable").value(true));
    }

    @Test
    void shouldAllowAdminToAddPrerequisite() throws Exception {
        AuthenticatedUser admin = new AuthenticatedUser("admin-1", "admin@studentcentral.com", "ADMIN");
        AddPrerequisiteRequest request = new AddPrerequisiteRequest("CS201");
        PrerequisiteResponse response = new PrerequisiteResponse(
                "c-1", "CS501",
                List.of(new PrerequisiteItemResponse("p-1", "c-2", "CS201", "Data Structures", 4))
        );

        when(prerequisiteService.addPrerequisite(eq("CS501"), any(AddPrerequisiteRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/courses/CS501/prerequisites")
                        .with(user(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.courseCode").value("CS501"))
                .andExpect(jsonPath("$.prerequisites[0].courseCode").value("CS201"));
    }

    @Test
    void shouldAllowAdminToDeletePrerequisite() throws Exception {
        AuthenticatedUser admin = new AuthenticatedUser("admin-1", "admin@studentcentral.com", "ADMIN");
        doNothing().when(prerequisiteService).deletePrerequisite("CS501", "p-1");

        mockMvc.perform(delete("/api/courses/CS501/prerequisites/p-1")
                        .with(user(admin)))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldRejectUnauthenticatedRequest() throws Exception {
        mockMvc.perform(get("/api/courses"))
                .andExpect(status().isUnauthorized());
    }
}

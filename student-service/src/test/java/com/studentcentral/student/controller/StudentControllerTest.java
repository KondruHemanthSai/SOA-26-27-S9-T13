package com.studentcentral.student.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.studentcentral.student.dto.*;
import com.studentcentral.student.exception.ForbiddenAccessException;
import com.studentcentral.student.exception.StudentAlreadyExistsException;
import com.studentcentral.student.exception.StudentNotFoundException;
import com.studentcentral.student.model.AdmissionStatus;
import com.studentcentral.student.repository.StudentRepository;
import com.studentcentral.student.security.AuthenticatedUser;
import com.studentcentral.student.service.StudentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "eureka.client.enabled=false",
        "spring.data.mongodb.uri=mongodb://localhost:27017/test_student_db"
})
@AutoConfigureMockMvc
class StudentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private StudentService studentService;

    @MockBean
    private StudentRepository studentRepository;

    @Test
    void shouldCreateStudentProfileSuccessfully() throws Exception {
        AuthenticatedUser user = new AuthenticatedUser("user-101", "student@example.com", "STUDENT");
        CreateStudentRequest request = new CreateStudentRequest(
                "John", "Doe", "+1234567890",
                LocalDate.of(2002, 5, 15),
                "123 Campus Way", "B.Tech Computer Science", "Computer Science", 2026, 1
        );

        StudentResponse response = new StudentResponse(
                "mongo-1", "user-101", "SC20260001",
                "John", "Doe", "student@example.com", "+1234567890",
                LocalDate.of(2002, 5, 15), "123 Campus Way", "B.Tech Computer Science",
                "Computer Science", 2026, 1, AdmissionStatus.PENDING, true, null,
                Instant.now(), Instant.now()
        );

        when(studentService.createProfile(any(AuthenticatedUser.class), any(CreateStudentRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/students/profile")
                        .with(user(user))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("mongo-1"))
                .andExpect(jsonPath("$.studentId").value("SC20260001"))
                .andExpect(jsonPath("$.userId").value("user-101"))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.profileCompleted").value(true));
    }

    @Test
    void shouldRejectProfileCreationWithValidationErrors() throws Exception {
        AuthenticatedUser user = new AuthenticatedUser("user-101", "student@example.com", "STUDENT");
        CreateStudentRequest invalidRequest = new CreateStudentRequest(
                "", "", "invalid-phone", null, null, null, null, 1990, 15
        );

        mockMvc.perform(post("/api/students/profile")
                        .with(user(user))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.validationErrors.firstName").exists())
                .andExpect(jsonPath("$.validationErrors.lastName").exists())
                .andExpect(jsonPath("$.validationErrors.semester").exists());
    }

    @Test
    void shouldGetOwnProfileSuccessfully() throws Exception {
        AuthenticatedUser user = new AuthenticatedUser("user-101", "student@example.com", "STUDENT");
        StudentResponse response = new StudentResponse(
                "mongo-1", "user-101", "SC20260001",
                "John", "Doe", "student@example.com", "+1234567890",
                LocalDate.of(2002, 5, 15), "123 Campus Way", "B.Tech Computer Science",
                "Computer Science", 2026, 1, AdmissionStatus.PENDING, true, null,
                Instant.now(), Instant.now()
        );

        when(studentService.getOwnProfile("user-101")).thenReturn(response);

        mockMvc.perform(get("/api/students/profile")
                        .with(user(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.studentId").value("SC20260001"))
                .andExpect(jsonPath("$.firstName").value("John"));
    }

    @Test
    void shouldReturn404WhenOwnProfileNotFound() throws Exception {
        AuthenticatedUser user = new AuthenticatedUser("user-404", "student@example.com", "STUDENT");

        when(studentService.getOwnProfile("user-404"))
                .thenThrow(new StudentNotFoundException("Student profile not found for user: user-404"));

        mockMvc.perform(get("/api/students/profile")
                        .with(user(user)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("STUDENT_NOT_FOUND"));
    }

    @Test
    void shouldUpdateOwnProfileSuccessfully() throws Exception {
        AuthenticatedUser user = new AuthenticatedUser("user-101", "student@example.com", "STUDENT");
        UpdateStudentRequest request = new UpdateStudentRequest();
        request.setFirstName("Johnny");
        request.setAddress("456 College Rd");

        StudentResponse response = new StudentResponse(
                "mongo-1", "user-101", "SC20260001",
                "Johnny", "Doe", "student@example.com", "+1234567890",
                LocalDate.of(2002, 5, 15), "456 College Rd", "B.Tech Computer Science",
                "Computer Science", 2026, 1, AdmissionStatus.PENDING, true, null,
                Instant.now(), Instant.now()
        );

        when(studentService.updateOwnProfile(eq("user-101"), any(UpdateStudentRequest.class)))
                .thenReturn(response);

        mockMvc.perform(put("/api/students/profile")
                        .with(user(user))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Johnny"))
                .andExpect(jsonPath("$.address").value("456 College Rd"));
    }

    @Test
    void shouldGetOwnAcademicRecordSuccessfully() throws Exception {
        AuthenticatedUser user = new AuthenticatedUser("user-101", "student@example.com", "STUDENT");
        AcademicRecordResponse response = new AcademicRecordResponse("High School X", "CBSE", 95.0, 9.5, 2022);

        when(studentService.getOwnAcademicRecord("user-101")).thenReturn(response);

        mockMvc.perform(get("/api/students/profile/academic-record")
                        .with(user(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.previousInstitution").value("High School X"))
                .andExpect(jsonPath("$.percentage").value(95.0));
    }

    @Test
    void shouldUpdateOwnAcademicRecordSuccessfully() throws Exception {
        AuthenticatedUser user = new AuthenticatedUser("user-101", "student@example.com", "STUDENT");
        AcademicRecordRequest request = new AcademicRecordRequest("High School Y", "State", 90.0, 9.0, 2022);
        AcademicRecordResponse response = new AcademicRecordResponse("High School Y", "State", 90.0, 9.0, 2022);

        when(studentService.updateOwnAcademicRecord(eq("user-101"), any(AcademicRecordRequest.class)))
                .thenReturn(response);

        mockMvc.perform(put("/api/students/profile/academic-record")
                        .with(user(user))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.previousInstitution").value("High School Y"))
                .andExpect(jsonPath("$.percentage").value(90.0));
    }

    @Test
    void shouldAllowAdminToListStudents() throws Exception {
        AuthenticatedUser admin = new AuthenticatedUser("admin-1", "admin@studentcentral.com", "ADMIN");
        StudentResponse s1 = new StudentResponse(
                "mongo-1", "user-101", "SC20260001",
                "John", "Doe", "student@example.com", "+1234567890",
                null, null, null, "CSE", 2026, 1, AdmissionStatus.APPROVED, true, null,
                Instant.now(), Instant.now()
        );

        when(studentService.listStudents(eq("CSE"), eq(AdmissionStatus.APPROVED)))
                .thenReturn(List.of(s1));

        mockMvc.perform(get("/api/students")
                        .param("department", "CSE")
                        .param("status", "APPROVED")
                        .with(user(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].studentId").value("SC20260001"));
    }

    @Test
    void shouldForbidStudentFromListingStudents() throws Exception {
        AuthenticatedUser student = new AuthenticatedUser("user-101", "student@example.com", "STUDENT");

        mockMvc.perform(get("/api/students")
                        .with(user(student)))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldRejectUnauthenticatedRequest() throws Exception {
        mockMvc.perform(get("/api/students/profile"))
                .andExpect(status().isUnauthorized());
    }
}

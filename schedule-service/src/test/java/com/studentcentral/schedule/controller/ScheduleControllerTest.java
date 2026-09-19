package com.studentcentral.schedule.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.studentcentral.schedule.dto.*;
import com.studentcentral.schedule.exception.ClassroomConflictException;
import com.studentcentral.schedule.exception.InvalidTimeRangeException;
import com.studentcentral.schedule.exception.ScheduleConflictException;
import com.studentcentral.schedule.model.DayOfWeek;
import com.studentcentral.schedule.model.ScheduleType;
import com.studentcentral.schedule.repository.ScheduleRepository;
import com.studentcentral.schedule.security.AuthenticatedUser;
import com.studentcentral.schedule.service.ScheduleService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.LocalTime;
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
        "spring.data.mongodb.uri=mongodb://localhost:27017/test_schedule_db"
})
@AutoConfigureMockMvc
class ScheduleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ScheduleService scheduleService;

    @MockBean
    private ScheduleRepository scheduleRepository;

    @Test
    void shouldAllowAdminToCreateSchedule() throws Exception {
        AuthenticatedUser admin = new AuthenticatedUser("admin-001", "admin@example.com", "ADMIN");

        CreateScheduleRequest request = new CreateScheduleRequest();
        request.setCourseId("course-ml-101");
        request.setCourseCode("CS501");
        request.setCourseName("Machine Learning");
        request.setSection("A");
        request.setDayOfWeek(DayOfWeek.MONDAY);
        request.setStartTime(LocalTime.of(10, 0));
        request.setEndTime(LocalTime.of(11, 0));
        request.setClassroom("CR-301");
        request.setAcademicYear("2026-27");

        ScheduleResponse response = new ScheduleResponse();
        response.setId("sch-001");
        response.setCourseId("course-ml-101");
        response.setCourseCode("CS501");
        response.setDayOfWeek(DayOfWeek.MONDAY);
        response.setStartTime(LocalTime.of(10, 0));
        response.setEndTime(LocalTime.of(11, 0));
        response.setClassroom("CR-301");

        when(scheduleService.createSchedule(any(), any())).thenReturn(response);

        mockMvc.perform(post("/api/schedules")
                        .with(user(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("sch-001"))
                .andExpect(jsonPath("$.courseCode").value("CS501"))
                .andExpect(jsonPath("$.classroom").value("CR-301"));
    }

    @Test
    void shouldForbidStudentFromCreatingSchedule() throws Exception {
        AuthenticatedUser student = new AuthenticatedUser("user-101", "student@example.com", "STUDENT");

        CreateScheduleRequest request = new CreateScheduleRequest();
        request.setCourseId("course-ml-101");
        request.setDayOfWeek(DayOfWeek.MONDAY);
        request.setStartTime(LocalTime.of(10, 0));
        request.setEndTime(LocalTime.of(11, 0));
        request.setClassroom("CR-301");
        request.setAcademicYear("2026-27");

        mockMvc.perform(post("/api/schedules")
                        .with(user(student))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldAllowAdminToUpdateSchedule() throws Exception {
        AuthenticatedUser admin = new AuthenticatedUser("admin-001", "admin@example.com", "ADMIN");

        UpdateScheduleRequest request = new UpdateScheduleRequest();
        request.setClassroom("CR-305");

        ScheduleResponse response = new ScheduleResponse();
        response.setId("sch-001");
        response.setClassroom("CR-305");

        when(scheduleService.updateSchedule(any(), eq("sch-001"), any())).thenReturn(response);

        mockMvc.perform(put("/api/schedules/sch-001")
                        .with(user(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.classroom").value("CR-305"));
    }

    @Test
    void shouldAllowAdminToDeleteSchedule() throws Exception {
        AuthenticatedUser admin = new AuthenticatedUser("admin-001", "admin@example.com", "ADMIN");

        doNothing().when(scheduleService).deleteSchedule(any(), eq("sch-001"));

        mockMvc.perform(delete("/api/schedules/sch-001")
                        .with(user(admin)))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldAllowAuthenticatedUserToGetScheduleById() throws Exception {
        AuthenticatedUser student = new AuthenticatedUser("user-101", "student@example.com", "STUDENT");

        ScheduleResponse response = new ScheduleResponse();
        response.setId("sch-001");
        response.setCourseCode("CS501");

        when(scheduleService.getScheduleById("sch-001")).thenReturn(response);

        mockMvc.perform(get("/api/schedules/sch-001")
                        .with(user(student)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("sch-001"))
                .andExpect(jsonPath("$.courseCode").value("CS501"));
    }

    @Test
    void shouldAllowAuthenticatedUserToGetCourseSchedules() throws Exception {
        AuthenticatedUser student = new AuthenticatedUser("user-101", "student@example.com", "STUDENT");

        ScheduleResponse response = new ScheduleResponse();
        response.setId("sch-001");
        response.setCourseCode("CS501");

        when(scheduleService.getSchedulesByCourse("CS501")).thenReturn(List.of(response));

        mockMvc.perform(get("/api/schedules/course/CS501")
                        .with(user(student)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].courseCode").value("CS501"));
    }

    @Test
    void shouldAllowStudentToGetPersonalizedTimetable() throws Exception {
        AuthenticatedUser student = new AuthenticatedUser("user-101", "student@example.com", "STUDENT");

        ScheduleResponse response = new ScheduleResponse();
        response.setId("sch-001");
        response.setCourseCode("CS501");

        when(scheduleService.getMyTimetable(any())).thenReturn(List.of(response));

        mockMvc.perform(get("/api/schedules/my")
                        .with(user(student)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].courseCode").value("CS501"));
    }

    @Test
    void shouldPerformConflictCheckEndpoint() throws Exception {
        AuthenticatedUser internalUser = new AuthenticatedUser("reg-service", "internal@campus.local", "SERVICE");

        ScheduleConflictRequest request = new ScheduleConflictRequest("SC20260001", "course-ml-101");
        ConflictingCourseResponse conflict = new ConflictingCourseResponse(
                "course-db-102", "CS402", "Database Systems", "MONDAY", "10:00", "11:00");
        ScheduleConflictResponse response = new ScheduleConflictResponse(true, conflict);

        when(scheduleService.checkStudentScheduleConflict(any(), any())).thenReturn(response);

        mockMvc.perform(post("/api/schedules/check-conflict")
                        .with(user(internalUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.conflict").value(true))
                .andExpect(jsonPath("$.conflictingCourse.courseCode").value("CS402"));
    }

    @Test
    void shouldHandleInvalidTimeRangeExceptionProperly() throws Exception {
        AuthenticatedUser admin = new AuthenticatedUser("admin-001", "admin@example.com", "ADMIN");

        CreateScheduleRequest request = new CreateScheduleRequest();
        request.setCourseId("course-ml-101");
        request.setDayOfWeek(DayOfWeek.MONDAY);
        request.setStartTime(LocalTime.of(11, 0));
        request.setEndTime(LocalTime.of(10, 0));
        request.setClassroom("CR-301");
        request.setAcademicYear("2026-27");

        when(scheduleService.createSchedule(any(), any()))
                .thenThrow(new InvalidTimeRangeException("Start time must be strictly before end time"));

        mockMvc.perform(post("/api/schedules")
                        .with(user(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("INVALID_TIME_RANGE"))
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void shouldHandleClassroomConflictProperly() throws Exception {
        AuthenticatedUser admin = new AuthenticatedUser("admin-001", "admin@example.com", "ADMIN");

        CreateScheduleRequest request = new CreateScheduleRequest();
        request.setCourseId("course-ml-101");
        request.setDayOfWeek(DayOfWeek.MONDAY);
        request.setStartTime(LocalTime.of(10, 0));
        request.setEndTime(LocalTime.of(11, 0));
        request.setClassroom("CR-301");
        request.setAcademicYear("2026-27");

        when(scheduleService.createSchedule(any(), any()))
                .thenThrow(new ClassroomConflictException("CR-301 is already occupied"));

        mockMvc.perform(post("/api/schedules")
                        .with(user(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("CLASSROOM_CONFLICT"))
                .andExpect(jsonPath("$.status").value(409));
    }
}

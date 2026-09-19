package com.studentcentral.schedule.service;

import com.studentcentral.schedule.client.CourseServiceClient;
import com.studentcentral.schedule.client.RegistrationServiceClient;
import com.studentcentral.schedule.client.StudentServiceClient;
import com.studentcentral.schedule.client.dto.CourseDto;
import com.studentcentral.schedule.client.dto.StudentProfileDto;
import com.studentcentral.schedule.dto.*;
import com.studentcentral.schedule.exception.*;
import com.studentcentral.schedule.model.DayOfWeek;
import com.studentcentral.schedule.model.Schedule;
import com.studentcentral.schedule.model.ScheduleType;
import com.studentcentral.schedule.repository.ScheduleRepository;
import com.studentcentral.schedule.security.AuthenticatedUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScheduleServiceTest {

    @Mock
    private ScheduleRepository scheduleRepository;

    @Mock
    private CourseServiceClient courseServiceClient;

    @Mock
    private RegistrationServiceClient registrationServiceClient;

    @Mock
    private StudentServiceClient studentServiceClient;

    @Mock
    private MongoTemplate mongoTemplate;

    @InjectMocks
    private ScheduleService scheduleService;

    private AuthenticatedUser adminUser;
    private AuthenticatedUser studentUser;
    private CourseDto mockCourse;

    @BeforeEach
    void setUp() {
        adminUser = new AuthenticatedUser("admin-001", "admin@campus.edu", "ADMIN", "mock-admin-token");
        studentUser = new AuthenticatedUser("user-101", "student@campus.edu", "STUDENT", "mock-student-token");

        mockCourse = new CourseDto();
        mockCourse.setId("course-ml-101");
        mockCourse.setCourseCode("CS501");
        mockCourse.setCourseName("Machine Learning");
        mockCourse.setDepartment("Computer Science");
        mockCourse.setSemester(5);
        mockCourse.setCredits(4);
        mockCourse.setStatus("ACTIVE");
    }

    // ========================================================
    // 1. SCHEDULE CRUD & TIME VALIDATION TESTS
    // ========================================================

    @Test
    void shouldCreateScheduleSuccessfully() {
        CreateScheduleRequest req = new CreateScheduleRequest();
        req.setCourseId("course-ml-101");
        req.setSection("A");
        req.setDayOfWeek(DayOfWeek.MONDAY);
        req.setStartTime(LocalTime.of(10, 0));
        req.setEndTime(LocalTime.of(11, 0));
        req.setClassroom("CR-301");
        req.setBuilding("CSE Block");
        req.setFaculty("Dr. Rao");
        req.setAcademicYear("2026-27");
        req.setScheduleType(ScheduleType.LECTURE);

        when(courseServiceClient.getCourse(eq("course-ml-101"), any())).thenReturn(Optional.of(mockCourse));
        when(scheduleRepository.findByCourseIdAndDayOfWeek("course-ml-101", DayOfWeek.MONDAY)).thenReturn(Collections.emptyList());
        when(scheduleRepository.findByClassroomAndDayOfWeek("CR-301", DayOfWeek.MONDAY)).thenReturn(Collections.emptyList());
        when(scheduleRepository.findByFacultyAndDayOfWeek("Dr. Rao", DayOfWeek.MONDAY)).thenReturn(Collections.emptyList());
        when(scheduleRepository.save(any(Schedule.class))).thenAnswer(invocation -> {
            Schedule s = invocation.getArgument(0);
            s.setId("sch-001");
            return s;
        });

        ScheduleResponse response = scheduleService.createSchedule(adminUser, req);

        assertNotNull(response);
        assertEquals("sch-001", response.getId());
        assertEquals("CS501", response.getCourseCode());
        assertEquals("CR-301", response.getClassroom());
        assertEquals(DayOfWeek.MONDAY, response.getDayOfWeek());
        assertEquals(LocalTime.of(10, 0), response.getStartTime());
        assertEquals(LocalTime.of(11, 0), response.getEndTime());
    }

    @Test
    void shouldRejectScheduleWhenStartTimeIsAfterEndTime() {
        CreateScheduleRequest req = new CreateScheduleRequest();
        req.setCourseId("course-ml-101");
        req.setDayOfWeek(DayOfWeek.MONDAY);
        req.setStartTime(LocalTime.of(11, 0));
        req.setEndTime(LocalTime.of(10, 0)); // invalid
        req.setClassroom("CR-301");
        req.setAcademicYear("2026-27");

        InvalidTimeRangeException ex = assertThrows(InvalidTimeRangeException.class, () ->
                scheduleService.createSchedule(adminUser, req));

        assertTrue(ex.getMessage().contains("must be strictly before"));
    }

    @Test
    void shouldRejectScheduleWhenStartTimeEqualsEndTime() {
        CreateScheduleRequest req = new CreateScheduleRequest();
        req.setCourseId("course-ml-101");
        req.setDayOfWeek(DayOfWeek.MONDAY);
        req.setStartTime(LocalTime.of(10, 0));
        req.setEndTime(LocalTime.of(10, 0)); // invalid
        req.setClassroom("CR-301");
        req.setAcademicYear("2026-27");

        assertThrows(InvalidTimeRangeException.class, () ->
                scheduleService.createSchedule(adminUser, req));
    }

    @Test
    void shouldRejectScheduleWhenCourseNotFound() {
        CreateScheduleRequest req = new CreateScheduleRequest();
        req.setCourseId("non-existent-course");
        req.setDayOfWeek(DayOfWeek.MONDAY);
        req.setStartTime(LocalTime.of(10, 0));
        req.setEndTime(LocalTime.of(11, 0));
        req.setClassroom("CR-301");
        req.setAcademicYear("2026-27");

        when(courseServiceClient.getCourse(eq("non-existent-course"), any())).thenReturn(Optional.empty());

        assertThrows(CourseNotFoundException.class, () ->
                scheduleService.createSchedule(adminUser, req));
    }

    @Test
    void shouldGetScheduleByIdSuccessfully() {
        Schedule schedule = new Schedule("course-ml-101", "CS501", "Machine Learning", "A",
                DayOfWeek.MONDAY, LocalTime.of(10, 0), LocalTime.of(11, 0),
                "CR-301", "CSE Block", "Dr. Rao", 5, "2026-27", ScheduleType.LECTURE);
        schedule.setId("sch-001");

        when(scheduleRepository.findById("sch-001")).thenReturn(Optional.of(schedule));

        ScheduleResponse response = scheduleService.getScheduleById("sch-001");

        assertNotNull(response);
        assertEquals("sch-001", response.getId());
        assertEquals("CS501", response.getCourseCode());
    }

    @Test
    void shouldThrowWhenScheduleNotFoundById() {
        when(scheduleRepository.findById("invalid-id")).thenReturn(Optional.empty());

        assertThrows(ScheduleNotFoundException.class, () ->
                scheduleService.getScheduleById("invalid-id"));
    }

    @Test
    void shouldUpdateScheduleSuccessfully() {
        Schedule existing = new Schedule("course-ml-101", "CS501", "Machine Learning", "A",
                DayOfWeek.MONDAY, LocalTime.of(10, 0), LocalTime.of(11, 0),
                "CR-301", "CSE Block", "Dr. Rao", 5, "2026-27", ScheduleType.LECTURE);
        existing.setId("sch-001");

        UpdateScheduleRequest updateReq = new UpdateScheduleRequest();
        updateReq.setClassroom("CR-302");
        updateReq.setStartTime(LocalTime.of(11, 0));
        updateReq.setEndTime(LocalTime.of(12, 0));

        when(scheduleRepository.findById("sch-001")).thenReturn(Optional.of(existing));
        when(scheduleRepository.findByCourseIdAndDayOfWeek("course-ml-101", DayOfWeek.MONDAY)).thenReturn(List.of(existing));
        when(scheduleRepository.findByClassroomAndDayOfWeek("CR-302", DayOfWeek.MONDAY)).thenReturn(Collections.emptyList());
        when(scheduleRepository.findByFacultyAndDayOfWeek("Dr. Rao", DayOfWeek.MONDAY)).thenReturn(List.of(existing));
        when(scheduleRepository.save(any(Schedule.class))).thenAnswer(i -> i.getArgument(0));

        ScheduleResponse response = scheduleService.updateSchedule(adminUser, "sch-001", updateReq);

        assertNotNull(response);
        assertEquals("CR-302", response.getClassroom());
        assertEquals(LocalTime.of(11, 0), response.getStartTime());
        assertEquals(LocalTime.of(12, 0), response.getEndTime());
    }

    @Test
    void shouldDeleteScheduleSuccessfully() {
        Schedule existing = new Schedule("course-ml-101", "CS501", "Machine Learning", "A",
                DayOfWeek.MONDAY, LocalTime.of(10, 0), LocalTime.of(11, 0),
                "CR-301", "CSE Block", "Dr. Rao", 5, "2026-27", ScheduleType.LECTURE);
        existing.setId("sch-001");

        when(scheduleRepository.findById("sch-001")).thenReturn(Optional.of(existing));

        scheduleService.deleteSchedule(adminUser, "sch-001");

        verify(scheduleRepository, times(1)).delete(existing);
    }

    // ========================================================
    // 2. COURSE CONFLICT TESTS
    // ========================================================

    @Test
    void shouldRejectWhenSameCourseSectionHasOverlappingSlot() {
        Schedule existing = new Schedule("course-ml-101", "CS501", "Machine Learning", "A",
                DayOfWeek.MONDAY, LocalTime.of(10, 0), LocalTime.of(11, 0),
                "CR-301", "CSE Block", "Dr. Rao", 5, "2026-27", ScheduleType.LECTURE);
        existing.setId("sch-existing");

        CreateScheduleRequest req = new CreateScheduleRequest();
        req.setCourseId("course-ml-101");
        req.setSection("A");
        req.setDayOfWeek(DayOfWeek.MONDAY);
        req.setStartTime(LocalTime.of(10, 30));
        req.setEndTime(LocalTime.of(11, 30));
        req.setClassroom("CR-305");
        req.setAcademicYear("2026-27");

        when(courseServiceClient.getCourse(eq("course-ml-101"), any())).thenReturn(Optional.of(mockCourse));
        when(scheduleRepository.findByCourseIdAndDayOfWeek("course-ml-101", DayOfWeek.MONDAY)).thenReturn(List.of(existing));

        assertThrows(ScheduleConflictException.class, () ->
                scheduleService.createSchedule(adminUser, req));
    }

    // ========================================================
    // 3. CLASSROOM CONFLICT TESTS
    // ========================================================

    @Test
    void shouldRejectWhenClassroomIsOccupiedAtSameTime() {
        Schedule existing = new Schedule("course-db-102", "CS402", "Database Systems", "A",
                DayOfWeek.MONDAY, LocalTime.of(10, 0), LocalTime.of(11, 0),
                "CR-301", "CSE Block", "Dr. Sharma", 5, "2026-27", ScheduleType.LECTURE);
        existing.setId("sch-existing");

        CreateScheduleRequest req = new CreateScheduleRequest();
        req.setCourseId("course-ml-101");
        req.setSection("A");
        req.setDayOfWeek(DayOfWeek.MONDAY);
        req.setStartTime(LocalTime.of(10, 30));
        req.setEndTime(LocalTime.of(11, 30));
        req.setClassroom("CR-301"); // same room
        req.setAcademicYear("2026-27");

        when(courseServiceClient.getCourse(eq("course-ml-101"), any())).thenReturn(Optional.of(mockCourse));
        when(scheduleRepository.findByCourseIdAndDayOfWeek("course-ml-101", DayOfWeek.MONDAY)).thenReturn(Collections.emptyList());
        when(scheduleRepository.findByClassroomAndDayOfWeek("CR-301", DayOfWeek.MONDAY)).thenReturn(List.of(existing));

        assertThrows(ClassroomConflictException.class, () ->
                scheduleService.createSchedule(adminUser, req));
    }

    @Test
    void shouldAllowWhenClassroomIsNotOverlappingOnBoundary() {
        Schedule existing = new Schedule("course-db-102", "CS402", "Database Systems", "A",
                DayOfWeek.MONDAY, LocalTime.of(10, 0), LocalTime.of(11, 0),
                "CR-301", "CSE Block", "Dr. Sharma", 5, "2026-27", ScheduleType.LECTURE);
        existing.setId("sch-existing");

        CreateScheduleRequest req = new CreateScheduleRequest();
        req.setCourseId("course-ml-101");
        req.setSection("A");
        req.setDayOfWeek(DayOfWeek.MONDAY);
        req.setStartTime(LocalTime.of(11, 0)); // exact boundary
        req.setEndTime(LocalTime.of(12, 0));
        req.setClassroom("CR-301");
        req.setAcademicYear("2026-27");

        when(courseServiceClient.getCourse(eq("course-ml-101"), any())).thenReturn(Optional.of(mockCourse));
        when(scheduleRepository.findByCourseIdAndDayOfWeek("course-ml-101", DayOfWeek.MONDAY)).thenReturn(Collections.emptyList());
        when(scheduleRepository.findByClassroomAndDayOfWeek("CR-301", DayOfWeek.MONDAY)).thenReturn(List.of(existing));
        when(scheduleRepository.save(any(Schedule.class))).thenAnswer(i -> i.getArgument(0));

        ScheduleResponse response = scheduleService.createSchedule(adminUser, req);
        assertNotNull(response);
    }

    // ========================================================
    // 4. FACULTY CONFLICT TESTS
    // ========================================================

    @Test
    void shouldRejectWhenFacultyIsDoubleBooked() {
        Schedule existing = new Schedule("course-db-102", "CS402", "Database Systems", "A",
                DayOfWeek.MONDAY, LocalTime.of(10, 0), LocalTime.of(11, 0),
                "CR-301", "CSE Block", "Dr. Rao", 5, "2026-27", ScheduleType.LECTURE);
        existing.setId("sch-existing");

        CreateScheduleRequest req = new CreateScheduleRequest();
        req.setCourseId("course-ml-101");
        req.setSection("A");
        req.setDayOfWeek(DayOfWeek.MONDAY);
        req.setStartTime(LocalTime.of(10, 0));
        req.setEndTime(LocalTime.of(11, 0));
        req.setClassroom("CR-302");
        req.setFaculty("Dr. Rao"); // same faculty
        req.setAcademicYear("2026-27");

        when(courseServiceClient.getCourse(eq("course-ml-101"), any())).thenReturn(Optional.of(mockCourse));
        when(scheduleRepository.findByCourseIdAndDayOfWeek("course-ml-101", DayOfWeek.MONDAY)).thenReturn(Collections.emptyList());
        when(scheduleRepository.findByClassroomAndDayOfWeek("CR-302", DayOfWeek.MONDAY)).thenReturn(Collections.emptyList());
        when(scheduleRepository.findByFacultyAndDayOfWeek("Dr. Rao", DayOfWeek.MONDAY)).thenReturn(List.of(existing));

        assertThrows(FacultyScheduleConflictException.class, () ->
                scheduleService.createSchedule(adminUser, req));
    }

    // ========================================================
    // 5. TIME OVERLAP LOGIC TESTS (EXACT BOUNDARIES & CASES)
    // ========================================================

    @Test
    void shouldVerifyOverlapLogicCases() {
        LocalTime s1 = LocalTime.of(10, 0);
        LocalTime e1 = LocalTime.of(11, 0);

        // Exact boundary after -> NO overlap (11:00 - 12:00)
        assertFalse(ScheduleService.isOverlapping(s1, e1, LocalTime.of(11, 0), LocalTime.of(12, 0)));

        // Exact boundary before -> NO overlap (09:00 - 10:00)
        assertFalse(ScheduleService.isOverlapping(s1, e1, LocalTime.of(9, 0), LocalTime.of(10, 0)));

        // Partial overlap end -> OVERLAP (10:30 - 11:30)
        assertTrue(ScheduleService.isOverlapping(s1, e1, LocalTime.of(10, 30), LocalTime.of(11, 30)));

        // Partial overlap start -> OVERLAP (09:30 - 10:30)
        assertTrue(ScheduleService.isOverlapping(s1, e1, LocalTime.of(9, 30), LocalTime.of(10, 30)));

        // Complete containment (requested inside existing) -> OVERLAP (10:15 - 10:45 inside 10:00 - 11:00)
        assertTrue(ScheduleService.isOverlapping(s1, e1, LocalTime.of(10, 15), LocalTime.of(10, 45)));

        // Requested interval encompasses existing -> OVERLAP (09:00 - 12:00 contains 10:00 - 11:00)
        assertTrue(ScheduleService.isOverlapping(s1, e1, LocalTime.of(9, 0), LocalTime.of(12, 0)));

        // Identical intervals -> OVERLAP
        assertTrue(ScheduleService.isOverlapping(s1, e1, LocalTime.of(10, 0), LocalTime.of(11, 0)));
    }

    // ========================================================
    // 6. STUDENT TIMETABLE CONFLICT TESTS
    // ========================================================

    @Test
    void shouldDetectConflictWhenStudentHasRegisteredCourseAtSameTime() {
        Schedule requestedCourseSchedule = new Schedule("course-ml-101", "CS501", "Machine Learning", "A",
                DayOfWeek.MONDAY, LocalTime.of(10, 30), LocalTime.of(11, 30),
                "CR-301", "CSE Block", "Dr. Rao", 5, "2026-27", ScheduleType.LECTURE);

        Schedule registeredCourseSchedule = new Schedule("course-db-102", "CS402", "Database Systems", "A",
                DayOfWeek.MONDAY, LocalTime.of(10, 0), LocalTime.of(11, 0),
                "CR-302", "CSE Block", "Dr. Sharma", 5, "2026-27", ScheduleType.LECTURE);

        when(scheduleRepository.findByCourseId("course-ml-101")).thenReturn(List.of(requestedCourseSchedule));
        when(registrationServiceClient.getActiveCourseIds(eq("SC20260001"), any())).thenReturn(List.of("course-db-102"));
        when(scheduleRepository.findByCourseIdIn(List.of("course-db-102"))).thenReturn(List.of(registeredCourseSchedule));

        ScheduleConflictRequest req = new ScheduleConflictRequest("SC20260001", "course-ml-101");
        ScheduleConflictResponse response = scheduleService.checkStudentScheduleConflict(req, "mock-token");

        assertTrue(response.isConflict());
        assertNotNull(response.getConflictingCourse());
        assertEquals("CS402", response.getConflictingCourse().getCourseCode());
        assertEquals("MONDAY", response.getConflictingCourse().getDayOfWeek());
        assertEquals("10:00", response.getConflictingCourse().getStartTime());
        assertEquals("11:00", response.getConflictingCourse().getEndTime());
    }

    @Test
    void shouldReturnNoConflictWhenStudentHasNoOverlappingSchedules() {
        Schedule requestedCourseSchedule = new Schedule("course-ml-101", "CS501", "Machine Learning", "A",
                DayOfWeek.MONDAY, LocalTime.of(11, 0), LocalTime.of(12, 0),
                "CR-301", "CSE Block", "Dr. Rao", 5, "2026-27", ScheduleType.LECTURE);

        Schedule registeredCourseSchedule = new Schedule("course-db-102", "CS402", "Database Systems", "A",
                DayOfWeek.MONDAY, LocalTime.of(10, 0), LocalTime.of(11, 0),
                "CR-302", "CSE Block", "Dr. Sharma", 5, "2026-27", ScheduleType.LECTURE);

        when(scheduleRepository.findByCourseId("course-ml-101")).thenReturn(List.of(requestedCourseSchedule));
        when(registrationServiceClient.getActiveCourseIds(eq("SC20260001"), any())).thenReturn(List.of("course-db-102"));
        when(scheduleRepository.findByCourseIdIn(List.of("course-db-102"))).thenReturn(List.of(registeredCourseSchedule));

        ScheduleConflictRequest req = new ScheduleConflictRequest("SC20260001", "course-ml-101");
        ScheduleConflictResponse response = scheduleService.checkStudentScheduleConflict(req, "mock-token");

        assertFalse(response.isConflict());
        assertNull(response.getConflictingCourse());
    }

    @Test
    void shouldReturnNoConflictWhenStudentHasNoActiveRegistrations() {
        Schedule requestedCourseSchedule = new Schedule("course-ml-101", "CS501", "Machine Learning", "A",
                DayOfWeek.MONDAY, LocalTime.of(10, 0), LocalTime.of(11, 0),
                "CR-301", "CSE Block", "Dr. Rao", 5, "2026-27", ScheduleType.LECTURE);

        when(scheduleRepository.findByCourseId("course-ml-101")).thenReturn(List.of(requestedCourseSchedule));
        when(registrationServiceClient.getActiveCourseIds(eq("SC20260001"), any())).thenReturn(Collections.emptyList());

        ScheduleConflictRequest req = new ScheduleConflictRequest("SC20260001", "course-ml-101");
        ScheduleConflictResponse response = scheduleService.checkStudentScheduleConflict(req, "mock-token");

        assertFalse(response.isConflict());
    }

    // ========================================================
    // 7. STUDENT TIMETABLE /MY RESOLUTION TESTS
    // ========================================================

    @Test
    void shouldRetrievePersonalizedStudentTimetable() {
        StudentProfileDto profile = new StudentProfileDto();
        profile.setStudentId("SC20260001");
        profile.setUserId("user-101");

        Schedule sch1 = new Schedule("c-1", "CS501", "ML", "A",
                DayOfWeek.MONDAY, LocalTime.of(10, 0), LocalTime.of(11, 0), "CR-301", "CSE", "Dr. Rao", 5, "2026-27", ScheduleType.LECTURE);
        Schedule sch2 = new Schedule("c-2", "CS502", "Cloud", "A",
                DayOfWeek.WEDNESDAY, LocalTime.of(14, 0), LocalTime.of(15, 0), "CR-302", "CSE", "Dr. Kumar", 5, "2026-27", ScheduleType.LECTURE);

        when(studentServiceClient.getStudentProfile(eq("user-101"), any())).thenReturn(Optional.of(profile));
        when(registrationServiceClient.getActiveCourseIds(eq("SC20260001"), any())).thenReturn(List.of("c-1", "c-2"));
        when(scheduleRepository.findByCourseIdIn(List.of("c-1", "c-2"))).thenReturn(List.of(sch1, sch2));

        List<ScheduleResponse> timetable = scheduleService.getMyTimetable(studentUser);

        assertNotNull(timetable);
        assertEquals(2, timetable.size());
        assertEquals("CS501", timetable.get(0).getCourseCode());
        assertEquals("CS502", timetable.get(1).getCourseCode());
    }
}

package com.studentcentral.registration.service;

import com.studentcentral.registration.client.AdmissionServiceClient;
import com.studentcentral.registration.client.CourseServiceClient;
import com.studentcentral.registration.client.ScheduleValidationClient;
import com.studentcentral.registration.client.StudentServiceClient;
import com.studentcentral.registration.client.dto.*;
import com.studentcentral.registration.dto.CreateRegistrationRequest;
import com.studentcentral.registration.dto.RegistrationListResponse;
import com.studentcentral.registration.dto.RegistrationResponse;
import com.studentcentral.registration.dto.RegistrationSuccessResponse;
import com.studentcentral.registration.exception.*;
import com.studentcentral.registration.model.Registration;
import com.studentcentral.registration.model.RegistrationStatus;
import com.studentcentral.registration.repository.RegistrationRepository;
import com.studentcentral.registration.security.AuthenticatedUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegistrationServiceTest {

    @Mock
    private RegistrationRepository registrationRepository;

    @Mock
    private RegistrationIdGenerator registrationIdGenerator;

    @Mock
    private StudentServiceClient studentServiceClient;

    @Mock
    private AdmissionServiceClient admissionServiceClient;

    @Mock
    private CourseServiceClient courseServiceClient;

    @Mock
    private ScheduleValidationClient scheduleValidationClient;

    private RegistrationService registrationService;

    private AuthenticatedUser studentUser;
    private StudentProfileDto eligibleStudent;
    private CourseDto activeCourseML;
    private CourseAvailabilityDto availableSeatsDto;

    @BeforeEach
    void setUp() {
        registrationService = new RegistrationService(
                registrationRepository,
                registrationIdGenerator,
                studentServiceClient,
                admissionServiceClient,
                courseServiceClient,
                scheduleValidationClient,
                24 // maxCreditsPerSemester
        );

        studentUser = new AuthenticatedUser("user-101", "student@example.com", "STUDENT", "mock-jwt-token");

        eligibleStudent = new StudentProfileDto(
                "s-doc-1", "user-101", "SC20260001", "John", "Doe",
                "student@example.com", "CSE", 5, "APPROVED", List.of()
        );

        activeCourseML = new CourseDto(
                "c-ml-1", "CS501", "Machine Learning", 4, 40, 20,
                "ACTIVE", 5, "CSE", List.of()
        );

        availableSeatsDto = new CourseAvailabilityDto("c-ml-1", "CS501", 40, 20, true);
    }

    @Test
    void shouldRegisterCourseSuccessfullyWhenAllValidationsPass() {
        CreateRegistrationRequest request = new CreateRegistrationRequest("CS501");

        when(studentServiceClient.getStudentProfile(eq("user-101"), any())).thenReturn(Optional.of(eligibleStudent));
        when(courseServiceClient.getCourse(eq("CS501"), any())).thenReturn(Optional.of(activeCourseML));
        when(registrationRepository.existsByStudentIdAndCourseIdAndStatus("SC20260001", "c-ml-1", RegistrationStatus.REGISTERED)).thenReturn(false);
        when(registrationRepository.existsByStudentIdAndCourseCodeIgnoreCaseAndStatus("SC20260001", "CS501", RegistrationStatus.REGISTERED)).thenReturn(false);
        when(registrationRepository.findByStudentIdAndSemesterAndStatus("SC20260001", 5, RegistrationStatus.REGISTERED)).thenReturn(List.of());
        when(courseServiceClient.getAvailability(eq("c-ml-1"), any())).thenReturn(Optional.of(availableSeatsDto));
        when(scheduleValidationClient.hasScheduleConflict("SC20260001", "c-ml-1", 5)).thenReturn(false);
        when(courseServiceClient.reserveSeat(eq("c-ml-1"), any())).thenReturn(true);
        when(registrationIdGenerator.generateRegistrationId(anyInt())).thenReturn("SC-REG-2026-00001");
        when(registrationRepository.save(any(Registration.class))).thenAnswer(i -> i.getArgument(0));

        RegistrationSuccessResponse response = registrationService.registerCourse(studentUser, request);

        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals("SC-REG-2026-00001", response.getRegistration().getRegistrationId());
        assertEquals("CS501", response.getRegistration().getCourseCode());
        assertEquals(RegistrationStatus.REGISTERED, response.getRegistration().getStatus());
        verify(courseServiceClient, times(1)).reserveSeat("c-ml-1", "mock-jwt-token");
    }

    @Test
    void shouldRejectWhenStudentNotFound() {
        CreateRegistrationRequest request = new CreateRegistrationRequest("CS501");
        when(studentServiceClient.getStudentProfile(eq("user-101"), any())).thenReturn(Optional.empty());

        assertThrows(StudentNotFoundException.class, () ->
                registrationService.registerCourse(studentUser, request));
    }

    @Test
    void shouldRejectWhenStudentAdmissionIsNotApproved() {
        eligibleStudent.setAdmissionStatus("PENDING");
        when(studentServiceClient.getStudentProfile(eq("user-101"), any())).thenReturn(Optional.of(eligibleStudent));
        when(admissionServiceClient.isAdmissionApproved(any())).thenReturn(false);

        CreateRegistrationRequest request = new CreateRegistrationRequest("CS501");

        assertThrows(StudentNotEligibleException.class, () ->
                registrationService.registerCourse(studentUser, request));
    }

    @Test
    void shouldRejectWhenCourseNotFound() {
        CreateRegistrationRequest request = new CreateRegistrationRequest("NON_EXISTENT");

        when(studentServiceClient.getStudentProfile(eq("user-101"), any())).thenReturn(Optional.of(eligibleStudent));
        when(courseServiceClient.getCourse(eq("NON_EXISTENT"), any())).thenReturn(Optional.empty());

        assertThrows(CourseNotFoundException.class, () ->
                registrationService.registerCourse(studentUser, request));
    }

    @Test
    void shouldRejectWhenCourseIsInactive() {
        activeCourseML.setStatus("INACTIVE");
        CreateRegistrationRequest request = new CreateRegistrationRequest("CS501");

        when(studentServiceClient.getStudentProfile(eq("user-101"), any())).thenReturn(Optional.of(eligibleStudent));
        when(courseServiceClient.getCourse(eq("CS501"), any())).thenReturn(Optional.of(activeCourseML));

        assertThrows(CourseInactiveException.class, () ->
                registrationService.registerCourse(studentUser, request));
    }

    @Test
    void shouldRejectWhenStudentAlreadyRegisteredForCourse() {
        CreateRegistrationRequest request = new CreateRegistrationRequest("CS501");

        when(studentServiceClient.getStudentProfile(eq("user-101"), any())).thenReturn(Optional.of(eligibleStudent));
        when(courseServiceClient.getCourse(eq("CS501"), any())).thenReturn(Optional.of(activeCourseML));
        when(registrationRepository.existsByStudentIdAndCourseIdAndStatus("SC20260001", "c-ml-1", RegistrationStatus.REGISTERED)).thenReturn(true);

        assertThrows(AlreadyRegisteredException.class, () ->
                registrationService.registerCourse(studentUser, request));
    }

    @Test
    void shouldRejectWhenPrerequisitesAreNotMet() {
        CoursePrerequisiteItemDto prereq = new CoursePrerequisiteItemDto("p-1", "c-ds-2", "CS201", "Data Structures", 4);
        activeCourseML.setPrerequisites(List.of(prereq));
        eligibleStudent.setCompletedCourses(List.of()); // no completed courses

        CreateRegistrationRequest request = new CreateRegistrationRequest("CS501");

        when(studentServiceClient.getStudentProfile(eq("user-101"), any())).thenReturn(Optional.of(eligibleStudent));
        when(courseServiceClient.getCourse(eq("CS501"), any())).thenReturn(Optional.of(activeCourseML));
        when(registrationRepository.existsByStudentIdAndCourseIdAndStatus("SC20260001", "c-ml-1", RegistrationStatus.REGISTERED)).thenReturn(false);

        assertThrows(PrerequisiteNotMetException.class, () ->
                registrationService.registerCourse(studentUser, request));
    }

    @Test
    void shouldSucceedWhenPrerequisitesAreMet() {
        CoursePrerequisiteItemDto prereq = new CoursePrerequisiteItemDto("p-1", "c-ds-2", "CS201", "Data Structures", 4);
        activeCourseML.setPrerequisites(List.of(prereq));

        CompletedCourseDto completed = new CompletedCourseDto("c-ds-2", "CS201", "Data Structures", "A", 2);
        eligibleStudent.setCompletedCourses(List.of(completed));

        CreateRegistrationRequest request = new CreateRegistrationRequest("CS501");

        when(studentServiceClient.getStudentProfile(eq("user-101"), any())).thenReturn(Optional.of(eligibleStudent));
        when(courseServiceClient.getCourse(eq("CS501"), any())).thenReturn(Optional.of(activeCourseML));
        when(registrationRepository.existsByStudentIdAndCourseIdAndStatus("SC20260001", "c-ml-1", RegistrationStatus.REGISTERED)).thenReturn(false);
        when(registrationRepository.existsByStudentIdAndCourseCodeIgnoreCaseAndStatus("SC20260001", "CS501", RegistrationStatus.REGISTERED)).thenReturn(false);
        when(registrationRepository.findByStudentIdAndSemesterAndStatus("SC20260001", 5, RegistrationStatus.REGISTERED)).thenReturn(List.of());
        when(courseServiceClient.getAvailability(eq("c-ml-1"), any())).thenReturn(Optional.of(availableSeatsDto));
        when(scheduleValidationClient.hasScheduleConflict("SC20260001", "c-ml-1", 5)).thenReturn(false);
        when(courseServiceClient.reserveSeat(eq("c-ml-1"), any())).thenReturn(true);
        when(registrationIdGenerator.generateRegistrationId(anyInt())).thenReturn("SC-REG-2026-00001");
        when(registrationRepository.save(any(Registration.class))).thenAnswer(i -> i.getArgument(0));

        RegistrationSuccessResponse response = registrationService.registerCourse(studentUser, request);

        assertNotNull(response);
        assertTrue(response.isSuccess());
    }

    @Test
    void shouldRejectWhenCreditLimitIsExceeded() {
        Registration existing1 = new Registration("reg-1", "SC20260001", "user-101", "c-1", "CS502", "Cloud Computing", 5, "2026-27", 12);
        Registration existing2 = new Registration("reg-2", "SC20260001", "user-101", "c-2", "CS503", "Compiler Design", 5, "2026-27", 10);
        // Total currently registered = 22 credits. New course = 4 credits -> 26 > 24 limit

        when(studentServiceClient.getStudentProfile(eq("user-101"), any())).thenReturn(Optional.of(eligibleStudent));
        when(courseServiceClient.getCourse(eq("CS501"), any())).thenReturn(Optional.of(activeCourseML));
        when(registrationRepository.existsByStudentIdAndCourseIdAndStatus("SC20260001", "c-ml-1", RegistrationStatus.REGISTERED)).thenReturn(false);
        when(registrationRepository.existsByStudentIdAndCourseCodeIgnoreCaseAndStatus("SC20260001", "CS501", RegistrationStatus.REGISTERED)).thenReturn(false);
        when(registrationRepository.findByStudentIdAndSemesterAndStatus("SC20260001", 5, RegistrationStatus.REGISTERED))
                .thenReturn(List.of(existing1, existing2));

        CreateRegistrationRequest request = new CreateRegistrationRequest("CS501");

        assertThrows(CreditLimitExceededException.class, () ->
                registrationService.registerCourse(studentUser, request));
    }

    @Test
    void shouldRejectWhenCourseIsFull() {
        availableSeatsDto.setAvailableSeats(0);
        availableSeatsDto.setIsAvailable(false);

        when(studentServiceClient.getStudentProfile(eq("user-101"), any())).thenReturn(Optional.of(eligibleStudent));
        when(courseServiceClient.getCourse(eq("CS501"), any())).thenReturn(Optional.of(activeCourseML));
        when(registrationRepository.existsByStudentIdAndCourseIdAndStatus("SC20260001", "c-ml-1", RegistrationStatus.REGISTERED)).thenReturn(false);
        when(registrationRepository.existsByStudentIdAndCourseCodeIgnoreCaseAndStatus("SC20260001", "CS501", RegistrationStatus.REGISTERED)).thenReturn(false);
        when(registrationRepository.findByStudentIdAndSemesterAndStatus("SC20260001", 5, RegistrationStatus.REGISTERED)).thenReturn(List.of());
        when(courseServiceClient.getAvailability(eq("c-ml-1"), any())).thenReturn(Optional.of(availableSeatsDto));

        CreateRegistrationRequest request = new CreateRegistrationRequest("CS501");

        assertThrows(CourseFullException.class, () ->
                registrationService.registerCourse(studentUser, request));
    }

    @Test
    void shouldCompensateSeatReleaseWhenDatabaseSaveFails() {
        CreateRegistrationRequest request = new CreateRegistrationRequest("CS501");

        when(studentServiceClient.getStudentProfile(eq("user-101"), any())).thenReturn(Optional.of(eligibleStudent));
        when(courseServiceClient.getCourse(eq("CS501"), any())).thenReturn(Optional.of(activeCourseML));
        when(registrationRepository.existsByStudentIdAndCourseIdAndStatus("SC20260001", "c-ml-1", RegistrationStatus.REGISTERED)).thenReturn(false);
        when(registrationRepository.existsByStudentIdAndCourseCodeIgnoreCaseAndStatus("SC20260001", "CS501", RegistrationStatus.REGISTERED)).thenReturn(false);
        when(registrationRepository.findByStudentIdAndSemesterAndStatus("SC20260001", 5, RegistrationStatus.REGISTERED)).thenReturn(List.of());
        when(courseServiceClient.getAvailability(eq("c-ml-1"), any())).thenReturn(Optional.of(availableSeatsDto));
        when(scheduleValidationClient.hasScheduleConflict("SC20260001", "c-ml-1", 5)).thenReturn(false);
        when(courseServiceClient.reserveSeat(eq("c-ml-1"), any())).thenReturn(true);
        when(registrationIdGenerator.generateRegistrationId(anyInt())).thenReturn("SC-REG-2026-00001");
        when(registrationRepository.save(any(Registration.class))).thenThrow(new RuntimeException("DB down"));

        assertThrows(RuntimeException.class, () ->
                registrationService.registerCourse(studentUser, request));

        // Verify compensating rollback release was triggered
        verify(courseServiceClient, times(1)).releaseSeat("c-ml-1", "mock-jwt-token");
    }

    @Test
    void shouldDropRegisteredCourseSuccessfullyAndReleaseSeat() {
        Registration reg = new Registration("SC-REG-2026-00001", "SC20260001", "user-101", "c-ml-1", "CS501", "ML", 5, "2026-27", 4);
        reg.setStatus(RegistrationStatus.REGISTERED);

        when(registrationRepository.findByRegistrationId("SC-REG-2026-00001")).thenReturn(Optional.of(reg));
        when(registrationRepository.save(any(Registration.class))).thenAnswer(i -> i.getArgument(0));

        RegistrationResponse response = registrationService.dropCourse(studentUser, "SC-REG-2026-00001");

        assertNotNull(response);
        assertEquals(RegistrationStatus.DROPPED, response.getStatus());
        verify(courseServiceClient, times(1)).releaseSeat("c-ml-1", "mock-jwt-token");
    }

    @Test
    void shouldRejectDropWhenNotOwnedByStudent() {
        Registration reg = new Registration("SC-REG-2026-00001", "SC20260002", "other-user-999", "c-ml-1", "CS501", "ML", 5, "2026-27", 4);

        when(registrationRepository.findByRegistrationId("SC-REG-2026-00001")).thenReturn(Optional.of(reg));

        assertThrows(RegistrationNotOwnedException.class, () ->
                registrationService.dropCourse(studentUser, "SC-REG-2026-00001"));
    }

    @Test
    void shouldRejectDropWhenAlreadyDropped() {
        Registration reg = new Registration("SC-REG-2026-00001", "SC20260001", "user-101", "c-ml-1", "CS501", "ML", 5, "2026-27", 4);
        reg.setStatus(RegistrationStatus.DROPPED);

        when(registrationRepository.findByRegistrationId("SC-REG-2026-00001")).thenReturn(Optional.of(reg));

        assertThrows(RegistrationAlreadyDroppedException.class, () ->
                registrationService.dropCourse(studentUser, "SC-REG-2026-00001"));
    }

    @Test
    void shouldGetActiveRegistrationsForStudent() {
        Registration regActive = new Registration("SC-REG-2026-00001", "SC20260001", "user-101", "c-ml-1", "CS501", "ML", 5, "2026-27", 4);
        regActive.setStatus(RegistrationStatus.REGISTERED);

        when(studentServiceClient.getStudentProfile(eq("user-101"), any())).thenReturn(Optional.of(eligibleStudent));
        when(registrationRepository.findByStudentIdAndStatus("SC20260001", RegistrationStatus.REGISTERED))
                .thenReturn(List.of(regActive));

        RegistrationListResponse response = registrationService.getMyActiveRegistrations(studentUser);

        assertEquals(1, response.getRegistrations().size());
        assertEquals("CS501", response.getRegistrations().get(0).getCourseCode());
    }
}

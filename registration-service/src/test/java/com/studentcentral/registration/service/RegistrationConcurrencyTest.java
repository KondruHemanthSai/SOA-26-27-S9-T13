package com.studentcentral.registration.service;

import com.studentcentral.registration.client.AdmissionServiceClient;
import com.studentcentral.registration.client.CourseServiceClient;
import com.studentcentral.registration.client.ScheduleValidationClient;
import com.studentcentral.registration.client.StudentServiceClient;
import com.studentcentral.registration.client.dto.CourseAvailabilityDto;
import com.studentcentral.registration.client.dto.CourseDto;
import com.studentcentral.registration.client.dto.ScheduleConflictResult;
import com.studentcentral.registration.client.dto.StudentProfileDto;
import com.studentcentral.registration.dto.CreateRegistrationRequest;
import com.studentcentral.registration.dto.RegistrationSuccessResponse;
import com.studentcentral.registration.exception.CourseFullException;
import com.studentcentral.registration.model.Registration;
import com.studentcentral.registration.model.RegistrationStatus;
import com.studentcentral.registration.repository.RegistrationRepository;
import com.studentcentral.registration.security.AuthenticatedUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegistrationConcurrencyTest {

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

    @BeforeEach
    void setUp() {
        registrationService = new RegistrationService(
                registrationRepository,
                registrationIdGenerator,
                studentServiceClient,
                admissionServiceClient,
                courseServiceClient,
                scheduleValidationClient,
                24
        );
    }

    @Test
    void shouldAllowOnlyOneStudentToRegisterWhenOnlyOneSeatIsAvailableUnderConcurrency() throws Exception {
        AuthenticatedUser studentA = new AuthenticatedUser("user-A", "studentA@example.com", "STUDENT", "token-A");
        AuthenticatedUser studentB = new AuthenticatedUser("user-B", "studentB@example.com", "STUDENT", "token-B");

        StudentProfileDto profileA = new StudentProfileDto("s-A", "user-A", "SC20260001", "Student", "A", "studentA@example.com", "CSE", 5, "APPROVED", List.of());
        StudentProfileDto profileB = new StudentProfileDto("s-B", "user-B", "SC20260002", "Student", "B", "studentB@example.com", "CSE", 5, "APPROVED", List.of());

        CourseDto singleSeatCourse = new CourseDto("c-last-1", "CS599", "Special Seminar", 3, 1, 1, "ACTIVE", 5, "CSE", List.of());
        CourseAvailabilityDto initialAvailability = new CourseAvailabilityDto("c-last-1", "CS599", 1, 1, true);

        when(studentServiceClient.getStudentProfile(eq("user-A"), any())).thenReturn(Optional.of(profileA));
        when(studentServiceClient.getStudentProfile(eq("user-B"), any())).thenReturn(Optional.of(profileB));

        when(courseServiceClient.getCourse(eq("CS599"), any())).thenReturn(Optional.of(singleSeatCourse));
        when(courseServiceClient.getAvailability(eq("c-last-1"), any())).thenReturn(Optional.of(initialAvailability));

        when(registrationRepository.existsByStudentIdAndCourseIdAndStatus(anyString(), anyString(), any(RegistrationStatus.class))).thenReturn(false);
        when(registrationRepository.existsByStudentIdAndCourseCodeIgnoreCaseAndStatus(anyString(), anyString(), any(RegistrationStatus.class))).thenReturn(false);
        when(registrationRepository.findByStudentIdAndSemesterAndStatus(anyString(), anyInt(), any(RegistrationStatus.class))).thenReturn(List.of());
        when(scheduleValidationClient.checkScheduleConflict(anyString(), anyString(), anyInt(), any())).thenReturn(new ScheduleConflictResult(false, null));

        // Atomic seat reservation simulator: only the first call returns true; subsequent call returns false
        AtomicInteger availableSeats = new AtomicInteger(1);
        when(courseServiceClient.reserveSeat(eq("c-last-1"), any())).thenAnswer(invocation -> {
            int current = availableSeats.getAndDecrement();
            return current > 0;
        });

        when(registrationIdGenerator.generateRegistrationId(anyInt())).thenAnswer(i -> "SC-REG-2026-" + System.nanoTime());
        when(registrationRepository.save(any(Registration.class))).thenAnswer(i -> i.getArgument(0));

        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(2);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        Runnable taskA = () -> {
            try {
                startLatch.await();
                RegistrationSuccessResponse resp = registrationService.registerCourse(studentA, new CreateRegistrationRequest("CS599"));
                if (resp.isSuccess()) {
                    successCount.incrementAndGet();
                }
            } catch (CourseFullException e) {
                failureCount.incrementAndGet();
            } catch (Exception ignored) {
            } finally {
                doneLatch.countDown();
            }
        };

        Runnable taskB = () -> {
            try {
                startLatch.await();
                RegistrationSuccessResponse resp = registrationService.registerCourse(studentB, new CreateRegistrationRequest("CS599"));
                if (resp.isSuccess()) {
                    successCount.incrementAndGet();
                }
            } catch (CourseFullException e) {
                failureCount.incrementAndGet();
            } catch (Exception ignored) {
            } finally {
                doneLatch.countDown();
            }
        };

        executor.submit(taskA);
        executor.submit(taskB);

        // Release both threads simultaneously
        startLatch.countDown();
        doneLatch.await(5, TimeUnit.SECONDS);
        executor.shutdown();

        // Exactly 1 must succeed, exactly 1 must fail due to course full
        assertEquals(1, successCount.get(), "Exactly one student should successfully claim the last seat");
        assertEquals(1, failureCount.get(), "The competing student must be rejected with CourseFullException");
    }
}

package com.studentcentral.registration.service;

import com.studentcentral.registration.client.AdmissionServiceClient;
import com.studentcentral.registration.client.CourseServiceClient;
import com.studentcentral.registration.client.NotificationServiceClient;
import com.studentcentral.registration.client.ScheduleValidationClient;
import com.studentcentral.registration.client.StudentServiceClient;
import com.studentcentral.registration.client.dto.*;
import com.studentcentral.registration.dto.*;
import com.studentcentral.registration.exception.*;
import com.studentcentral.registration.model.Registration;
import com.studentcentral.registration.model.RegistrationStatus;
import com.studentcentral.registration.repository.RegistrationRepository;
import com.studentcentral.registration.security.AuthenticatedUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.Year;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class RegistrationService {

    private static final Logger log = LoggerFactory.getLogger(RegistrationService.class);

    private final RegistrationRepository registrationRepository;
    private final RegistrationIdGenerator registrationIdGenerator;
    private final StudentServiceClient studentServiceClient;
    private final AdmissionServiceClient admissionServiceClient;
    private final NotificationServiceClient notificationServiceClient;
    private final CourseServiceClient courseServiceClient;
    private final ScheduleValidationClient scheduleValidationClient;

    private final int maxCreditsPerSemester;

    @org.springframework.beans.factory.annotation.Autowired
    public RegistrationService(
            RegistrationRepository registrationRepository,
            RegistrationIdGenerator registrationIdGenerator,
            StudentServiceClient studentServiceClient,
            AdmissionServiceClient admissionServiceClient,
            CourseServiceClient courseServiceClient,
            ScheduleValidationClient scheduleValidationClient,
            NotificationServiceClient notificationServiceClient,
            @Value("${registration.max-credits-per-semester:24}") int maxCreditsPerSemester) {
        this.registrationRepository = registrationRepository;
        this.registrationIdGenerator = registrationIdGenerator;
        this.studentServiceClient = studentServiceClient;
        this.admissionServiceClient = admissionServiceClient;
        this.courseServiceClient = courseServiceClient;
        this.scheduleValidationClient = scheduleValidationClient;
        this.notificationServiceClient = notificationServiceClient;
        this.maxCreditsPerSemester = maxCreditsPerSemester;
    }

    public RegistrationService(
            RegistrationRepository registrationRepository,
            RegistrationIdGenerator registrationIdGenerator,
            StudentServiceClient studentServiceClient,
            AdmissionServiceClient admissionServiceClient,
            CourseServiceClient courseServiceClient,
            ScheduleValidationClient scheduleValidationClient,
            int maxCreditsPerSemester) {
        this(registrationRepository, registrationIdGenerator, studentServiceClient,
             admissionServiceClient, courseServiceClient, scheduleValidationClient,
             null, maxCreditsPerSemester);
    }

    /**
     * Executes the comprehensive 7-step course registration pipeline.
     */
    public RegistrationSuccessResponse registerCourse(AuthenticatedUser user, CreateRegistrationRequest request) {
        log.info("Initiating course registration for user '{}', course '{}'", user.getUserId(), request.getCourseId());

        // 1. Student Resolution
        StudentProfileDto student = studentServiceClient.getStudentProfile(user.getUserId(), user.getToken())
                .orElseThrow(() -> new StudentNotFoundException("Student profile not found for user: " + user.getUserId()));
        String studentId = student.getStudentId() != null ? student.getStudentId() : student.getId();

        // 2. Admission Eligibility Check
        boolean isAdmissionApproved = "APPROVED".equalsIgnoreCase(student.getAdmissionStatus())
                || admissionServiceClient.isAdmissionApproved(user.getToken());
        if (!isAdmissionApproved) {
            log.warn("Registration rejected: Student '{}' admission status is not APPROVED", studentId);
            throw new StudentNotEligibleException("Student admission is not approved for course registration");
        }

        // 3. Course Verification
        CourseDto course = courseServiceClient.getCourse(request.getCourseId(), user.getToken())
                .orElseThrow(() -> new CourseNotFoundException("Course not found with identifier: " + request.getCourseId()));

        if (!"ACTIVE".equalsIgnoreCase(course.getStatus())) {
            log.warn("Registration rejected: Course '{}' is inactive", course.getCourseCode());
            throw new CourseInactiveException("Course " + course.getCourseCode() + " is currently inactive");
        }

        Integer targetSemester = request.getSemester() != null ? request.getSemester() :
                (course.getSemester() != null ? course.getSemester() :
                        (student.getSemester() != null ? student.getSemester() : 1));
        String academicYear = (request.getAcademicYear() != null && !request.getAcademicYear().isBlank()) ?
                request.getAcademicYear().trim() : "2026-27";

        // 4. Duplicate Registration Check
        boolean isAlreadyRegistered = registrationRepository.existsByStudentIdAndCourseIdAndStatus(studentId, course.getId(), RegistrationStatus.REGISTERED)
                || registrationRepository.existsByStudentIdAndCourseCodeIgnoreCaseAndStatus(studentId, course.getCourseCode(), RegistrationStatus.REGISTERED);
        if (isAlreadyRegistered) {
            log.warn("Registration rejected: Student '{}' already registered for course '{}'", studentId, course.getCourseCode());
            throw new AlreadyRegisteredException("Student already has an active registration for course " + course.getCourseCode());
        }

        // 5. Prerequisite Fulfillment Check
        List<CoursePrerequisiteItemDto> prerequisites = course.getPrerequisites();
        if (prerequisites != null && !prerequisites.isEmpty()) {
            Set<String> completedCodes = student.getCompletedCourses().stream()
                    .map(c -> c.getCourseCode() != null ? c.getCourseCode().toUpperCase().trim() : "")
                    .collect(Collectors.toSet());
            Set<String> completedIds = student.getCompletedCourses().stream()
                    .map(CompletedCourseDto::getCourseId)
                    .collect(Collectors.toSet());

            for (CoursePrerequisiteItemDto prereq : prerequisites) {
                String reqCode = prereq.getCourseCode() != null ? prereq.getCourseCode().toUpperCase().trim() : "";
                String reqId = prereq.getCourseId();
                if (!completedCodes.contains(reqCode) && !completedIds.contains(reqId)) {
                    log.warn("Registration rejected: Student '{}' missing prerequisite '{}' for course '{}'",
                            studentId, reqCode, course.getCourseCode());
                    throw new PrerequisiteNotMetException("Prerequisite not met: Course " + course.getCourseCode()
                            + " requires completion of " + reqCode + " (" + prereq.getCourseName() + ")");
                }
            }
        }

        // 6. Credit Limit Check
        List<Registration> currentRegistrations = registrationRepository.findByStudentIdAndSemesterAndStatus(
                studentId, targetSemester, RegistrationStatus.REGISTERED);
        int currentRegisteredCredits = currentRegistrations.stream()
                .mapToInt(r -> r.getCredits() != null ? r.getCredits() : 0)
                .sum();
        int newCourseCredits = course.getCredits();

        if (currentRegisteredCredits + newCourseCredits > maxCreditsPerSemester) {
            log.warn("Registration rejected: Student '{}' credit limit exceeded ({} + {} > {})",
                    studentId, currentRegisteredCredits, newCourseCredits, maxCreditsPerSemester);
            throw new CreditLimitExceededException("Credit limit exceeded: Current registered credits ("
                    + currentRegisteredCredits + ") + course credits (" + newCourseCredits
                    + ") exceeds maximum limit of " + maxCreditsPerSemester + " credits for semester " + targetSemester);
        }

        // 7. Seat Availability Check
        CourseAvailabilityDto availability = courseServiceClient.getAvailability(course.getId(), user.getToken()).orElse(null);
        if (availability == null || !availability.getIsAvailable() || availability.getAvailableSeats() <= 0) {
            log.warn("Registration rejected: Course '{}' has no available seats", course.getCourseCode());
            throw new CourseFullException("No seats are available for course " + course.getCourseCode());
        }

        // 8. Timetable Conflict Check
        ScheduleConflictResult conflictResult = scheduleValidationClient.checkScheduleConflict(
                studentId, course.getId(), targetSemester, user.getToken());
        if (conflictResult.isConflict()) {
            ConflictingCourseDto conflict = conflictResult.getConflictingCourse();
            String detail;
            if (conflict != null && conflict.getCourseCode() != null) {
                detail = String.format("The selected course conflicts with %s on %s from %s to %s",
                        conflict.getCourseCode(),
                        capitalize(conflict.getDayOfWeek()),
                        conflict.getStartTime(),
                        conflict.getEndTime());
            } else {
                detail = "The selected course conflicts with an already registered course in your schedule";
            }
            log.warn("Registration rejected: Schedule conflict for student '{}', course '{}': {}",
                    studentId, course.getCourseCode(), detail);
            throw new ScheduleConflictException(detail);
        }

        // 9. Atomic Seat Reservation
        boolean reserved = courseServiceClient.reserveSeat(course.getId(), user.getToken());
        if (!reserved) {
            log.warn("Seat reservation failed in Course Service for course '{}'", course.getCourseCode());
            throw new CourseFullException("Failed to reserve seat: Course " + course.getCourseCode() + " is full");
        }

        // 10. Persist Registration Record (with compensating rollback on failure)
        String registrationId = registrationIdGenerator.generateRegistrationId(Year.now().getValue());
        Registration registration = new Registration(
                registrationId,
                studentId,
                user.getUserId(),
                course.getId(),
                course.getCourseCode(),
                course.getCourseName(),
                targetSemester,
                academicYear,
                newCourseCredits
        );

        Registration savedRegistration;
        try {
            savedRegistration = registrationRepository.save(registration);
            log.info("Course registration successfully created: registrationId={}, studentId={}, courseCode={}",
                    savedRegistration.getRegistrationId(), studentId, course.getCourseCode());
        } catch (Exception e) {
            log.error("Failed to save registration record for student {}. Executing compensating seat release for course {}",
                    studentId, course.getCourseCode());
            courseServiceClient.releaseSeat(course.getId(), user.getToken());
            throw e;
        }

        // Best-effort notification — failure does not affect registration
        if (notificationServiceClient != null) {
            try {
                notificationServiceClient.sendRegistrationNotification(
                        user.getUserId(), course.getCourseCode(), course.getCourseName(),
                        savedRegistration.getRegistrationId());
            } catch (Exception e) {
                log.warn("Failed to send registration notification for user {}: {}", user.getUserId(), e.getMessage());
            }
        }

        return new RegistrationSuccessResponse(
                true,
                "Course registered successfully",
                RegistrationResponse.fromModel(savedRegistration)
        );
    }

    /**
     * Drops a registered course, updates status to DROPPED, and releases the reserved seat.
     */
    public RegistrationResponse dropCourse(AuthenticatedUser user, String registrationId) {
        Registration registration = findRegistrationByIdOrRegId(registrationId);

        if (user.isStudent() && !user.getUserId().equals(registration.getUserId())) {
            log.warn("Unauthorized drop attempt by user '{}' on registration '{}'", user.getUserId(), registrationId);
            throw new RegistrationNotOwnedException("You do not own this course registration");
        }

        if (registration.getStatus() != RegistrationStatus.REGISTERED) {
            log.warn("Drop rejected: Registration '{}' is already in status '{}'", registrationId, registration.getStatus());
            throw new RegistrationAlreadyDroppedException("Registration is already in " + registration.getStatus() + " status");
        }

        registration.setStatus(RegistrationStatus.DROPPED);
        registration.setUpdatedAt(Instant.now());
        Registration updated = registrationRepository.save(registration);

        log.info("Registration '{}' marked as DROPPED. Releasing seat for course '{}'", registrationId, registration.getCourseId());
        courseServiceClient.releaseSeat(registration.getCourseId(), user.getToken());

        // Best-effort notification — failure does not affect drop operation
        if (notificationServiceClient != null) {
            try {
                notificationServiceClient.sendDropNotification(
                        user.getUserId(), registration.getCourseCode(), registration.getCourseName(),
                        registration.getRegistrationId());
            } catch (Exception e) {
                log.warn("Failed to send drop notification for user {}: {}", user.getUserId(), e.getMessage());
            }
        }

        return RegistrationResponse.fromModel(updated);
    }

    /**
     * Retrieves all registrations for the authenticated student.
     */
    public RegistrationListResponse getMyRegistrations(AuthenticatedUser user) {
        String studentId = resolveStudentId(user);
        List<Registration> registrations = registrationRepository.findByStudentId(studentId);
        List<RegistrationResponse> responses = registrations.stream()
                .map(RegistrationResponse::fromModel)
                .collect(Collectors.toList());
        return new RegistrationListResponse(studentId, responses);
    }

    /**
     * Retrieves active (REGISTERED) course enrollments for the authenticated student.
     */
    public RegistrationListResponse getMyActiveRegistrations(AuthenticatedUser user) {
        String studentId = resolveStudentId(user);
        List<Registration> registrations = registrationRepository.findByStudentIdAndStatus(studentId, RegistrationStatus.REGISTERED);
        List<RegistrationResponse> responses = registrations.stream()
                .map(RegistrationResponse::fromModel)
                .collect(Collectors.toList());
        return new RegistrationListResponse(studentId, responses);
    }

    /**
     * Retrieves complete registration history (REGISTERED, DROPPED, CANCELLED) for the authenticated student.
     */
    public RegistrationListResponse getMyRegistrationHistory(AuthenticatedUser user) {
        return getMyRegistrations(user);
    }

    /**
     * Retrieves a single registration by ID or registrationId.
     */
    public RegistrationResponse getRegistrationById(AuthenticatedUser user, String registrationId) {
        Registration registration = findRegistrationByIdOrRegId(registrationId);

        if (user.isStudent() && !user.getUserId().equals(registration.getUserId())) {
            log.warn("Access denied for student '{}' viewing registration '{}'", user.getUserId(), registrationId);
            throw new ForbiddenAccessException("You do not have permission to view this registration");
        }

        return RegistrationResponse.fromModel(registration);
    }

    /**
     * Admin: List all registrations with optional filtering by course, semester, and status.
     */
    public List<RegistrationResponse> listAllRegistrations(String courseId, Integer semester, RegistrationStatus status) {
        List<Registration> registrations = registrationRepository.findAll();

        return registrations.stream()
                .filter(r -> {
                    if (courseId != null && !courseId.isBlank()) {
                        if (!r.getCourseId().equalsIgnoreCase(courseId.trim()) && !r.getCourseCode().equalsIgnoreCase(courseId.trim())) return false;
                    }
                    if (semester != null) {
                        if (!r.getSemester().equals(semester)) return false;
                    }
                    if (status != null) {
                        if (r.getStatus() != status) return false;
                    }
                    return true;
                })
                .map(RegistrationResponse::fromModel)
                .collect(Collectors.toList());
    }

    /**
     * Admin: List registrations for a specific student.
     */
    public List<RegistrationResponse> getRegistrationsByStudentId(String studentId) {
        List<Registration> registrations = registrationRepository.findByStudentId(studentId);
        return registrations.stream()
                .map(RegistrationResponse::fromModel)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves active registered course IDs for a student without invoking schedule validation (preventing circular loops).
     */
    public List<String> getActiveCourseIdsByStudentId(String studentId) {
        List<Registration> active = registrationRepository.findByStudentIdAndStatus(studentId, RegistrationStatus.REGISTERED);
        return active.stream()
                .map(Registration::getCourseId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
    }

    private String capitalize(String str) {
        if (str == null || str.isBlank()) return "";
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }

    // ==========================================
    // Internal Helper Methods
    // ==========================================

    private String resolveStudentId(AuthenticatedUser user) {
        Optional<StudentProfileDto> studentOpt = studentServiceClient.getStudentProfile(user.getUserId(), user.getToken());
        if (studentOpt.isPresent() && studentOpt.get().getStudentId() != null) {
            return studentOpt.get().getStudentId();
        }
        return user.getUserId();
    }

    private Registration findRegistrationByIdOrRegId(String idOrRegId) {
        Optional<Registration> byRegId = registrationRepository.findByRegistrationId(idOrRegId.trim());
        if (byRegId.isPresent()) {
            return byRegId.get();
        }
        return registrationRepository.findById(idOrRegId)
                .orElseThrow(() -> new RegistrationNotFoundException("Registration not found with identifier: " + idOrRegId));
    }
}

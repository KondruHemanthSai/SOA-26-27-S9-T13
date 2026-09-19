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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ScheduleService {

    private static final Logger log = LoggerFactory.getLogger(ScheduleService.class);

    private final ScheduleRepository scheduleRepository;
    private final CourseServiceClient courseServiceClient;
    private final RegistrationServiceClient registrationServiceClient;
    private final StudentServiceClient studentServiceClient;
    private final MongoTemplate mongoTemplate;

    public ScheduleService(ScheduleRepository scheduleRepository,
                           CourseServiceClient courseServiceClient,
                           RegistrationServiceClient registrationServiceClient,
                           StudentServiceClient studentServiceClient,
                           MongoTemplate mongoTemplate) {
        this.scheduleRepository = scheduleRepository;
        this.courseServiceClient = courseServiceClient;
        this.registrationServiceClient = registrationServiceClient;
        this.studentServiceClient = studentServiceClient;
        this.mongoTemplate = mongoTemplate;
    }

    /**
     * Creates a new timetable schedule entry. (ADMIN only)
     */
    public ScheduleResponse createSchedule(AuthenticatedUser user, CreateScheduleRequest request) {
        log.info("Creating new schedule for course '{}' on {} from {} to {}",
                request.getCourseId(), request.getDayOfWeek(), request.getStartTime(), request.getEndTime());

        // 1. Validate Time Range: startTime must be strictly before endTime
        validateTimeRange(request.getStartTime(), request.getEndTime());

        // 2. Validate Course Existence via Course Service
        String token = user != null ? user.getToken() : null;
        CourseDto course = courseServiceClient.getCourse(request.getCourseId(), token)
                .orElseThrow(() -> new CourseNotFoundException("Course with ID " + request.getCourseId() + " not found"));

        String courseCode = (request.getCourseCode() != null && !request.getCourseCode().isBlank())
                ? request.getCourseCode().trim() : course.getCourseCode();
        String courseName = (request.getCourseName() != null && !request.getCourseName().isBlank())
                ? request.getCourseName().trim() : course.getCourseName();
        Integer semester = request.getSemester() != null ? request.getSemester() : course.getSemester();
        String section = (request.getSection() != null && !request.getSection().isBlank())
                ? request.getSection().trim() : "A";

        // 3. Course Timetable Conflict: Same course & section cannot have overlapping slots on same day
        checkCourseScheduleConflict(null, course.getId(), section, request.getDayOfWeek(),
                request.getStartTime(), request.getEndTime(), courseCode);

        // 4. Classroom Conflict: Classroom cannot be double-booked on same day/time
        checkClassroomConflict(null, request.getClassroom(), request.getDayOfWeek(),
                request.getStartTime(), request.getEndTime());

        // 5. Faculty Conflict: Faculty member cannot teach two classes simultaneously
        if (request.getFaculty() != null && !request.getFaculty().isBlank()) {
            checkFacultyConflict(null, request.getFaculty().trim(), request.getDayOfWeek(),
                    request.getStartTime(), request.getEndTime());
        }

        Schedule schedule = new Schedule(
                course.getId(),
                courseCode,
                courseName,
                section,
                request.getDayOfWeek(),
                request.getStartTime(),
                request.getEndTime(),
                request.getClassroom().trim(),
                request.getBuilding() != null ? request.getBuilding().trim() : null,
                request.getFaculty() != null ? request.getFaculty().trim() : null,
                semester,
                request.getAcademicYear() != null ? request.getAcademicYear().trim() : "2026-27",
                request.getScheduleType() != null ? request.getScheduleType() : ScheduleType.LECTURE
        );

        Schedule saved = scheduleRepository.save(schedule);
        log.info("Schedule created successfully: id={}, courseCode={}, day={}, time={}-{}",
                saved.getId(), saved.getCourseCode(), saved.getDayOfWeek(), saved.getStartTime(), saved.getEndTime());

        return ScheduleResponse.fromModel(saved);
    }

    /**
     * Updates an existing timetable schedule. (ADMIN only)
     */
    public ScheduleResponse updateSchedule(AuthenticatedUser user, String scheduleId, UpdateScheduleRequest request) {
        log.info("Updating schedule with ID: {}", scheduleId);

        Schedule existing = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ScheduleNotFoundException("Schedule with ID " + scheduleId + " not found"));

        DayOfWeek targetDay = request.getDayOfWeek() != null ? request.getDayOfWeek() : existing.getDayOfWeek();
        LocalTime targetStartTime = request.getStartTime() != null ? request.getStartTime() : existing.getStartTime();
        LocalTime targetEndTime = request.getEndTime() != null ? request.getEndTime() : existing.getEndTime();
        String targetClassroom = (request.getClassroom() != null && !request.getClassroom().isBlank())
                ? request.getClassroom().trim() : existing.getClassroom();
        String targetFaculty = request.getFaculty() != null ? request.getFaculty().trim() : existing.getFaculty();
        String targetSection = (request.getSection() != null && !request.getSection().isBlank())
                ? request.getSection().trim() : existing.getSection();

        // Validate time range
        validateTimeRange(targetStartTime, targetEndTime);

        // Check Course Conflict excluding current schedule
        checkCourseScheduleConflict(scheduleId, existing.getCourseId(), targetSection, targetDay,
                targetStartTime, targetEndTime, existing.getCourseCode());

        // Check Classroom Conflict excluding current schedule
        checkClassroomConflict(scheduleId, targetClassroom, targetDay, targetStartTime, targetEndTime);

        // Check Faculty Conflict excluding current schedule
        if (targetFaculty != null && !targetFaculty.isBlank()) {
            checkFacultyConflict(scheduleId, targetFaculty, targetDay, targetStartTime, targetEndTime);
        }

        // Apply updates
        if (request.getSection() != null) existing.setSection(targetSection);
        if (request.getDayOfWeek() != null) existing.setDayOfWeek(targetDay);
        if (request.getStartTime() != null) existing.setStartTime(targetStartTime);
        if (request.getEndTime() != null) existing.setEndTime(targetEndTime);
        if (request.getClassroom() != null) existing.setClassroom(targetClassroom);
        if (request.getBuilding() != null) existing.setBuilding(request.getBuilding().trim());
        if (request.getFaculty() != null) existing.setFaculty(targetFaculty);
        if (request.getSemester() != null) existing.setSemester(request.getSemester());
        if (request.getAcademicYear() != null) existing.setAcademicYear(request.getAcademicYear().trim());
        if (request.getScheduleType() != null) existing.setScheduleType(request.getScheduleType());
        existing.setUpdatedAt(Instant.now());

        Schedule updated = scheduleRepository.save(existing);
        log.info("Schedule updated successfully: id={}", updated.getId());

        return ScheduleResponse.fromModel(updated);
    }

    /**
     * Deletes an existing timetable schedule. (ADMIN only)
     */
    public void deleteSchedule(AuthenticatedUser user, String scheduleId) {
        log.info("Deleting schedule with ID: {}", scheduleId);
        Schedule existing = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ScheduleNotFoundException("Schedule with ID " + scheduleId + " not found"));
        scheduleRepository.delete(existing);
        log.info("Schedule deleted successfully: id={}", scheduleId);
    }

    /**
     * Retrieves a single schedule by ID.
     */
    public ScheduleResponse getScheduleById(String scheduleId) {
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ScheduleNotFoundException("Schedule with ID " + scheduleId + " not found"));
        return ScheduleResponse.fromModel(schedule);
    }

    /**
     * Retrieves all schedule entries for a specific course (by courseId or courseCode).
     */
    public List<ScheduleResponse> getSchedulesByCourse(String courseIdOrCode) {
        List<Schedule> schedules = scheduleRepository.findByCourseId(courseIdOrCode);
        if (schedules.isEmpty()) {
            schedules = scheduleRepository.findByCourseCode(courseIdOrCode);
        }
        return schedules.stream()
                .sorted(Comparator.comparing(Schedule::getDayOfWeek).thenComparing(Schedule::getStartTime))
                .map(ScheduleResponse::fromModel)
                .collect(Collectors.toList());
    }

    /**
     * Lists schedules with optional multi-attribute filters.
     */
    public List<ScheduleResponse> listSchedules(DayOfWeek dayOfWeek, String courseId, Integer semester,
                                                String academicYear, String classroom, String faculty) {
        Query query = new Query();

        if (dayOfWeek != null) {
            query.addCriteria(Criteria.where("dayOfWeek").is(dayOfWeek));
        }
        if (courseId != null && !courseId.isBlank()) {
            query.addCriteria(new Criteria().orOperator(
                    Criteria.where("courseId").is(courseId.trim()),
                    Criteria.where("courseCode").is(courseId.trim())
            ));
        }
        if (semester != null) {
            query.addCriteria(Criteria.where("semester").is(semester));
        }
        if (academicYear != null && !academicYear.isBlank()) {
            query.addCriteria(Criteria.where("academicYear").is(academicYear.trim()));
        }
        if (classroom != null && !classroom.isBlank()) {
            query.addCriteria(Criteria.where("classroom").regex("^" + classroom.trim() + "$", "i"));
        }
        if (faculty != null && !faculty.isBlank()) {
            query.addCriteria(Criteria.where("faculty").regex("^" + faculty.trim() + "$", "i"));
        }

        List<Schedule> schedules = mongoTemplate.find(query, Schedule.class);
        return schedules.stream()
                .sorted(Comparator.comparing(Schedule::getDayOfWeek).thenComparing(Schedule::getStartTime))
                .map(ScheduleResponse::fromModel)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves the personalized timetable for the authenticated student.
     */
    public List<ScheduleResponse> getMyTimetable(AuthenticatedUser user) {
        String token = user.getToken();
        String studentId = resolveStudentId(user, token);

        // Fetch active registered course IDs from Registration Service
        List<String> activeCourseIds = registrationServiceClient.getActiveCourseIds(studentId, token);
        if (activeCourseIds.isEmpty()) {
            log.info("No active course registrations found for student {}", studentId);
            return Collections.emptyList();
        }

        List<Schedule> schedules = scheduleRepository.findByCourseIdIn(activeCourseIds);
        return schedules.stream()
                .sorted(Comparator.comparing(Schedule::getDayOfWeek).thenComparing(Schedule::getStartTime))
                .map(ScheduleResponse::fromModel)
                .collect(Collectors.toList());
    }

    /**
     * Checks if enrolling in a requested course introduces a timetable conflict with existing registered courses.
     * Service-to-service validation endpoint for Registration Service.
     */
    public ScheduleConflictResponse checkStudentScheduleConflict(ScheduleConflictRequest request, String token) {
        String studentId = request.getStudentId();
        String requestedCourseId = request.getCourseId();

        log.info("Checking schedule conflict for student {} and requested course {}", studentId, requestedCourseId);

        // 1. Get requested course schedules
        List<Schedule> requestedSchedules = scheduleRepository.findByCourseId(requestedCourseId);
        if (requestedSchedules.isEmpty()) {
            requestedSchedules = scheduleRepository.findByCourseCode(requestedCourseId);
        }

        if (requestedSchedules.isEmpty()) {
            log.debug("No schedule entries found for requested course {}", requestedCourseId);
            return new ScheduleConflictResponse(false, null);
        }

        // 2. Get student's active registered course IDs from Registration Service
        List<String> activeCourseIds = registrationServiceClient.getActiveCourseIds(studentId, token);
        if (activeCourseIds.isEmpty()) {
            log.debug("Student {} has no active registered courses", studentId);
            return new ScheduleConflictResponse(false, null);
        }

        // 3. Retrieve all schedules for active registered courses
        List<Schedule> registeredSchedules = scheduleRepository.findByCourseIdIn(activeCourseIds);
        if (registeredSchedules.isEmpty()) {
            log.debug("No schedules found for registered courses of student {}", studentId);
            return new ScheduleConflictResponse(false, null);
        }

        // 4. Perform timetable overlap detection
        for (Schedule req : requestedSchedules) {
            for (Schedule reg : registeredSchedules) {
                // Skip if same course
                if (req.getCourseId().equals(reg.getCourseId())) {
                    continue;
                }

                if (req.getDayOfWeek() == reg.getDayOfWeek() &&
                        isOverlapping(req.getStartTime(), req.getEndTime(), reg.getStartTime(), reg.getEndTime())) {

                    log.warn("Timetable conflict detected: Student {} requested course {} ({} {}-{}) overlaps with registered course {} ({} {}-{})",
                            studentId, req.getCourseCode(), req.getDayOfWeek(), req.getStartTime(), req.getEndTime(),
                            reg.getCourseCode(), reg.getDayOfWeek(), reg.getStartTime(), reg.getEndTime());

                    ConflictingCourseResponse conflictInfo = new ConflictingCourseResponse(
                            reg.getCourseId(),
                            reg.getCourseCode(),
                            reg.getCourseName(),
                            reg.getDayOfWeek().name(),
                            reg.getStartTime().toString(),
                            reg.getEndTime().toString()
                    );
                    return new ScheduleConflictResponse(true, conflictInfo);
                }
            }
        }

        log.info("No timetable conflict found for student {} and course {}", studentId, requestedCourseId);
        return new ScheduleConflictResponse(false, null);
    }

    // ==========================================
    // VALIDATION & OVERLAP LOGIC
    // ==========================================

    /**
     * Validates that startTime is strictly before endTime.
     */
    private void validateTimeRange(LocalTime startTime, LocalTime endTime) {
        if (startTime == null || endTime == null) {
            throw new InvalidTimeRangeException("Start time and end time must not be null");
        }
        if (!startTime.isBefore(endTime)) {
            log.warn("Invalid time range rejected: startTime {} >= endTime {}", startTime, endTime);
            throw new InvalidTimeRangeException("Start time (" + startTime + ") must be strictly before end time (" + endTime + ")");
        }
    }

    /**
     * Core time overlap algorithm:
     * Two intervals [s1, e1) and [s2, e2) overlap if:
     * s1 < e2 AND s2 < e1
     */
    public static boolean isOverlapping(LocalTime start1, LocalTime end1, LocalTime start2, LocalTime end2) {
        return start1.isBefore(end2) && start2.isBefore(end1);
    }

    private void checkCourseScheduleConflict(String currentScheduleId, String courseId, String section,
                                              DayOfWeek dayOfWeek, LocalTime startTime, LocalTime endTime, String courseCode) {
        List<Schedule> schedules = scheduleRepository.findByCourseIdAndDayOfWeek(courseId, dayOfWeek);
        for (Schedule s : schedules) {
            if (currentScheduleId != null && currentScheduleId.equals(s.getId())) {
                continue;
            }
            if (section.equalsIgnoreCase(s.getSection()) && isOverlapping(startTime, endTime, s.getStartTime(), s.getEndTime())) {
                log.warn("Course timetable conflict: Course {} section {} already has schedule on {} {}-{}",
                        courseCode, section, dayOfWeek, s.getStartTime(), s.getEndTime());
                throw new ScheduleConflictException("Course " + courseCode + " section " + section
                        + " already has a schedule on " + dayOfWeek + " from " + s.getStartTime() + " to " + s.getEndTime());
            }
        }
    }

    private void checkClassroomConflict(String currentScheduleId, String classroom, DayOfWeek dayOfWeek,
                                         LocalTime startTime, LocalTime endTime) {
        List<Schedule> schedules = scheduleRepository.findByClassroomAndDayOfWeek(classroom, dayOfWeek);
        for (Schedule s : schedules) {
            if (currentScheduleId != null && currentScheduleId.equals(s.getId())) {
                continue;
            }
            if (isOverlapping(startTime, endTime, s.getStartTime(), s.getEndTime())) {
                log.warn("Classroom conflict: Classroom {} is occupied on {} {}-{} by course {}",
                        classroom, dayOfWeek, s.getStartTime(), s.getEndTime(), s.getCourseCode());
                throw new ClassroomConflictException("Classroom " + classroom + " is already occupied on "
                        + dayOfWeek + " from " + s.getStartTime() + " to " + s.getEndTime() + " by course " + s.getCourseCode());
            }
        }
    }

    private void checkFacultyConflict(String currentScheduleId, String faculty, DayOfWeek dayOfWeek,
                                       LocalTime startTime, LocalTime endTime) {
        List<Schedule> schedules = scheduleRepository.findByFacultyAndDayOfWeek(faculty, dayOfWeek);
        for (Schedule s : schedules) {
            if (currentScheduleId != null && currentScheduleId.equals(s.getId())) {
                continue;
            }
            if (isOverlapping(startTime, endTime, s.getStartTime(), s.getEndTime())) {
                log.warn("Faculty schedule conflict: Faculty {} already assigned on {} {}-{} to course {}",
                        faculty, dayOfWeek, s.getStartTime(), s.getEndTime(), s.getCourseCode());
                throw new FacultyScheduleConflictException("Faculty member " + faculty + " is already assigned to course "
                        + s.getCourseCode() + " on " + dayOfWeek + " from " + s.getStartTime() + " to " + s.getEndTime());
            }
        }
    }

    private String resolveStudentId(AuthenticatedUser user, String token) {
        Optional<StudentProfileDto> profile = studentServiceClient.getStudentProfile(user.getUserId(), token);
        if (profile.isPresent() && profile.get().getStudentId() != null) {
            return profile.get().getStudentId();
        }
        return user.getUserId();
    }
}

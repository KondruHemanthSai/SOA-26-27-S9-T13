package com.studentcentral.course.service;

import com.studentcentral.course.dto.*;
import com.studentcentral.course.exception.*;
import com.studentcentral.course.model.Course;
import com.studentcentral.course.model.CourseStatus;
import com.studentcentral.course.model.CourseType;
import com.studentcentral.course.repository.CourseRepository;
import com.studentcentral.course.security.AuthenticatedUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CourseService {

    private static final Logger log = LoggerFactory.getLogger(CourseService.class);

    private final CourseRepository courseRepository;
    private final PrerequisiteService prerequisiteService;

    public CourseService(CourseRepository courseRepository, PrerequisiteService prerequisiteService) {
        this.courseRepository = courseRepository;
        this.prerequisiteService = prerequisiteService;
    }

    /**
     * Admin: Creates a new course in the catalogue.
     */
    public CourseResponse createCourse(CreateCourseRequest request) {
        String normalizedCode = request.getCourseCode().toUpperCase().trim();

        if (courseRepository.existsByCourseCodeIgnoreCase(normalizedCode)) {
            log.warn("Attempt to create duplicate course with code: {}", normalizedCode);
            throw new DuplicateCourseCodeException("Course with code '" + normalizedCode + "' already exists");
        }

        if (request.getCapacity() == null || request.getCapacity() <= 0) {
            throw new InvalidCapacityException("Course capacity must be greater than zero");
        }

        Course course = new Course(
                normalizedCode,
                request.getCourseName().trim(),
                request.getDescription(),
                request.getDepartment().trim(),
                request.getSemester(),
                request.getCredits(),
                request.getCapacity(),
                request.getCourseType(),
                request.getFaculty()
        );

        Course saved = courseRepository.save(course);
        log.info("Course created successfully: code={}, id={}", saved.getCourseCode(), saved.getId());
        return CourseResponse.fromModel(saved, List.of());
    }

    /**
     * Retrieves course information by MongoDB ID or courseCode.
     */
    public CourseResponse getCourse(String idOrCode) {
        Course course = findCourseByIdOrCode(idOrCode);
        List<PrerequisiteItemResponse> prerequisites = prerequisiteService.getPrerequisiteItemResponses(course.getId());
        return CourseResponse.fromModel(course, prerequisites);
    }

    /**
     * Admin: Updates an existing course with capacity and availability recalculation.
     */
    public CourseResponse updateCourse(String idOrCode, UpdateCourseRequest request) {
        Course course = findCourseByIdOrCode(idOrCode);

        if (request.getCourseName() != null && !request.getCourseName().isBlank()) {
            course.setCourseName(request.getCourseName().trim());
        }
        if (request.getDescription() != null) {
            course.setDescription(request.getDescription());
        }
        if (request.getDepartment() != null && !request.getDepartment().isBlank()) {
            course.setDepartment(request.getDepartment().trim());
        }
        if (request.getSemester() != null) {
            course.setSemester(request.getSemester());
        }
        if (request.getCredits() != null) {
            course.setCredits(request.getCredits());
        }
        if (request.getCourseType() != null) {
            course.setCourseType(request.getCourseType());
        }
        if (request.getStatus() != null) {
            course.setStatus(request.getStatus());
        }
        if (request.getFaculty() != null) {
            course.setFaculty(request.getFaculty());
        }

        if (request.getCapacity() != null) {
            if (request.getCapacity() <= 0) {
                throw new InvalidCapacityException("Capacity must be greater than zero");
            }
            int currentCapacity = course.getCapacity();
            int currentAvailable = course.getAvailableSeats() != null ? course.getAvailableSeats() : currentCapacity;
            int occupiedSeats = Math.max(0, currentCapacity - currentAvailable);

            if (request.getCapacity() < occupiedSeats) {
                log.warn("Cannot reduce capacity of course {} to {} below occupied seats {}", course.getCourseCode(), request.getCapacity(), occupiedSeats);
                throw new InvalidCapacityException("New capacity (" + request.getCapacity() + ") cannot be less than currently occupied seats (" + occupiedSeats + ")");
            }

            int newAvailableSeats = request.getCapacity() - occupiedSeats;
            course.setCapacity(request.getCapacity());
            course.setAvailableSeats(newAvailableSeats);
        }

        course.setUpdatedAt(Instant.now());
        Course updated = courseRepository.save(course);
        log.info("Course {} updated successfully", updated.getCourseCode());

        List<PrerequisiteItemResponse> prerequisites = prerequisiteService.getPrerequisiteItemResponses(updated.getId());
        return CourseResponse.fromModel(updated, prerequisites);
    }

    /**
     * Admin: Activates a course.
     */
    public CourseResponse activateCourse(String idOrCode) {
        Course course = findCourseByIdOrCode(idOrCode);
        course.setStatus(CourseStatus.ACTIVE);
        course.setUpdatedAt(Instant.now());
        Course saved = courseRepository.save(course);
        log.info("Course {} activated", saved.getCourseCode());
        List<PrerequisiteItemResponse> prerequisites = prerequisiteService.getPrerequisiteItemResponses(saved.getId());
        return CourseResponse.fromModel(saved, prerequisites);
    }

    /**
     * Admin: Deactivates a course.
     */
    public CourseResponse deactivateCourse(String idOrCode) {
        Course course = findCourseByIdOrCode(idOrCode);
        course.setStatus(CourseStatus.INACTIVE);
        course.setUpdatedAt(Instant.now());
        Course saved = courseRepository.save(course);
        log.info("Course {} deactivated", saved.getCourseCode());
        List<PrerequisiteItemResponse> prerequisites = prerequisiteService.getPrerequisiteItemResponses(saved.getId());
        return CourseResponse.fromModel(saved, prerequisites);
    }

    /**
     * Retrieves seat availability details for a course.
     */
    public CourseAvailabilityResponse getAvailability(String idOrCode) {
        Course course = findCourseByIdOrCode(idOrCode);
        int availableSeats = course.getAvailableSeats() != null ? course.getAvailableSeats() : course.getCapacity();
        boolean isAvailable = availableSeats > 0 && course.getStatus() == CourseStatus.ACTIVE;

        return new CourseAvailabilityResponse(
                course.getId(),
                course.getCourseCode(),
                course.getCapacity(),
                availableSeats,
                isAvailable
        );
    }

    /**
     * Lists courses with search and filtering capabilities.
     * Enforces active-only filtering for students unless an admin requests all/inactive.
     */
    public List<CourseResponse> listCourses(String search, String department, Integer semester,
                                            CourseType courseType, CourseStatus status,
                                            Boolean available, AuthenticatedUser user) {
        List<Course> courses;

        if (search != null && !search.isBlank()) {
            courses = courseRepository.searchCourses(search.trim());
        } else {
            courses = courseRepository.findAll();
        }

        boolean isAdmin = user != null && user.getRole().equalsIgnoreCase("ADMIN");

        return courses.stream()
                .filter(c -> {
                    // Role check: non-admins only see ACTIVE courses
                    if (!isAdmin) {
                        if (c.getStatus() != CourseStatus.ACTIVE) return false;
                    } else if (status != null) {
                        if (c.getStatus() != status) return false;
                    }

                    if (department != null && !department.isBlank()) {
                        if (!c.getDepartment().equalsIgnoreCase(department.trim())) return false;
                    }
                    if (semester != null) {
                        if (!c.getSemester().equals(semester)) return false;
                    }
                    if (courseType != null) {
                        if (c.getCourseType() != courseType) return false;
                    }
                    if (available != null) {
                        int seats = c.getAvailableSeats() != null ? c.getAvailableSeats() : 0;
                        if (available && seats <= 0) return false;
                        if (!available && seats > 0) return false;
                    }
                    return true;
                })
                .map(c -> CourseResponse.fromModel(c, prerequisiteService.getPrerequisiteItemResponses(c.getId())))
                .collect(Collectors.toList());
    }

    /**
     * Internal: Reserves a seat in a course.
     */
    public synchronized void reserveSeat(String idOrCode) {
        Course course = findCourseByIdOrCode(idOrCode);
        if (course.getStatus() != CourseStatus.ACTIVE) {
            throw new CourseInactiveException("Cannot reserve seat: Course " + course.getCourseCode() + " is inactive");
        }
        int available = course.getAvailableSeats() != null ? course.getAvailableSeats() : course.getCapacity();
        if (available <= 0) {
            throw new CourseFullException("Cannot reserve seat: Course " + course.getCourseCode() + " has no available seats");
        }
        course.setAvailableSeats(available - 1);
        course.setUpdatedAt(Instant.now());
        courseRepository.save(course);
        log.info("Seat reserved for course {}. Remaining seats: {}", course.getCourseCode(), course.getAvailableSeats());
    }

    /**
     * Internal: Releases a reserved seat in a course.
     */
    public synchronized void releaseSeat(String idOrCode) {
        Course course = findCourseByIdOrCode(idOrCode);
        int available = course.getAvailableSeats() != null ? course.getAvailableSeats() : course.getCapacity();
        if (available < course.getCapacity()) {
            course.setAvailableSeats(available + 1);
            course.setUpdatedAt(Instant.now());
            courseRepository.save(course);
            log.info("Seat released for course {}. Remaining seats: {}", course.getCourseCode(), course.getAvailableSeats());
        }
    }

    // ==========================================
    // Internal Helper Methods
    // ==========================================

    private Course findCourseByIdOrCode(String idOrCode) {
        Optional<Course> byCode = courseRepository.findByCourseCodeIgnoreCase(idOrCode.trim());
        if (byCode.isPresent()) {
            return byCode.get();
        }
        Optional<Course> byId = courseRepository.findById(idOrCode);
        if (byId.isPresent()) {
            return byId.get();
        }
        throw new CourseNotFoundException("Course not found with identifier: " + idOrCode);
    }
}

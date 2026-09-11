package com.studentcentral.course.service;

import com.studentcentral.course.dto.*;
import com.studentcentral.course.exception.*;
import com.studentcentral.course.model.Course;
import com.studentcentral.course.model.CourseStatus;
import com.studentcentral.course.model.CourseType;
import com.studentcentral.course.repository.CourseRepository;
import com.studentcentral.course.security.AuthenticatedUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CourseServiceTest {

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private PrerequisiteService prerequisiteService;

    @InjectMocks
    private CourseService courseService;

    private Course sampleCourse;
    private AuthenticatedUser studentUser;
    private AuthenticatedUser adminUser;

    @BeforeEach
    void setUp() {
        sampleCourse = new Course(
                "CS501", "Machine Learning", "ML concepts",
                "CSE", 5, 4, 40, CourseType.CORE, "Dr. Rao"
        );
        sampleCourse.setId("c-1");
        sampleCourse.setAvailableSeats(40);

        studentUser = new AuthenticatedUser("student-1", "student@example.com", "STUDENT");
        adminUser = new AuthenticatedUser("admin-1", "admin@studentcentral.com", "ADMIN");
    }

    @Test
    void shouldCreateCourseSuccessfullyWithAvailableSeatsEqualToCapacity() {
        CreateCourseRequest request = new CreateCourseRequest(
                "CS501", "Machine Learning", "ML concepts", "CSE", 5, 4, 40, CourseType.CORE, "Dr. Rao"
        );

        when(courseRepository.existsByCourseCodeIgnoreCase("CS501")).thenReturn(false);
        when(courseRepository.save(any(Course.class))).thenAnswer(i -> {
            Course c = i.getArgument(0);
            c.setId("c-1");
            return c;
        });

        CourseResponse response = courseService.createCourse(request);

        assertNotNull(response);
        assertEquals("CS501", response.getCourseCode());
        assertEquals(40, response.getCapacity());
        assertEquals(40, response.getAvailableSeats());
        assertEquals(CourseStatus.ACTIVE, response.getStatus());
    }

    @Test
    void shouldRejectDuplicateCourseCodeCreation() {
        CreateCourseRequest request = new CreateCourseRequest("CS501", "ML", null, "CSE", 5, 4, 40, CourseType.CORE, null);
        when(courseRepository.existsByCourseCodeIgnoreCase("CS501")).thenReturn(true);

        assertThrows(DuplicateCourseCodeException.class, () -> courseService.createCourse(request));
    }

    @Test
    void shouldRejectInvalidCapacityOnCreation() {
        CreateCourseRequest request = new CreateCourseRequest("CS501", "ML", null, "CSE", 5, 4, 0, CourseType.CORE, null);

        assertThrows(InvalidCapacityException.class, () -> courseService.createCourse(request));
    }

    @Test
    void shouldGetCourseByIdOrCode() {
        when(courseRepository.findByCourseCodeIgnoreCase("CS501")).thenReturn(Optional.of(sampleCourse));
        when(prerequisiteService.getPrerequisiteItemResponses("c-1")).thenReturn(List.of());

        CourseResponse response = courseService.getCourse("CS501");

        assertEquals("CS501", response.getCourseCode());
        assertEquals("Machine Learning", response.getCourseName());
    }

    @Test
    void shouldThrowWhenCourseNotFound() {
        when(courseRepository.findByCourseCodeIgnoreCase("NON_EXISTENT")).thenReturn(Optional.empty());
        when(courseRepository.findById("NON_EXISTENT")).thenReturn(Optional.empty());

        assertThrows(CourseNotFoundException.class, () -> courseService.getCourse("NON_EXISTENT"));
    }

    @Test
    void shouldUpdateCourseDetailsSuccessfully() {
        when(courseRepository.findByCourseCodeIgnoreCase("CS501")).thenReturn(Optional.of(sampleCourse));
        when(courseRepository.save(any(Course.class))).thenAnswer(i -> i.getArgument(0));
        when(prerequisiteService.getPrerequisiteItemResponses("c-1")).thenReturn(List.of());

        UpdateCourseRequest request = new UpdateCourseRequest();
        request.setCourseName("Advanced Machine Learning");

        CourseResponse response = courseService.updateCourse("CS501", request);

        assertEquals("Advanced Machine Learning", response.getCourseName());
    }

    @Test
    void shouldRecalculateAvailableSeatsWhenCapacityIncreases() {
        // capacity = 40, available = 10 -> occupied = 30
        sampleCourse.setCapacity(40);
        sampleCourse.setAvailableSeats(10);

        when(courseRepository.findByCourseCodeIgnoreCase("CS501")).thenReturn(Optional.of(sampleCourse));
        when(courseRepository.save(any(Course.class))).thenAnswer(i -> i.getArgument(0));
        when(prerequisiteService.getPrerequisiteItemResponses("c-1")).thenReturn(List.of());

        UpdateCourseRequest request = new UpdateCourseRequest();
        request.setCapacity(50); // new capacity = 50 -> new available = 50 - 30 = 20

        CourseResponse response = courseService.updateCourse("CS501", request);

        assertEquals(50, response.getCapacity());
        assertEquals(20, response.getAvailableSeats());
    }

    @Test
    void shouldRejectCapacityReductionBelowOccupiedSeats() {
        // capacity = 40, available = 5 -> occupied = 35
        sampleCourse.setCapacity(40);
        sampleCourse.setAvailableSeats(5);

        when(courseRepository.findByCourseCodeIgnoreCase("CS501")).thenReturn(Optional.of(sampleCourse));

        UpdateCourseRequest request = new UpdateCourseRequest();
        request.setCapacity(30); // 30 is less than 35 occupied

        assertThrows(InvalidCapacityException.class, () -> courseService.updateCourse("CS501", request));
    }

    @Test
    void shouldActivateAndDeactivateCourse() {
        when(courseRepository.findByCourseCodeIgnoreCase("CS501")).thenReturn(Optional.of(sampleCourse));
        when(courseRepository.save(any(Course.class))).thenAnswer(i -> i.getArgument(0));
        when(prerequisiteService.getPrerequisiteItemResponses("c-1")).thenReturn(List.of());

        CourseResponse deactivated = courseService.deactivateCourse("CS501");
        assertEquals(CourseStatus.INACTIVE, deactivated.getStatus());

        CourseResponse activated = courseService.activateCourse("CS501");
        assertEquals(CourseStatus.ACTIVE, activated.getStatus());
    }

    @Test
    void shouldReturnCorrectAvailabilityResponse() {
        sampleCourse.setCapacity(40);
        sampleCourse.setAvailableSeats(12);

        when(courseRepository.findByCourseCodeIgnoreCase("CS501")).thenReturn(Optional.of(sampleCourse));

        CourseAvailabilityResponse availability = courseService.getAvailability("CS501");

        assertEquals(40, availability.getCapacity());
        assertEquals(12, availability.getAvailableSeats());
        assertTrue(availability.getIsAvailable());
    }

    @Test
    void shouldIndicateFullCourseWhenAvailableSeatsIsZero() {
        sampleCourse.setCapacity(40);
        sampleCourse.setAvailableSeats(0);

        when(courseRepository.findByCourseCodeIgnoreCase("CS501")).thenReturn(Optional.of(sampleCourse));

        CourseAvailabilityResponse availability = courseService.getAvailability("CS501");

        assertEquals(0, availability.getAvailableSeats());
        assertFalse(availability.getIsAvailable());
    }

    @Test
    void shouldFilterActiveCoursesForStudentUser() {
        Course activeCourse = new Course("CS501", "ML", "desc", "CSE", 5, 4, 40, CourseType.CORE, "Dr. Rao");
        activeCourse.setId("c-1");
        activeCourse.setStatus(CourseStatus.ACTIVE);

        Course inactiveCourse = new Course("CS502", "Old Course", "desc", "CSE", 5, 4, 40, CourseType.CORE, "Dr. Smith");
        inactiveCourse.setId("c-2");
        inactiveCourse.setStatus(CourseStatus.INACTIVE);

        when(courseRepository.findAll()).thenReturn(List.of(activeCourse, inactiveCourse));
        when(prerequisiteService.getPrerequisiteItemResponses(any())).thenReturn(List.of());

        List<CourseResponse> studentView = courseService.listCourses(null, null, null, null, null, null, studentUser);

        assertEquals(1, studentView.size());
        assertEquals("CS501", studentView.get(0).getCourseCode());
    }

    @Test
    void shouldAllowAdminToViewInactiveCoursesWhenRequested() {
        Course activeCourse = new Course("CS501", "ML", "desc", "CSE", 5, 4, 40, CourseType.CORE, "Dr. Rao");
        activeCourse.setId("c-1");
        activeCourse.setStatus(CourseStatus.ACTIVE);

        Course inactiveCourse = new Course("CS502", "Old Course", "desc", "CSE", 5, 4, 40, CourseType.CORE, "Dr. Smith");
        inactiveCourse.setId("c-2");
        inactiveCourse.setStatus(CourseStatus.INACTIVE);

        when(courseRepository.findAll()).thenReturn(List.of(activeCourse, inactiveCourse));
        when(prerequisiteService.getPrerequisiteItemResponses(any())).thenReturn(List.of());

        List<CourseResponse> adminView = courseService.listCourses(null, null, null, null, CourseStatus.INACTIVE, null, adminUser);

        assertEquals(1, adminView.size());
        assertEquals("CS502", adminView.get(0).getCourseCode());
    }

    @Test
    void shouldPerformSeatReservationAndRelease() {
        sampleCourse.setCapacity(40);
        sampleCourse.setAvailableSeats(10);
        sampleCourse.setStatus(CourseStatus.ACTIVE);

        when(courseRepository.findByCourseCodeIgnoreCase("CS501")).thenReturn(Optional.of(sampleCourse));
        when(courseRepository.save(any(Course.class))).thenAnswer(i -> i.getArgument(0));

        courseService.reserveSeat("CS501");
        assertEquals(9, sampleCourse.getAvailableSeats());

        courseService.releaseSeat("CS501");
        assertEquals(10, sampleCourse.getAvailableSeats());
    }

    @Test
    void shouldRejectSeatReservationWhenCourseIsFull() {
        sampleCourse.setCapacity(40);
        sampleCourse.setAvailableSeats(0);
        sampleCourse.setStatus(CourseStatus.ACTIVE);

        when(courseRepository.findByCourseCodeIgnoreCase("CS501")).thenReturn(Optional.of(sampleCourse));

        assertThrows(CourseFullException.class, () -> courseService.reserveSeat("CS501"));
    }
}

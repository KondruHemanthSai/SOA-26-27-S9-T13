package com.studentcentral.course.service;

import com.studentcentral.course.dto.AddPrerequisiteRequest;
import com.studentcentral.course.dto.PrerequisiteResponse;
import com.studentcentral.course.exception.*;
import com.studentcentral.course.model.Course;
import com.studentcentral.course.model.CourseType;
import com.studentcentral.course.model.Prerequisite;
import com.studentcentral.course.repository.CourseRepository;
import com.studentcentral.course.repository.PrerequisiteRepository;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PrerequisiteServiceTest {

    @Mock
    private PrerequisiteRepository prerequisiteRepository;

    @Mock
    private CourseRepository courseRepository;

    @InjectMocks
    private PrerequisiteService prerequisiteService;

    private Course courseML;
    private Course courseDS;

    @BeforeEach
    void setUp() {
        courseML = new Course("CS501", "Machine Learning", "ML intro", "CSE", 5, 4, 40, CourseType.CORE, "Dr. Rao");
        courseML.setId("c-ml-1");

        courseDS = new Course("CS201", "Data Structures", "DS intro", "CSE", 2, 4, 60, CourseType.CORE, "Prof. Smith");
        courseDS.setId("c-ds-2");
    }

    @Test
    void shouldAddPrerequisiteSuccessfully() {
        when(courseRepository.findByCourseCodeIgnoreCase("CS501")).thenReturn(Optional.of(courseML));
        when(courseRepository.findByCourseCodeIgnoreCase("CS201")).thenReturn(Optional.of(courseDS));
        when(prerequisiteRepository.existsByCourseIdAndPrerequisiteCourseId("c-ml-1", "c-ds-2")).thenReturn(false);
        when(prerequisiteRepository.existsByCourseIdAndPrerequisiteCourseId("c-ds-2", "c-ml-1")).thenReturn(false);

        Prerequisite savedPrereq = new Prerequisite("c-ml-1", "c-ds-2");
        savedPrereq.setId("prereq-1");
        when(prerequisiteRepository.save(any(Prerequisite.class))).thenReturn(savedPrereq);
        when(prerequisiteRepository.findByCourseId("c-ml-1")).thenReturn(List.of(savedPrereq));
        when(courseRepository.findById("c-ds-2")).thenReturn(Optional.of(courseDS));

        AddPrerequisiteRequest request = new AddPrerequisiteRequest("CS201");
        PrerequisiteResponse response = prerequisiteService.addPrerequisite("CS501", request);

        assertNotNull(response);
        assertEquals("CS501", response.getCourseCode());
        assertEquals(1, response.getPrerequisites().size());
        assertEquals("CS201", response.getPrerequisites().get(0).getCourseCode());
    }

    @Test
    void shouldRejectSelfPrerequisite() {
        when(courseRepository.findByCourseCodeIgnoreCase("CS501")).thenReturn(Optional.of(courseML));

        AddPrerequisiteRequest request = new AddPrerequisiteRequest("CS501");

        assertThrows(SelfPrerequisiteException.class, () ->
                prerequisiteService.addPrerequisite("CS501", request));
    }

    @Test
    void shouldRejectDuplicatePrerequisite() {
        when(courseRepository.findByCourseCodeIgnoreCase("CS501")).thenReturn(Optional.of(courseML));
        when(courseRepository.findByCourseCodeIgnoreCase("CS201")).thenReturn(Optional.of(courseDS));
        when(prerequisiteRepository.existsByCourseIdAndPrerequisiteCourseId("c-ml-1", "c-ds-2")).thenReturn(true);

        AddPrerequisiteRequest request = new AddPrerequisiteRequest("CS201");

        assertThrows(DuplicatePrerequisiteException.class, () ->
                prerequisiteService.addPrerequisite("CS501", request));
    }

    @Test
    void shouldRejectCircularPrerequisite() {
        when(courseRepository.findByCourseCodeIgnoreCase("CS501")).thenReturn(Optional.of(courseML));
        when(courseRepository.findByCourseCodeIgnoreCase("CS201")).thenReturn(Optional.of(courseDS));
        when(prerequisiteRepository.existsByCourseIdAndPrerequisiteCourseId("c-ml-1", "c-ds-2")).thenReturn(false);
        // CS201 already requires CS501
        when(prerequisiteRepository.existsByCourseIdAndPrerequisiteCourseId("c-ds-2", "c-ml-1")).thenReturn(true);

        AddPrerequisiteRequest request = new AddPrerequisiteRequest("CS201");

        assertThrows(CircularPrerequisiteException.class, () ->
                prerequisiteService.addPrerequisite("CS501", request));
    }

    @Test
    void shouldThrowWhenPrerequisiteCourseDoesNotExist() {
        when(courseRepository.findByCourseCodeIgnoreCase("CS501")).thenReturn(Optional.of(courseML));
        when(courseRepository.findByCourseCodeIgnoreCase("NON_EXISTENT")).thenReturn(Optional.empty());
        when(courseRepository.findById("NON_EXISTENT")).thenReturn(Optional.empty());

        AddPrerequisiteRequest request = new AddPrerequisiteRequest("NON_EXISTENT");

        assertThrows(CourseNotFoundException.class, () ->
                prerequisiteService.addPrerequisite("CS501", request));
    }

    @Test
    void shouldGetPrerequisitesForCourse() {
        when(courseRepository.findByCourseCodeIgnoreCase("CS501")).thenReturn(Optional.of(courseML));
        Prerequisite p = new Prerequisite("c-ml-1", "c-ds-2");
        p.setId("prereq-1");
        when(prerequisiteRepository.findByCourseId("c-ml-1")).thenReturn(List.of(p));
        when(courseRepository.findById("c-ds-2")).thenReturn(Optional.of(courseDS));

        PrerequisiteResponse response = prerequisiteService.getPrerequisites("CS501");

        assertEquals(1, response.getPrerequisites().size());
        assertEquals("CS201", response.getPrerequisites().get(0).getCourseCode());
    }

    @Test
    void shouldDeletePrerequisiteSuccessfully() {
        when(courseRepository.findByCourseCodeIgnoreCase("CS501")).thenReturn(Optional.of(courseML));
        Prerequisite p = new Prerequisite("c-ml-1", "c-ds-2");
        p.setId("prereq-1");
        when(prerequisiteRepository.findByIdAndCourseId("prereq-1", "c-ml-1")).thenReturn(Optional.of(p));

        prerequisiteService.deletePrerequisite("CS501", "prereq-1");

        verify(prerequisiteRepository, times(1)).delete(p);
    }
}

package com.studentcentral.course.service;

import com.studentcentral.course.dto.AddPrerequisiteRequest;
import com.studentcentral.course.dto.PrerequisiteItemResponse;
import com.studentcentral.course.dto.PrerequisiteResponse;
import com.studentcentral.course.exception.*;
import com.studentcentral.course.model.Course;
import com.studentcentral.course.model.Prerequisite;
import com.studentcentral.course.repository.CourseRepository;
import com.studentcentral.course.repository.PrerequisiteRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class PrerequisiteService {

    private static final Logger log = LoggerFactory.getLogger(PrerequisiteService.class);

    private final PrerequisiteRepository prerequisiteRepository;
    private final CourseRepository courseRepository;

    public PrerequisiteService(PrerequisiteRepository prerequisiteRepository, CourseRepository courseRepository) {
        this.prerequisiteRepository = prerequisiteRepository;
        this.courseRepository = courseRepository;
    }

    /**
     * Adds a prerequisite requirement to a course with validation against self/duplicate/circular references.
     */
    public PrerequisiteResponse addPrerequisite(String courseIdOrCode, AddPrerequisiteRequest request) {
        Course targetCourse = findCourseByIdOrCode(courseIdOrCode);
        Course prereqCourse = findCourseByIdOrCode(request.getPrerequisiteCourseId());

        if (targetCourse.getId().equals(prereqCourse.getId())) {
            log.warn("Self-prerequisite attempt for course {}", targetCourse.getCourseCode());
            throw new SelfPrerequisiteException("A course cannot be a prerequisite for itself: " + targetCourse.getCourseCode());
        }

        if (prerequisiteRepository.existsByCourseIdAndPrerequisiteCourseId(targetCourse.getId(), prereqCourse.getId())) {
            log.warn("Duplicate prerequisite attempt: {} already requires {}", targetCourse.getCourseCode(), prereqCourse.getCourseCode());
            throw new DuplicatePrerequisiteException("Course " + targetCourse.getCourseCode() + " already has prerequisite " + prereqCourse.getCourseCode());
        }

        // Circular check: If B already requires A, A cannot require B
        if (prerequisiteRepository.existsByCourseIdAndPrerequisiteCourseId(prereqCourse.getId(), targetCourse.getId())) {
            log.warn("Circular prerequisite detected between {} and {}", targetCourse.getCourseCode(), prereqCourse.getCourseCode());
            throw new CircularPrerequisiteException("Circular prerequisite detected: Course " + prereqCourse.getCourseCode() + " already requires " + targetCourse.getCourseCode());
        }

        Prerequisite prerequisite = new Prerequisite(targetCourse.getId(), prereqCourse.getId());
        prerequisiteRepository.save(prerequisite);
        log.info("Added prerequisite {} -> {} (Prerequisite ID: {})", targetCourse.getCourseCode(), prereqCourse.getCourseCode(), prerequisite.getId());

        List<PrerequisiteItemResponse> items = getPrerequisiteItemResponses(targetCourse.getId());
        return new PrerequisiteResponse(targetCourse.getId(), targetCourse.getCourseCode(), items);
    }

    /**
     * Retrieves all prerequisites for a given course.
     */
    public PrerequisiteResponse getPrerequisites(String courseIdOrCode) {
        Course course = findCourseByIdOrCode(courseIdOrCode);
        List<PrerequisiteItemResponse> items = getPrerequisiteItemResponses(course.getId());
        return new PrerequisiteResponse(course.getId(), course.getCourseCode(), items);
    }

    /**
     * Helper to load prerequisite item details.
     */
    public List<PrerequisiteItemResponse> getPrerequisiteItemResponses(String courseId) {
        List<Prerequisite> prerequisites = prerequisiteRepository.findByCourseId(courseId);
        List<PrerequisiteItemResponse> items = new ArrayList<>();

        for (Prerequisite p : prerequisites) {
            Optional<Course> prereqCourseOpt = courseRepository.findById(p.getPrerequisiteCourseId());
            if (prereqCourseOpt.isPresent()) {
                Course pc = prereqCourseOpt.get();
                items.add(new PrerequisiteItemResponse(
                        p.getId(),
                        pc.getId(),
                        pc.getCourseCode(),
                        pc.getCourseName(),
                        pc.getCredits()
                ));
            }
        }
        return items;
    }

    /**
     * Removes a prerequisite relationship.
     */
    public void deletePrerequisite(String courseIdOrCode, String prerequisiteId) {
        Course course = findCourseByIdOrCode(courseIdOrCode);

        Optional<Prerequisite> prereqOpt = prerequisiteRepository.findByIdAndCourseId(prerequisiteId, course.getId());
        if (prereqOpt.isPresent()) {
            prerequisiteRepository.delete(prereqOpt.get());
            log.info("Deleted prerequisite ID {} for course {}", prerequisiteId, course.getCourseCode());
            return;
        }

        // Also allow deleting by prerequisite course ID or code
        Optional<Course> targetPrereqCourse = findCourseOptional(prerequisiteId);
        if (targetPrereqCourse.isPresent()) {
            List<Prerequisite> matches = prerequisiteRepository.findByCourseId(course.getId());
            for (Prerequisite p : matches) {
                if (p.getPrerequisiteCourseId().equals(targetPrereqCourse.get().getId())) {
                    prerequisiteRepository.delete(p);
                    log.info("Deleted prerequisite relationship between {} and {}", course.getCourseCode(), targetPrereqCourse.get().getCourseCode());
                    return;
                }
            }
        }

        throw new PrerequisiteNotFoundException("Prerequisite not found: " + prerequisiteId + " for course: " + course.getCourseCode());
    }

    private Course findCourseByIdOrCode(String idOrCode) {
        return findCourseOptional(idOrCode)
                .orElseThrow(() -> new CourseNotFoundException("Course not found: " + idOrCode));
    }

    private Optional<Course> findCourseOptional(String idOrCode) {
        Optional<Course> byCode = courseRepository.findByCourseCodeIgnoreCase(idOrCode.trim());
        if (byCode.isPresent()) {
            return byCode;
        }
        return courseRepository.findById(idOrCode);
    }
}

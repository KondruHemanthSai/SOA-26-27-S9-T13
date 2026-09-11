package com.studentcentral.course.repository;

import com.studentcentral.course.model.Course;
import com.studentcentral.course.model.CourseStatus;
import com.studentcentral.course.model.CourseType;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRepository extends MongoRepository<Course, String> {

    Optional<Course> findByCourseCodeIgnoreCase(String courseCode);

    boolean existsByCourseCodeIgnoreCase(String courseCode);

    List<Course> findByStatus(CourseStatus status);

    List<Course> findByDepartmentIgnoreCase(String department);

    List<Course> findByDepartmentIgnoreCaseAndStatus(String department, CourseStatus status);

    List<Course> findBySemester(Integer semester);

    List<Course> findBySemesterAndStatus(Integer semester, CourseStatus status);

    List<Course> findByCourseType(CourseType courseType);

    List<Course> findByCourseTypeAndStatus(CourseType courseType, CourseStatus status);

    List<Course> findByAvailableSeatsGreaterThan(int seats);

    List<Course> findByAvailableSeatsGreaterThanAndStatus(int seats, CourseStatus status);

    @Query("{ '$or': [ " +
            "{ 'courseCode': { '$regex': ?0, '$options': 'i' } }, " +
            "{ 'courseName': { '$regex': ?0, '$options': 'i' } }, " +
            "{ 'description': { '$regex': ?0, '$options': 'i' } } " +
            "] }")
    List<Course> searchCourses(String keyword);
}

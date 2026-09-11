package com.studentcentral.registration.repository;

import com.studentcentral.registration.model.Registration;
import com.studentcentral.registration.model.RegistrationStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RegistrationRepository extends MongoRepository<Registration, String> {

    Optional<Registration> findByRegistrationId(String registrationId);

    List<Registration> findByStudentId(String studentId);

    List<Registration> findByStudentIdAndStatus(String studentId, RegistrationStatus status);

    List<Registration> findByStudentIdAndSemesterAndAcademicYearAndStatus(
            String studentId, Integer semester, String academicYear, RegistrationStatus status);

    List<Registration> findByStudentIdAndSemesterAndStatus(String studentId, Integer semester, RegistrationStatus status);

    Optional<Registration> findByStudentIdAndCourseIdAndStatus(String studentId, String courseId, RegistrationStatus status);

    boolean existsByStudentIdAndCourseIdAndStatus(String studentId, String courseId, RegistrationStatus status);

    boolean existsByStudentIdAndCourseCodeIgnoreCaseAndStatus(String studentId, String courseCode, RegistrationStatus status);

    List<Registration> findByCourseId(String courseId);

    List<Registration> findByCourseIdAndStatus(String courseId, RegistrationStatus status);

    List<Registration> findBySemester(Integer semester);

    List<Registration> findByStatus(RegistrationStatus status);

    Optional<Registration> findTopByOrderByIdDesc();
}

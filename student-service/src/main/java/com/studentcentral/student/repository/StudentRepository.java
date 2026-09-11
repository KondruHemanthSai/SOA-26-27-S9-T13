package com.studentcentral.student.repository;

import com.studentcentral.student.model.AdmissionStatus;
import com.studentcentral.student.model.Student;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends MongoRepository<Student, String> {

    Optional<Student> findByUserId(String userId);

    Optional<Student> findByStudentId(String studentId);

    boolean existsByUserId(String userId);

    boolean existsByStudentId(String studentId);

    List<Student> findByDepartmentIgnoreCase(String department);

    List<Student> findByAdmissionStatus(AdmissionStatus status);

    List<Student> findByDepartmentIgnoreCaseAndAdmissionStatus(String department, AdmissionStatus status);

    long countByEnrollmentYear(Integer year);
}

package com.studentcentral.admission.repository;

import com.studentcentral.admission.model.AdmissionApplication;
import com.studentcentral.admission.model.ApplicationStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AdmissionApplicationRepository extends MongoRepository<AdmissionApplication, String> {

    Optional<AdmissionApplication> findByApplicationId(String applicationId);

    Optional<AdmissionApplication> findByUserId(String userId);

    Optional<AdmissionApplication> findByStudentId(String studentId);

    boolean existsByUserId(String userId);

    boolean existsByApplicationId(String applicationId);

    List<AdmissionApplication> findByStatus(ApplicationStatus status);

    List<AdmissionApplication> findByDepartmentIgnoreCase(String department);

    List<AdmissionApplication> findByDepartmentIgnoreCaseAndStatus(String department, ApplicationStatus status);

    long count();
}

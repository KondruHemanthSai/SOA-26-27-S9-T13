package com.studentcentral.course.repository;

import com.studentcentral.course.model.Prerequisite;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PrerequisiteRepository extends MongoRepository<Prerequisite, String> {

    List<Prerequisite> findByCourseId(String courseId);

    List<Prerequisite> findByPrerequisiteCourseId(String prerequisiteCourseId);

    boolean existsByCourseIdAndPrerequisiteCourseId(String courseId, String prerequisiteCourseId);

    Optional<Prerequisite> findByIdAndCourseId(String id, String courseId);

    void deleteByCourseId(String courseId);
}

package com.studentcentral.admission.repository;

import com.studentcentral.admission.model.Document;
import com.studentcentral.admission.model.DocumentType;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentRepository extends MongoRepository<Document, String> {

    List<Document> findByApplicationId(String applicationId);

    List<Document> findByStudentId(String studentId);

    Optional<Document> findByIdAndApplicationId(String id, String applicationId);

    boolean existsByApplicationIdAndType(String applicationId, DocumentType type);

    long countByApplicationId(String applicationId);

    void deleteByApplicationId(String applicationId);
}

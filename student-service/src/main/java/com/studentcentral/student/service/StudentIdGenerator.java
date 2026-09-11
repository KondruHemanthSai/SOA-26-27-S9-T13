package com.studentcentral.student.service;

import com.studentcentral.student.repository.StudentRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class StudentIdGenerator {

    private final StudentRepository studentRepository;

    public StudentIdGenerator(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    /**
     * Generates a unique institutional Student ID.
     * Format: SC + [Year] + [4-digit sequential index] -> e.g. SC20260001
     */
    public synchronized String generateStudentId(Integer enrollmentYear) {
        int year = (enrollmentYear != null && enrollmentYear >= 2000) ? enrollmentYear : LocalDate.now().getYear();
        long existingCount = studentRepository.countByEnrollmentYear(year);

        long nextSequence = existingCount + 1;
        String candidateId = String.format("SC%d%04d", year, nextSequence);

        // Ensure uniqueness even if concurrent or prior deletions occurred
        while (studentRepository.existsByStudentId(candidateId)) {
            nextSequence++;
            candidateId = String.format("SC%d%04d", year, nextSequence);
        }

        return candidateId;
    }
}

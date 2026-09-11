package com.studentcentral.admission.service;

import com.studentcentral.admission.repository.AdmissionApplicationRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class ApplicationIdGenerator {

    private final AdmissionApplicationRepository applicationRepository;

    public ApplicationIdGenerator(AdmissionApplicationRepository applicationRepository) {
        this.applicationRepository = applicationRepository;
    }

    /**
     * Generates a unique, collision-resistant application ID in the format SC-ADM-<YEAR>-<5-digit-seq>.
     * e.g., SC-ADM-2026-00001
     */
    public synchronized String generateApplicationId() {
        int year = LocalDate.now().getYear();
        long count = applicationRepository.count();
        long sequence = count + 1;

        String generatedId;
        do {
            generatedId = String.format("SC-ADM-%d-%05d", year, sequence);
            sequence++;
        } while (applicationRepository.existsByApplicationId(generatedId));

        return generatedId;
    }
}

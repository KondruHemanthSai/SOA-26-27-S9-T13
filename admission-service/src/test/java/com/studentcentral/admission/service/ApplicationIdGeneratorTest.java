package com.studentcentral.admission.service;

import com.studentcentral.admission.repository.AdmissionApplicationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApplicationIdGeneratorTest {

    @Mock
    private AdmissionApplicationRepository applicationRepository;

    @InjectMocks
    private ApplicationIdGenerator generator;

    @Test
    void shouldGenerateFirstApplicationIdWhenCollectionIsEmpty() {
        when(applicationRepository.count()).thenReturn(0L);
        when(applicationRepository.existsByApplicationId(anyString())).thenReturn(false);

        String appId = generator.generateApplicationId();
        int currentYear = LocalDate.now().getYear();

        assertEquals(String.format("SC-ADM-%d-00001", currentYear), appId);
    }

    @Test
    void shouldIncrementSequenceForSubsequentApplications() {
        when(applicationRepository.count()).thenReturn(14L);
        when(applicationRepository.existsByApplicationId(anyString())).thenReturn(false);

        String appId = generator.generateApplicationId();
        int currentYear = LocalDate.now().getYear();

        assertEquals(String.format("SC-ADM-%d-00015", currentYear), appId);
    }

    @Test
    void shouldHandleCollisionsByIncrementingUntilUnique() {
        when(applicationRepository.count()).thenReturn(0L);
        int currentYear = LocalDate.now().getYear();
        String collisionId = String.format("SC-ADM-%d-00001", currentYear);
        String uniqueId = String.format("SC-ADM-%d-00002", currentYear);

        when(applicationRepository.existsByApplicationId(collisionId)).thenReturn(true);
        when(applicationRepository.existsByApplicationId(uniqueId)).thenReturn(false);

        String appId = generator.generateApplicationId();

        assertEquals(uniqueId, appId);
    }
}

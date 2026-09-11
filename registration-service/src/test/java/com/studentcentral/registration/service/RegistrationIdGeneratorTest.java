package com.studentcentral.registration.service;

import com.studentcentral.registration.model.Registration;
import com.studentcentral.registration.repository.RegistrationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegistrationIdGeneratorTest {

    @Mock
    private RegistrationRepository registrationRepository;

    private RegistrationIdGenerator generator;

    @BeforeEach
    void setUp() {
        generator = new RegistrationIdGenerator(registrationRepository);
    }

    @Test
    void shouldGenerateFirstRegistrationIdWhenNoPriorExists() {
        when(registrationRepository.findTopByOrderByIdDesc()).thenReturn(Optional.empty());
        when(registrationRepository.findByRegistrationId(anyString())).thenReturn(Optional.empty());

        String regId = generator.generateRegistrationId(2026);

        assertEquals("SC-REG-2026-00001", regId);
    }

    @Test
    void shouldIncrementRegistrationIdFromPriorRecord() {
        Registration prior = new Registration();
        prior.setRegistrationId("SC-REG-2026-00042");

        when(registrationRepository.findTopByOrderByIdDesc()).thenReturn(Optional.of(prior));
        when(registrationRepository.findByRegistrationId(anyString())).thenReturn(Optional.empty());

        String regId = generator.generateRegistrationId(2026);

        assertEquals("SC-REG-2026-00043", regId);
    }

    @Test
    void shouldHandleCollisionAndGenerateNextAvailable() {
        when(registrationRepository.findTopByOrderByIdDesc()).thenReturn(Optional.empty());

        Registration existing = new Registration();
        existing.setRegistrationId("SC-REG-2026-00001");

        when(registrationRepository.findByRegistrationId("SC-REG-2026-00001")).thenReturn(Optional.of(existing));
        when(registrationRepository.findByRegistrationId("SC-REG-2026-00002")).thenReturn(Optional.empty());

        String regId = generator.generateRegistrationId(2026);

        assertEquals("SC-REG-2026-00002", regId);
    }
}

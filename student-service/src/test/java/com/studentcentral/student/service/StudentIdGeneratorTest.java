package com.studentcentral.student.service;

import com.studentcentral.student.repository.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudentIdGeneratorTest {

    @Mock
    private StudentRepository studentRepository;

    private StudentIdGenerator studentIdGenerator;

    @BeforeEach
    void setUp() {
        studentIdGenerator = new StudentIdGenerator(studentRepository);
    }

    @Test
    void shouldGenerateFirstStudentIdForYear() {
        when(studentRepository.countByEnrollmentYear(2026)).thenReturn(0L);
        when(studentRepository.existsByStudentId("SC20260001")).thenReturn(false);

        String studentId = studentIdGenerator.generateStudentId(2026);

        assertEquals("SC20260001", studentId);
    }

    @Test
    void shouldGenerateNextSequentialStudentId() {
        when(studentRepository.countByEnrollmentYear(2026)).thenReturn(15L);
        when(studentRepository.existsByStudentId("SC20260016")).thenReturn(false);

        String studentId = studentIdGenerator.generateStudentId(2026);

        assertEquals("SC20260016", studentId);
    }

    @Test
    void shouldHandleCollisionAndIncrementUntilUnique() {
        when(studentRepository.countByEnrollmentYear(2026)).thenReturn(0L);
        when(studentRepository.existsByStudentId("SC20260001")).thenReturn(true);
        when(studentRepository.existsByStudentId("SC20260002")).thenReturn(false);

        String studentId = studentIdGenerator.generateStudentId(2026);

        assertEquals("SC20260002", studentId);
    }
}

package com.studentcentral.student.service;

import com.studentcentral.student.dto.*;
import com.studentcentral.student.exception.ForbiddenAccessException;
import com.studentcentral.student.exception.StudentAlreadyExistsException;
import com.studentcentral.student.exception.StudentNotFoundException;
import com.studentcentral.student.model.AcademicRecord;
import com.studentcentral.student.model.AdmissionStatus;
import com.studentcentral.student.model.Student;
import com.studentcentral.student.repository.StudentRepository;
import com.studentcentral.student.security.AuthenticatedUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudentServiceTest {

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private StudentIdGenerator studentIdGenerator;

    private StudentService studentService;

    @BeforeEach
    void setUp() {
        studentService = new StudentService(studentRepository, studentIdGenerator);
    }

    @Test
    void shouldCreateStudentProfileSuccessfully() {
        AuthenticatedUser user = new AuthenticatedUser("user-101", "student@example.com", "STUDENT");
        CreateStudentRequest request = new CreateStudentRequest(
                "John", "Doe", "+1234567890",
                LocalDate.of(2002, 5, 15),
                "123 Main St", "B.Tech Computer Science", "Computer Science", 2026, 1
        );
        request.setAcademicRecord(new AcademicRecordRequest("High School X", "CBSE", 92.5, 9.2, 2022));

        when(studentRepository.existsByUserId("user-101")).thenReturn(false);
        when(studentIdGenerator.generateStudentId(2026)).thenReturn("SC20260001");

        Student savedStudent = new Student();
        savedStudent.setId("mongo-1");
        savedStudent.setUserId("user-101");
        savedStudent.setStudentId("SC20260001");
        savedStudent.setEmail("student@example.com");
        savedStudent.setFirstName("John");
        savedStudent.setLastName("Doe");
        savedStudent.setPhone("+1234567890");
        savedStudent.setDateOfBirth(LocalDate.of(2002, 5, 15));
        savedStudent.setAddress("123 Main St");
        savedStudent.setProgram("B.Tech Computer Science");
        savedStudent.setDepartment("Computer Science");
        savedStudent.setEnrollmentYear(2026);
        savedStudent.setSemester(1);
        savedStudent.setAdmissionStatus(AdmissionStatus.PENDING);
        savedStudent.setProfileCompleted(true);
        savedStudent.setAcademicRecord(new AcademicRecord("High School X", "CBSE", 92.5, 9.2, 2022));
        savedStudent.setCreatedAt(Instant.now());
        savedStudent.setUpdatedAt(Instant.now());

        when(studentRepository.save(any(Student.class))).thenReturn(savedStudent);

        StudentResponse response = studentService.createProfile(user, request);

        assertNotNull(response);
        assertEquals("mongo-1", response.getId());
        assertEquals("user-101", response.getUserId());
        assertEquals("SC20260001", response.getStudentId());
        assertEquals("John", response.getFirstName());
        assertEquals("Doe", response.getLastName());
        assertEquals("student@example.com", response.getEmail());
        assertTrue(response.isProfileCompleted());
        assertEquals(AdmissionStatus.PENDING, response.getAdmissionStatus());
        assertNotNull(response.getAcademicRecord());
        assertEquals(92.5, response.getAcademicRecord().getPercentage());

        verify(studentRepository).save(any(Student.class));
    }

    @Test
    void shouldRejectProfileCreationIfProfileAlreadyExists() {
        AuthenticatedUser user = new AuthenticatedUser("user-101", "student@example.com", "STUDENT");
        CreateStudentRequest request = new CreateStudentRequest(
                "John", "Doe", "+1234567890",
                LocalDate.of(2002, 5, 15),
                "123 Main St", "B.Tech Computer Science", "Computer Science", 2026, 1
        );

        when(studentRepository.existsByUserId("user-101")).thenReturn(true);

        assertThrows(StudentAlreadyExistsException.class, () -> studentService.createProfile(user, request));
        verify(studentRepository, never()).save(any());
    }

    @Test
    void shouldGetOwnProfileSuccessfully() {
        Student student = new Student();
        student.setId("mongo-1");
        student.setUserId("user-101");
        student.setStudentId("SC20260001");
        student.setFirstName("John");
        student.setLastName("Doe");

        when(studentRepository.findByUserId("user-101")).thenReturn(Optional.of(student));

        StudentResponse response = studentService.getOwnProfile("user-101");

        assertNotNull(response);
        assertEquals("SC20260001", response.getStudentId());
        assertEquals("John", response.getFirstName());
    }

    @Test
    void shouldThrowIfOwnProfileNotFound() {
        when(studentRepository.findByUserId("user-404")).thenReturn(Optional.empty());

        assertThrows(StudentNotFoundException.class, () -> studentService.getOwnProfile("user-404"));
    }

    @Test
    void shouldUpdateOwnProfileSuccessfully() {
        Student student = new Student();
        student.setId("mongo-1");
        student.setUserId("user-101");
        student.setStudentId("SC20260001");
        student.setFirstName("OldName");
        student.setLastName("Doe");
        student.setPhone("+1234567890");
        student.setDateOfBirth(LocalDate.of(2002, 5, 15));
        student.setAddress("Old Address");
        student.setProgram("B.Tech");
        student.setDepartment("CSE");
        student.setEnrollmentYear(2026);

        when(studentRepository.findByUserId("user-101")).thenReturn(Optional.of(student));
        when(studentRepository.save(any(Student.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UpdateStudentRequest updateRequest = new UpdateStudentRequest();
        updateRequest.setFirstName("NewFirstName");
        updateRequest.setAddress("456 New Blvd");

        StudentResponse response = studentService.updateOwnProfile("user-101", updateRequest);

        assertNotNull(response);
        assertEquals("NewFirstName", response.getFirstName());
        assertEquals("456 New Blvd", response.getAddress());
        assertEquals("Doe", response.getLastName());
        assertTrue(response.isProfileCompleted());
    }

    @Test
    void shouldUpdateAcademicRecordSuccessfully() {
        Student student = new Student();
        student.setId("mongo-1");
        student.setUserId("user-101");

        when(studentRepository.findByUserId("user-101")).thenReturn(Optional.of(student));
        when(studentRepository.save(any(Student.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AcademicRecordRequest academicRequest = new AcademicRecordRequest("State College", "ICSE", 88.0, 8.8, 2021);

        AcademicRecordResponse response = studentService.updateOwnAcademicRecord("user-101", academicRequest);

        assertNotNull(response);
        assertEquals("State College", response.getPreviousInstitution());
        assertEquals("ICSE", response.getBoard());
        assertEquals(88.0, response.getPercentage());
        assertEquals(8.8, response.getCgpa());
        assertEquals(2021, response.getGraduationYear());
    }

    @Test
    void shouldAllowAdminToLookupAnyStudent() {
        AuthenticatedUser adminUser = new AuthenticatedUser("admin-1", "admin@studentcentral.com", "ADMIN");
        Student student = new Student();
        student.setId("mongo-99");
        student.setUserId("user-101");
        student.setStudentId("SC20260099");

        when(studentRepository.findById("mongo-99")).thenReturn(Optional.of(student));

        StudentResponse response = studentService.getStudentById(adminUser, "mongo-99");

        assertNotNull(response);
        assertEquals("SC20260099", response.getStudentId());
    }

    @Test
    void shouldForbidStudentFromViewingAnotherStudentsProfile() {
        AuthenticatedUser studentUser = new AuthenticatedUser("user-101", "student1@example.com", "STUDENT");
        Student otherStudent = new Student();
        otherStudent.setId("mongo-99");
        otherStudent.setUserId("user-999"); // different student user ID
        otherStudent.setStudentId("SC20260099");

        when(studentRepository.findById("mongo-99")).thenReturn(Optional.of(otherStudent));

        assertThrows(ForbiddenAccessException.class, () -> studentService.getStudentById(studentUser, "mongo-99"));
    }

    @Test
    void shouldListStudentsWithFiltersForAdmin() {
        Student s1 = new Student();
        s1.setId("1");
        s1.setDepartment("CSE");
        s1.setAdmissionStatus(AdmissionStatus.APPROVED);

        when(studentRepository.findByDepartmentIgnoreCaseAndAdmissionStatus("CSE", AdmissionStatus.APPROVED))
                .thenReturn(List.of(s1));

        List<StudentResponse> list = studentService.listStudents("CSE", AdmissionStatus.APPROVED);

        assertNotNull(list);
        assertEquals(1, list.size());
        assertEquals("1", list.get(0).getId());
    }
}

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class StudentService {

    private static final Logger log = LoggerFactory.getLogger(StudentService.class);

    private final StudentRepository studentRepository;
    private final StudentIdGenerator studentIdGenerator;

    public StudentService(StudentRepository studentRepository, StudentIdGenerator studentIdGenerator) {
        this.studentRepository = studentRepository;
        this.studentIdGenerator = studentIdGenerator;
    }

    /**
     * Create a new student profile for the authenticated user.
     */
    public StudentResponse createProfile(AuthenticatedUser currentUser, CreateStudentRequest request) {
        String userId = currentUser.getUserId();

        if (studentRepository.existsByUserId(userId)) {
            log.warn("Profile creation failed: user '{}' already has a student profile", userId);
            throw new StudentAlreadyExistsException("A student profile already exists for this user account");
        }

        String studentId = studentIdGenerator.generateStudentId(request.getEnrollmentYear());

        Student student = new Student();
        student.setUserId(userId);
        student.setStudentId(studentId);
        student.setEmail(currentUser.getEmail());
        student.setFirstName(request.getFirstName().trim());
        student.setLastName(request.getLastName().trim());
        student.setPhone(request.getPhone() != null ? request.getPhone().trim() : null);
        student.setDateOfBirth(request.getDateOfBirth());
        student.setAddress(request.getAddress() != null ? request.getAddress().trim() : null);
        student.setProgram(request.getProgram() != null ? request.getProgram().trim() : null);
        student.setDepartment(request.getDepartment() != null ? request.getDepartment().trim() : null);
        student.setEnrollmentYear(request.getEnrollmentYear());
        student.setSemester(request.getSemester() != null ? request.getSemester() : 1);
        student.setAdmissionStatus(AdmissionStatus.PENDING);

        if (request.getAcademicRecord() != null) {
            AcademicRecord record = new AcademicRecord(
                    request.getAcademicRecord().getPreviousInstitution(),
                    request.getAcademicRecord().getBoard(),
                    request.getAcademicRecord().getPercentage(),
                    request.getAcademicRecord().getCgpa(),
                    request.getAcademicRecord().getGraduationYear()
            );
            student.setAcademicRecord(record);
        }

        student.setProfileCompleted(calculateProfileCompleted(student));
        student.setCreatedAt(Instant.now());
        student.setUpdatedAt(Instant.now());

        Student savedStudent = studentRepository.save(student);
        log.info("Student profile created successfully: studentId={}, userId={}", savedStudent.getStudentId(), userId);

        return StudentResponse.fromStudent(savedStudent);
    }

    /**
     * Retrieve the profile of the authenticated student.
     */
    public StudentResponse getOwnProfile(String userId) {
        Student student = studentRepository.findByUserId(userId)
                .orElseThrow(() -> new StudentNotFoundException("Student profile not found for user: " + userId));
        return StudentResponse.fromStudent(student);
    }

    /**
     * Update the profile of the authenticated student.
     */
    public StudentResponse updateOwnProfile(String userId, UpdateStudentRequest request) {
        Student student = studentRepository.findByUserId(userId)
                .orElseThrow(() -> new StudentNotFoundException("Student profile not found for user: " + userId));

        if (request.getFirstName() != null && !request.getFirstName().isBlank()) {
            student.setFirstName(request.getFirstName().trim());
        }
        if (request.getLastName() != null && !request.getLastName().isBlank()) {
            student.setLastName(request.getLastName().trim());
        }
        if (request.getPhone() != null) {
            student.setPhone(request.getPhone().trim());
        }
        if (request.getDateOfBirth() != null) {
            student.setDateOfBirth(request.getDateOfBirth());
        }
        if (request.getAddress() != null) {
            student.setAddress(request.getAddress().trim());
        }
        if (request.getProgram() != null) {
            student.setProgram(request.getProgram().trim());
        }
        if (request.getDepartment() != null) {
            student.setDepartment(request.getDepartment().trim());
        }
        if (request.getEnrollmentYear() != null) {
            student.setEnrollmentYear(request.getEnrollmentYear());
        }
        if (request.getSemester() != null) {
            student.setSemester(request.getSemester());
        }

        student.setProfileCompleted(calculateProfileCompleted(student));
        student.setUpdatedAt(Instant.now());

        Student updatedStudent = studentRepository.save(student);
        log.info("Student profile updated successfully for userId: {}", userId);

        return StudentResponse.fromStudent(updatedStudent);
    }

    /**
     * Retrieve the academic record of the authenticated student.
     */
    public AcademicRecordResponse getOwnAcademicRecord(String userId) {
        Student student = studentRepository.findByUserId(userId)
                .orElseThrow(() -> new StudentNotFoundException("Student profile not found for user: " + userId));

        return AcademicRecordResponse.fromModel(student.getAcademicRecord());
    }

    /**
     * Update the academic record of the authenticated student.
     */
    public AcademicRecordResponse updateOwnAcademicRecord(String userId, AcademicRecordRequest request) {
        Student student = studentRepository.findByUserId(userId)
                .orElseThrow(() -> new StudentNotFoundException("Student profile not found for user: " + userId));

        AcademicRecord record = new AcademicRecord(
                request.getPreviousInstitution(),
                request.getBoard(),
                request.getPercentage(),
                request.getCgpa(),
                request.getGraduationYear()
        );

        student.setAcademicRecord(record);
        student.setUpdatedAt(Instant.now());

        studentRepository.save(student);
        log.info("Academic record updated for userId: {}", userId);

        return AcademicRecordResponse.fromModel(record);
    }

    /**
     * Get a student by internal ID, studentId, or userId.
     * Enforces that STUDENT users may only view their own profile.
     */
    public StudentResponse getStudentById(AuthenticatedUser currentUser, String identifier) {
        Student student = studentRepository.findById(identifier)
                .or(() -> studentRepository.findByStudentId(identifier))
                .or(() -> studentRepository.findByUserId(identifier))
                .orElseThrow(() -> new StudentNotFoundException("Student not found with identifier: " + identifier));

        if (currentUser.isStudent() && !student.getUserId().equals(currentUser.getUserId())) {
            log.warn("Unauthorized access: student '{}' attempted to view profile of student '{}'",
                    currentUser.getUserId(), student.getUserId());
            throw new ForbiddenAccessException("Students can only view their own profile");
        }

        return StudentResponse.fromStudent(student);
    }

    /**
     * List all students with optional filters for department and admission status.
     * Only accessible to ADMIN users.
     */
    public List<StudentResponse> listStudents(String department, AdmissionStatus status) {
        List<Student> students;

        boolean hasDept = department != null && !department.isBlank();
        boolean hasStatus = status != null;

        if (hasDept && hasStatus) {
            students = studentRepository.findByDepartmentIgnoreCaseAndAdmissionStatus(department.trim(), status);
        } else if (hasDept) {
            students = studentRepository.findByDepartmentIgnoreCase(department.trim());
        } else if (hasStatus) {
            students = studentRepository.findByAdmissionStatus(status);
        } else {
            students = studentRepository.findAll();
        }

        return students.stream()
                .map(StudentResponse::fromStudent)
                .collect(Collectors.toList());
    }

    /**
     * Determines whether a student profile has all core required fields populated.
     */
    public boolean calculateProfileCompleted(Student student) {
        if (student == null) {
            return false;
        }
        return student.getFirstName() != null && !student.getFirstName().isBlank()
                && student.getLastName() != null && !student.getLastName().isBlank()
                && student.getPhone() != null && !student.getPhone().isBlank()
                && student.getDateOfBirth() != null
                && student.getAddress() != null && !student.getAddress().isBlank()
                && student.getProgram() != null && !student.getProgram().isBlank()
                && student.getDepartment() != null && !student.getDepartment().isBlank()
                && student.getEnrollmentYear() != null;
    }
}

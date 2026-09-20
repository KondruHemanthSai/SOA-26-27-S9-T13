package com.studentcentral.admission.service;

import com.studentcentral.admission.client.NotificationServiceClient;
import com.studentcentral.admission.client.StudentServiceClient;
import com.studentcentral.admission.dto.*;
import com.studentcentral.admission.exception.*;
import com.studentcentral.admission.model.*;
import com.studentcentral.admission.repository.AdmissionApplicationRepository;
import com.studentcentral.admission.repository.DocumentRepository;
import com.studentcentral.admission.security.AuthenticatedUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AdmissionService {

    private static final Logger log = LoggerFactory.getLogger(AdmissionService.class);

    private final AdmissionApplicationRepository applicationRepository;
    private final DocumentRepository documentRepository;
    private final ApplicationIdGenerator applicationIdGenerator;
    private final StudentServiceClient studentServiceClient;
    private final NotificationServiceClient notificationServiceClient;

    public AdmissionService(AdmissionApplicationRepository applicationRepository,
                            DocumentRepository documentRepository,
                            ApplicationIdGenerator applicationIdGenerator,
                            StudentServiceClient studentServiceClient) {
        this(applicationRepository, documentRepository, applicationIdGenerator, studentServiceClient, null);
    }

    @org.springframework.beans.factory.annotation.Autowired
    public AdmissionService(AdmissionApplicationRepository applicationRepository,
                            DocumentRepository documentRepository,
                            ApplicationIdGenerator applicationIdGenerator,
                            StudentServiceClient studentServiceClient,
                            NotificationServiceClient notificationServiceClient) {
        this.applicationRepository = applicationRepository;
        this.documentRepository = documentRepository;
        this.applicationIdGenerator = applicationIdGenerator;
        this.studentServiceClient = studentServiceClient;
        this.notificationServiceClient = notificationServiceClient;
    }

    /**
     * Creates a new admission application in DRAFT status for the authenticated student.
     */
    public ApplicationResponse createApplication(AuthenticatedUser user, CreateApplicationRequest request, String authHeader) {
        if (applicationRepository.existsByUserId(user.getUserId())) {
            log.warn("User {} already has an admission application", user.getUserId());
            throw new ApplicationAlreadyExistsException("An admission application already exists for this student");
        }

        String resolvedStudentId = studentServiceClient.resolveStudentId(user.getUserId(), authHeader);
        String studentId = resolvedStudentId != null ? resolvedStudentId : user.getUserId();

        String generatedApplicationId = applicationIdGenerator.generateApplicationId();

        AcademicDetails academicDetails = null;
        if (request.getAcademicDetails() != null) {
            academicDetails = new AcademicDetails(
                    request.getAcademicDetails().getPreviousInstitution(),
                    request.getAcademicDetails().getBoard(),
                    request.getAcademicDetails().getPercentage(),
                    request.getAcademicDetails().getCgpa(),
                    request.getAcademicDetails().getGraduationYear()
            );
        }

        AdmissionApplication application = new AdmissionApplication(
                generatedApplicationId,
                studentId,
                user.getUserId(),
                request.getProgram(),
                request.getDepartment(),
                academicDetails
        );

        AdmissionApplication saved = applicationRepository.save(application);
        log.info("Admission application created successfully: applicationId={}, userId={}", saved.getApplicationId(), user.getUserId());
        return ApplicationResponse.fromModel(saved, List.of());
    }

    /**
     * Retrieves the authenticated student's own application.
     */
    public ApplicationResponse getOwnApplication(String userId) {
        AdmissionApplication application = applicationRepository.findByUserId(userId)
                .orElseThrow(() -> new ApplicationNotFoundException("No admission application found for current student"));

        List<DocumentResponse> documents = getDocumentResponsesForApplication(application.getApplicationId());
        return ApplicationResponse.fromModel(application, documents);
    }

    /**
     * Updates an application while in DRAFT or CHANGES_REQUESTED status.
     */
    public ApplicationResponse updateOwnApplication(String userId, String idOrAppId, UpdateApplicationRequest request) {
        AdmissionApplication application = findApplicationByIdOrAppId(idOrAppId);

        validateApplicationOwnership(application, userId);

        if (application.getStatus() != ApplicationStatus.DRAFT && application.getStatus() != ApplicationStatus.CHANGES_REQUESTED) {
            log.warn("Attempt to update application {} in non-editable status {}", application.getApplicationId(), application.getStatus());
            throw new ApplicationNotEditableException("Application cannot be modified in status: " + application.getStatus());
        }

        if (request.getProgram() != null && !request.getProgram().isBlank()) {
            application.setProgram(request.getProgram());
        }
        if (request.getDepartment() != null && !request.getDepartment().isBlank()) {
            application.setDepartment(request.getDepartment());
        }
        if (request.getAcademicDetails() != null) {
            AcademicDetails details = application.getAcademicDetails();
            if (details == null) {
                details = new AcademicDetails();
            }
            if (request.getAcademicDetails().getPreviousInstitution() != null) {
                details.setPreviousInstitution(request.getAcademicDetails().getPreviousInstitution());
            }
            if (request.getAcademicDetails().getBoard() != null) {
                details.setBoard(request.getAcademicDetails().getBoard());
            }
            if (request.getAcademicDetails().getPercentage() != null) {
                details.setPercentage(request.getAcademicDetails().getPercentage());
            }
            if (request.getAcademicDetails().getCgpa() != null) {
                details.setCgpa(request.getAcademicDetails().getCgpa());
            }
            if (request.getAcademicDetails().getGraduationYear() != null) {
                details.setGraduationYear(request.getAcademicDetails().getGraduationYear());
            }
            application.setAcademicDetails(details);
        }

        application.setUpdatedAt(Instant.now());
        AdmissionApplication updated = applicationRepository.save(application);
        log.info("Admission application {} updated by user {}", updated.getApplicationId(), userId);

        List<DocumentResponse> documents = getDocumentResponsesForApplication(updated.getApplicationId());
        return ApplicationResponse.fromModel(updated, documents);
    }

    /**
     * Submits an application transitioning status to SUBMITTED after validating required fields and documents.
     */
    public ApplicationResponse submitApplication(String userId, String idOrAppId) {
        AdmissionApplication application = findApplicationByIdOrAppId(idOrAppId);

        validateApplicationOwnership(application, userId);

        if (application.getStatus() != ApplicationStatus.DRAFT && application.getStatus() != ApplicationStatus.CHANGES_REQUESTED) {
            log.warn("Cannot submit application {} in status {}", application.getApplicationId(), application.getStatus());
            throw new ApplicationNotSubmittableException("Only DRAFT or CHANGES_REQUESTED applications can be submitted. Current status: " + application.getStatus());
        }

        if (application.getProgram() == null || application.getProgram().isBlank()) {
            throw new ApplicationNotSubmittableException("Application cannot be submitted without a program specified");
        }
        if (application.getDepartment() == null || application.getDepartment().isBlank()) {
            throw new ApplicationNotSubmittableException("Application cannot be submitted without a department specified");
        }

        // Validate required documents: ID_PROOF, MARKS_CERTIFICATE, TRANSFER_CERTIFICATE
        validateRequiredDocuments(application.getApplicationId());

        application.setStatus(ApplicationStatus.SUBMITTED);
        application.setSubmissionDate(Instant.now());
        application.setUpdatedAt(Instant.now());

        AdmissionApplication saved = applicationRepository.save(application);
        log.info("Admission application {} submitted successfully by user {}", saved.getApplicationId(), userId);

        List<DocumentResponse> documents = getDocumentResponsesForApplication(saved.getApplicationId());
        return ApplicationResponse.fromModel(saved, documents);
    }

    /**
     * Uploads document metadata for an application.
     */
    public DocumentResponse uploadDocument(String userId, String idOrAppId, UploadDocumentRequest request) {
        AdmissionApplication application = findApplicationByIdOrAppId(idOrAppId);

        validateApplicationOwnership(application, userId);

        if (application.getStatus() == ApplicationStatus.APPROVED || application.getStatus() == ApplicationStatus.REJECTED) {
            throw new ApplicationNotEditableException("Cannot upload documents to an application in status: " + application.getStatus());
        }

        Document document = new Document(
                application.getApplicationId(),
                application.getStudentId(),
                request.getType(),
                request.getFileName(),
                request.getFileUrl() != null ? request.getFileUrl() : "/documents/" + application.getApplicationId() + "/" + request.getFileName()
        );

        Document saved = documentRepository.save(document);
        log.info("Document {} ({}) uploaded for application {}", saved.getId(), saved.getType(), application.getApplicationId());
        return DocumentResponse.fromModel(saved);
    }

    /**
     * Retrieves all documents for an application with authorization checks.
     */
    public List<DocumentResponse> getDocuments(AuthenticatedUser user, String idOrAppId) {
        AdmissionApplication application = findApplicationByIdOrAppId(idOrAppId);

        if (!user.getRole().equalsIgnoreCase("ADMIN") && !application.getUserId().equals(user.getUserId())) {
            log.warn("User {} attempted unauthorized document view for application {}", user.getUserId(), application.getApplicationId());
            throw new UnauthorizedApplicationAccessException("You do not have permission to view documents for this application");
        }

        return getDocumentResponsesForApplication(application.getApplicationId());
    }

    /**
     * Deletes a document if the application is in DRAFT or CHANGES_REQUESTED status.
     */
    public void deleteDocument(String userId, String idOrAppId, String documentId) {
        AdmissionApplication application = findApplicationByIdOrAppId(idOrAppId);

        validateApplicationOwnership(application, userId);

        if (application.getStatus() != ApplicationStatus.DRAFT && application.getStatus() != ApplicationStatus.CHANGES_REQUESTED) {
            throw new ApplicationNotEditableException("Documents cannot be deleted from an application in status: " + application.getStatus());
        }

        Document document = documentRepository.findByIdAndApplicationId(documentId, application.getApplicationId())
                .orElseThrow(() -> new DocumentNotFoundException("Document not found: " + documentId));

        documentRepository.delete(document);
        log.info("Document {} deleted from application {} by user {}", documentId, application.getApplicationId(), userId);
    }

    /**
     * Admin: Lists admission applications with optional department and status filtering.
     */
    public List<ApplicationResponse> listApplications(String department, ApplicationStatus status) {
        List<AdmissionApplication> applications;

        if (department != null && !department.isBlank() && status != null) {
            applications = applicationRepository.findByDepartmentIgnoreCaseAndStatus(department.trim(), status);
        } else if (department != null && !department.isBlank()) {
            applications = applicationRepository.findByDepartmentIgnoreCase(department.trim());
        } else if (status != null) {
            applications = applicationRepository.findByStatus(status);
        } else {
            applications = applicationRepository.findAll();
        }

        return applications.stream()
                .map(app -> ApplicationResponse.fromModel(app, getDocumentResponsesForApplication(app.getApplicationId())))
                .collect(Collectors.toList());
    }

    /**
     * Admin: Retrieves application details and documents by ID or applicationId.
     */
    public ApplicationResponse getApplicationById(String idOrAppId) {
        AdmissionApplication application = findApplicationByIdOrAppId(idOrAppId);
        List<DocumentResponse> documents = getDocumentResponsesForApplication(application.getApplicationId());
        return ApplicationResponse.fromModel(application, documents);
    }

    /**
     * Admin: Executes an administrative review action (START_REVIEW, APPROVE, REJECT, REQUEST_CHANGES).
     */
    public ApplicationResponse reviewApplication(String adminUserId, String idOrAppId, ApplicationReviewRequest request) {
        AdmissionApplication application = findApplicationByIdOrAppId(idOrAppId);

        ApplicationStatus currentStatus = application.getStatus();
        ReviewAction action = request.getAction();
        ApplicationStatus targetStatus;

        switch (action) {
            case START_REVIEW:
                if (currentStatus != ApplicationStatus.SUBMITTED) {
                    throw new InvalidApplicationStatusException("Cannot start review on application in status: " + currentStatus + ". Must be SUBMITTED.");
                }
                targetStatus = ApplicationStatus.UNDER_REVIEW;
                break;

            case APPROVE:
                if (currentStatus != ApplicationStatus.UNDER_REVIEW && currentStatus != ApplicationStatus.SUBMITTED) {
                    throw new InvalidApplicationStatusException("Cannot approve application in status: " + currentStatus + ". Must be SUBMITTED or UNDER_REVIEW.");
                }
                targetStatus = ApplicationStatus.APPROVED;
                break;

            case REJECT:
                if (currentStatus != ApplicationStatus.UNDER_REVIEW && currentStatus != ApplicationStatus.SUBMITTED) {
                    throw new InvalidApplicationStatusException("Cannot reject application in status: " + currentStatus + ". Must be SUBMITTED or UNDER_REVIEW.");
                }
                targetStatus = ApplicationStatus.REJECTED;
                break;

            case REQUEST_CHANGES:
                if (currentStatus != ApplicationStatus.UNDER_REVIEW && currentStatus != ApplicationStatus.SUBMITTED) {
                    throw new InvalidApplicationStatusException("Cannot request changes on application in status: " + currentStatus + ". Must be SUBMITTED or UNDER_REVIEW.");
                }
                targetStatus = ApplicationStatus.CHANGES_REQUESTED;
                break;

            default:
                throw new InvalidApplicationStatusException("Unsupported review action: " + action);
        }

        application.setStatus(targetStatus);
        application.setReviewDate(Instant.now());
        application.setReviewedBy(adminUserId);
        if (request.getRemarks() != null && !request.getRemarks().isBlank()) {
            application.setRemarks(request.getRemarks());
        }
        application.setUpdatedAt(Instant.now());

        AdmissionApplication saved = applicationRepository.save(application);
        log.info("Admission application {} transitioned from {} to {} by admin {}", saved.getApplicationId(), currentStatus, targetStatus, adminUserId);

        List<DocumentResponse> documents = getDocumentResponsesForApplication(saved.getApplicationId());

        // Best-effort notification — failure does not affect the admission review
        if (notificationServiceClient != null) {
            try {
                switch (targetStatus) {
                    case APPROVED:
                        notificationServiceClient.sendAdmissionApprovedNotification(
                                saved.getUserId(), saved.getApplicationId());
                        break;
                    case REJECTED:
                        notificationServiceClient.sendAdmissionRejectedNotification(
                                saved.getUserId(), saved.getApplicationId());
                        break;
                    case CHANGES_REQUESTED:
                        notificationServiceClient.sendAdmissionChangesRequestedNotification(
                                saved.getUserId(), saved.getApplicationId());
                        break;
                    default:
                        break;
                }
            } catch (Exception e) {
                log.warn("Failed to send admission notification for applicationId={}: {}",
                        saved.getApplicationId(), e.getMessage());
            }
        }

        return ApplicationResponse.fromModel(saved, documents);
    }

    /**
     * Admin: Reviews an individual document.
     */
    public DocumentResponse reviewDocument(String adminUserId, String idOrAppId, String documentId, DocumentReviewRequest request) {
        AdmissionApplication application = findApplicationByIdOrAppId(idOrAppId);

        Document document = documentRepository.findByIdAndApplicationId(documentId, application.getApplicationId())
                .orElseThrow(() -> new DocumentNotFoundException("Document not found: " + documentId));

        document.setStatus(request.getStatus());
        document.setReviewedBy(adminUserId);
        document.setReviewedAt(Instant.now());
        if (request.getRemarks() != null && !request.getRemarks().isBlank()) {
            document.setRemarks(request.getRemarks());
        }

        Document saved = documentRepository.save(document);
        log.info("Document {} status updated to {} by admin {}", saved.getId(), saved.getStatus(), adminUserId);
        return DocumentResponse.fromModel(saved);
    }

    // ==========================================
    // Internal Helper Methods
    // ==========================================

    private AdmissionApplication findApplicationByIdOrAppId(String idOrAppId) {
        Optional<AdmissionApplication> byAppId = applicationRepository.findByApplicationId(idOrAppId);
        if (byAppId.isPresent()) {
            return byAppId.get();
        }

        Optional<AdmissionApplication> byId = applicationRepository.findById(idOrAppId);
        if (byId.isPresent()) {
            return byId.get();
        }

        Optional<AdmissionApplication> byUserId = applicationRepository.findByUserId(idOrAppId);
        if (byUserId.isPresent()) {
            return byUserId.get();
        }

        throw new ApplicationNotFoundException("Admission application not found: " + idOrAppId);
    }

    private void validateApplicationOwnership(AdmissionApplication application, String userId) {
        if (!application.getUserId().equals(userId)) {
            log.warn("User {} attempted unauthorized modification of application {}", userId, application.getApplicationId());
            throw new UnauthorizedApplicationAccessException("You do not have permission to modify this admission application");
        }
    }

    private void validateRequiredDocuments(String applicationId) {
        boolean hasIdProof = documentRepository.existsByApplicationIdAndType(applicationId, DocumentType.ID_PROOF);
        boolean hasMarksCert = documentRepository.existsByApplicationIdAndType(applicationId, DocumentType.MARKS_CERTIFICATE);
        boolean hasTransferCert = documentRepository.existsByApplicationIdAndType(applicationId, DocumentType.TRANSFER_CERTIFICATE);

        if (!hasIdProof || !hasMarksCert || !hasTransferCert) {
            StringBuilder missing = new StringBuilder("Required document(s) missing: ");
            if (!hasIdProof) missing.append("[ID_PROOF] ");
            if (!hasMarksCert) missing.append("[MARKS_CERTIFICATE] ");
            if (!hasTransferCert) missing.append("[TRANSFER_CERTIFICATE] ");
            log.warn("Application {} submission blocked: {}", applicationId, missing.toString().trim());
            throw new RequiredDocumentMissingException(missing.toString().trim());
        }
    }

    private List<DocumentResponse> getDocumentResponsesForApplication(String applicationId) {
        return documentRepository.findByApplicationId(applicationId).stream()
                .map(DocumentResponse::fromModel)
                .collect(Collectors.toList());
    }
}

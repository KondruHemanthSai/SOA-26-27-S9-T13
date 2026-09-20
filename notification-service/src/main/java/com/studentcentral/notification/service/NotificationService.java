package com.studentcentral.notification.service;

import com.studentcentral.notification.dto.*;
import com.studentcentral.notification.exception.NotificationNotFoundException;
import com.studentcentral.notification.exception.NotificationNotOwnedException;
import com.studentcentral.notification.model.Notification;
import com.studentcentral.notification.model.NotificationPriority;
import com.studentcentral.notification.model.NotificationType;
import com.studentcentral.notification.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notificationRepository;
    private final NotificationIdGenerator notificationIdGenerator;

    private final int defaultPageSize;
    private final int maxPageSize;

    public NotificationService(NotificationRepository notificationRepository,
                               NotificationIdGenerator notificationIdGenerator,
                               @Value("${notification.default-page-size:20}") int defaultPageSize,
                               @Value("${notification.max-page-size:100}") int maxPageSize) {
        this.notificationRepository = notificationRepository;
        this.notificationIdGenerator = notificationIdGenerator;
        this.defaultPageSize = defaultPageSize;
        this.maxPageSize = maxPageSize;
    }

    // ==========================================
    // Core Notification Creation
    // ==========================================

    /**
     * Centralized notification creation helper.
     */
    public Notification createNotification(String userId, String title, String message,
                                           NotificationType type, NotificationPriority priority,
                                           String relatedEntityType, String relatedEntityId,
                                           String createdBy, String createdByRole) {
        String notificationId = notificationIdGenerator.generateNotificationId();

        Notification notification = new Notification(
                notificationId, userId, title, message,
                type, priority, relatedEntityType, relatedEntityId,
                createdBy, createdByRole
        );

        Notification saved = notificationRepository.save(notification);
        log.info("Notification created: notificationId={}, userId={}, type={}, priority={}",
                saved.getNotificationId(), userId, type, priority);
        return saved;
    }

    // ==========================================
    // Student Operations
    // ==========================================

    /**
     * Returns paginated notifications for the authenticated student, sorted by createdAt DESC.
     */
    public NotificationPageResponse getMyNotifications(String userId, int page, int size) {
        int validatedSize = validatePageSize(size);
        Pageable pageable = PageRequest.of(page, validatedSize, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Notification> notificationPage = notificationRepository.findByUserId(userId, pageable);

        List<NotificationResponse> content = notificationPage.getContent().stream()
                .map(NotificationResponse::fromModel)
                .collect(Collectors.toList());

        return new NotificationPageResponse(
                content,
                notificationPage.getNumber(),
                notificationPage.getSize(),
                notificationPage.getTotalElements(),
                notificationPage.getTotalPages()
        );
    }

    /**
     * Returns unread notifications for the authenticated student, sorted by createdAt DESC.
     */
    public List<NotificationResponse> getMyUnreadNotifications(String userId) {
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        List<Notification> notifications = notificationRepository.findByUserIdAndIsReadFalse(userId, sort);
        return notifications.stream()
                .map(NotificationResponse::fromModel)
                .collect(Collectors.toList());
    }

    /**
     * Returns unread notification count for the authenticated student.
     */
    public UnreadCountResponse getUnreadCount(String userId) {
        long count = notificationRepository.countByUserIdAndIsReadFalse(userId);
        return new UnreadCountResponse(count);
    }

    /**
     * Returns a single notification by notificationId with ownership validation.
     */
    public NotificationResponse getNotificationById(String userId, String notificationId) {
        Notification notification = notificationRepository.findByNotificationId(notificationId)
                .orElseThrow(() -> new NotificationNotFoundException(
                        "Notification not found: " + notificationId));

        validateOwnership(userId, notification);
        return NotificationResponse.fromModel(notification);
    }

    /**
     * Marks a single notification as read.
     */
    public NotificationResponse markAsRead(String userId, String notificationId) {
        Notification notification = notificationRepository.findByNotificationId(notificationId)
                .orElseThrow(() -> new NotificationNotFoundException(
                        "Notification not found: " + notificationId));

        validateOwnership(userId, notification);

        if (!notification.getIsRead()) {
            notification.setIsRead(true);
            notification.setReadAt(Instant.now());
            notification = notificationRepository.save(notification);
            log.info("Notification marked as read: notificationId={}, userId={}", notificationId, userId);
        }

        return NotificationResponse.fromModel(notification);
    }

    /**
     * Marks all notifications for the authenticated student as read.
     */
    public void markAllAsRead(String userId) {
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        List<Notification> unread = notificationRepository.findByUserIdAndIsReadFalse(userId, sort);
        Instant now = Instant.now();

        for (Notification notification : unread) {
            notification.setIsRead(true);
            notification.setReadAt(now);
        }

        if (!unread.isEmpty()) {
            notificationRepository.saveAll(unread);
            log.info("Marked {} notifications as read for userId={}", unread.size(), userId);
        }
    }

    /**
     * Deletes a notification owned by the authenticated student.
     */
    public void deleteNotification(String userId, String notificationId) {
        Notification notification = notificationRepository.findByNotificationId(notificationId)
                .orElseThrow(() -> new NotificationNotFoundException(
                        "Notification not found: " + notificationId));

        validateOwnership(userId, notification);

        notificationRepository.delete(notification);
        log.info("Notification deleted: notificationId={}, userId={}", notificationId, userId);
    }

    // ==========================================
    // Admin Operations
    // ==========================================

    /**
     * Admin: Creates a notification for a specific student.
     */
    public NotificationResponse createAdminNotification(String adminUserId, CreateNotificationRequest request) {
        Notification notification = createNotification(
                request.getUserId(),
                request.getTitle(),
                request.getMessage(),
                request.getType(),
                request.getPriority(),
                request.getRelatedEntityType(),
                request.getRelatedEntityId(),
                adminUserId,
                "ADMIN"
        );
        log.info("Admin notification created by {}: notificationId={}, targetUser={}",
                adminUserId, notification.getNotificationId(), request.getUserId());
        return NotificationResponse.fromModel(notification);
    }

    /**
     * Admin: Broadcasts a notification to multiple students (creates N separate documents).
     */
    public List<NotificationResponse> broadcastNotification(String adminUserId, BroadcastNotificationRequest request) {
        List<NotificationResponse> results = new ArrayList<>();

        for (String targetUserId : request.getUserIds()) {
            try {
                Notification notification = createNotification(
                        targetUserId,
                        request.getTitle(),
                        request.getMessage(),
                        request.getType(),
                        request.getPriority(),
                        null, null,
                        adminUserId,
                        "ADMIN"
                );
                results.add(NotificationResponse.fromModel(notification));
            } catch (Exception e) {
                log.error("Failed to create broadcast notification for userId={}: {}", targetUserId, e.getMessage());
            }
        }

        log.info("Broadcast notification sent by admin {}: {} of {} delivered",
                adminUserId, results.size(), request.getUserIds().size());
        return results;
    }

    /**
     * Admin: Returns notifications with optional filters, paginated.
     */
    public NotificationPageResponse getAdminNotifications(String userId, NotificationType type,
                                                          NotificationPriority priority, Boolean isRead,
                                                          int page, int size) {
        int validatedSize = validatePageSize(size);
        Pageable pageable = PageRequest.of(page, validatedSize, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Notification> notificationPage;

        // Use specific query methods for common filter combinations
        if (userId != null && type == null && priority == null && isRead == null) {
            notificationPage = notificationRepository.findByUserId(userId, pageable);
        } else if (userId == null && type != null && priority == null && isRead == null) {
            notificationPage = notificationRepository.findByType(type, pageable);
        } else if (userId == null && type == null && priority != null && isRead == null) {
            notificationPage = notificationRepository.findByPriority(priority, pageable);
        } else if (userId == null && type == null && priority == null && isRead != null) {
            notificationPage = notificationRepository.findByIsRead(isRead, pageable);
        } else if (userId != null && type != null && priority == null && isRead == null) {
            notificationPage = notificationRepository.findByUserIdAndType(userId, type, pageable);
        } else if (userId == null && type == null && priority == null && isRead == null) {
            notificationPage = notificationRepository.findAll(pageable);
        } else {
            notificationPage = notificationRepository.findByFilters(userId, type, priority, isRead, pageable);
        }

        List<NotificationResponse> content = notificationPage.getContent().stream()
                .map(NotificationResponse::fromModel)
                .collect(Collectors.toList());

        return new NotificationPageResponse(
                content,
                notificationPage.getNumber(),
                notificationPage.getSize(),
                notificationPage.getTotalElements(),
                notificationPage.getTotalPages()
        );
    }

    /**
     * Admin: Returns notification history for a specific student.
     */
    public List<NotificationResponse> getNotificationsByUserId(String userId) {
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        List<Notification> notifications = notificationRepository.findByUserId(userId, sort);
        return notifications.stream()
                .map(NotificationResponse::fromModel)
                .collect(Collectors.toList());
    }

    // ==========================================
    // Internal Service Operations
    // ==========================================

    /**
     * Internal: Creates a notification from a service-to-service call.
     */
    public NotificationResponse createInternalNotification(InternalNotificationRequest request) {
        Notification notification = createNotification(
                request.getUserId(),
                request.getTitle(),
                request.getMessage(),
                request.getType(),
                request.getPriority(),
                request.getRelatedEntityType(),
                request.getRelatedEntityId(),
                request.getCreatedBy() != null ? request.getCreatedBy() : "SYSTEM",
                "SYSTEM"
        );
        log.info("Internal notification created: notificationId={}, userId={}, type={}",
                notification.getNotificationId(), request.getUserId(), request.getType());
        return NotificationResponse.fromModel(notification);
    }

    // ==========================================
    // Internal Helpers
    // ==========================================

    private void validateOwnership(String userId, Notification notification) {
        if (!notification.getUserId().equals(userId)) {
            log.warn("User {} attempted to access notification {} owned by {}",
                    userId, notification.getNotificationId(), notification.getUserId());
            throw new NotificationNotOwnedException("You do not have permission to access this notification");
        }
    }

    private int validatePageSize(int size) {
        if (size <= 0) {
            return defaultPageSize;
        }
        return Math.min(size, maxPageSize);
    }
}

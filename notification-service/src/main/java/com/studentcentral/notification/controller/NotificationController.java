package com.studentcentral.notification.controller;

import com.studentcentral.notification.dto.*;
import com.studentcentral.notification.model.NotificationPriority;
import com.studentcentral.notification.model.NotificationType;
import com.studentcentral.notification.security.AuthenticatedUser;
import com.studentcentral.notification.service.NotificationService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private static final Logger log = LoggerFactory.getLogger(NotificationController.class);

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    // ==========================================
    // Student Endpoints
    // ==========================================

    /**
     * Student: Get own notifications (paginated, sorted by createdAt DESC).
     */
    @GetMapping("/my")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<NotificationPageResponse>> getMyNotifications(
            @AuthenticationPrincipal AuthenticatedUser user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.info("REST request to get notifications for user {}, page={}, size={}", user.getUserId(), page, size);
        NotificationPageResponse response = notificationService.getMyNotifications(user.getUserId(), page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Student: Get own unread notifications.
     */
    @GetMapping("/my/unread")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getMyUnreadNotifications(
            @AuthenticationPrincipal AuthenticatedUser user) {
        log.info("REST request to get unread notifications for user {}", user.getUserId());
        List<NotificationResponse> notifications = notificationService.getMyUnreadNotifications(user.getUserId());
        return ResponseEntity.ok(ApiResponse.success(notifications));
    }

    /**
     * Student: Get unread notification count.
     */
    @GetMapping("/my/unread-count")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<UnreadCountResponse>> getMyUnreadCount(
            @AuthenticationPrincipal AuthenticatedUser user) {
        log.info("REST request to get unread count for user {}", user.getUserId());
        UnreadCountResponse response = notificationService.getUnreadCount(user.getUserId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Student: Mark all own notifications as read.
     */
    @PutMapping("/my/read-all")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead(
            @AuthenticationPrincipal AuthenticatedUser user) {
        log.info("REST request to mark all notifications as read for user {}", user.getUserId());
        notificationService.markAllAsRead(user.getUserId());
        return ResponseEntity.ok(ApiResponse.success("All notifications marked as read"));
    }

    /**
     * Authenticated: Get a specific notification by notificationId (ownership checked in service).
     */
    @GetMapping("/{notificationId}")
    public ResponseEntity<ApiResponse<NotificationResponse>> getNotificationById(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable String notificationId) {
        log.info("REST request to get notification {} by user {}", notificationId, user.getUserId());
        NotificationResponse response = notificationService.getNotificationById(user.getUserId(), notificationId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Authenticated: Mark a specific notification as read (ownership checked in service).
     */
    @PutMapping("/{notificationId}/read")
    public ResponseEntity<ApiResponse<NotificationResponse>> markAsRead(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable String notificationId) {
        log.info("REST request to mark notification {} as read by user {}", notificationId, user.getUserId());
        NotificationResponse response = notificationService.markAsRead(user.getUserId(), notificationId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Authenticated: Delete a specific notification (ownership checked in service).
     */
    @DeleteMapping("/{notificationId}")
    public ResponseEntity<Void> deleteNotification(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable String notificationId) {
        log.info("REST request to delete notification {} by user {}", notificationId, user.getUserId());
        notificationService.deleteNotification(user.getUserId(), notificationId);
        return ResponseEntity.noContent().build();
    }

    // ==========================================
    // Admin Endpoints
    // ==========================================

    /**
     * Admin: Create a notification for a specific student.
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<NotificationResponse>> createNotification(
            @AuthenticationPrincipal AuthenticatedUser admin,
            @Valid @RequestBody CreateNotificationRequest request) {
        log.info("REST request by admin {} to create notification for user {}", admin.getUserId(), request.getUserId());
        NotificationResponse response = notificationService.createAdminNotification(admin.getUserId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Notification created", response));
    }

    /**
     * Admin: Broadcast notification to multiple students.
     */
    @PostMapping("/broadcast")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> broadcastNotification(
            @AuthenticationPrincipal AuthenticatedUser admin,
            @Valid @RequestBody BroadcastNotificationRequest request) {
        log.info("REST request by admin {} to broadcast notification to {} users",
                admin.getUserId(), request.getUserIds().size());
        List<NotificationResponse> responses = notificationService.broadcastNotification(admin.getUserId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Broadcast notification sent to " + responses.size() + " users", responses));
    }

    /**
     * Admin: Get notifications with optional filters.
     */
    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<NotificationPageResponse>> getAdminNotifications(
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) NotificationType type,
            @RequestParam(required = false) NotificationPriority priority,
            @RequestParam(required = false) Boolean isRead,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.info("REST request by admin to list notifications (userId={}, type={}, priority={}, isRead={})",
                userId, type, priority, isRead);
        NotificationPageResponse response = notificationService.getAdminNotifications(
                userId, type, priority, isRead, page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Admin: Get notification history for a specific student.
     */
    @GetMapping("/admin/user/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getNotificationsByUserId(
            @PathVariable String userId) {
        log.info("REST request by admin to get notifications for user {}", userId);
        List<NotificationResponse> responses = notificationService.getNotificationsByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    // ==========================================
    // Internal Service Endpoint
    // ==========================================

    /**
     * Internal: Create notification from service-to-service call.
     * Temporarily secured by permitAll with header validation.
     * A proper service-to-service auth mechanism should be added in future phases.
     */
    @PostMapping("/internal")
    public ResponseEntity<ApiResponse<NotificationResponse>> createInternalNotification(
            @Valid @RequestBody InternalNotificationRequest request) {
        log.info("Internal notification request received: userId={}, type={}", request.getUserId(), request.getType());
        NotificationResponse response = notificationService.createInternalNotification(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Notification created", response));
    }
}

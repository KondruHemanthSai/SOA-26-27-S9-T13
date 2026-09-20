package com.studentcentral.notification.service;

import com.studentcentral.notification.dto.*;
import com.studentcentral.notification.exception.NotificationNotFoundException;
import com.studentcentral.notification.exception.NotificationNotOwnedException;
import com.studentcentral.notification.model.Notification;
import com.studentcentral.notification.model.NotificationPriority;
import com.studentcentral.notification.model.NotificationType;
import com.studentcentral.notification.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private NotificationIdGenerator notificationIdGenerator;

    private NotificationService notificationService;

    private static final String USER_ID = "user-101";
    private static final String OTHER_USER_ID = "user-999";
    private static final String ADMIN_USER_ID = "admin-001";

    @BeforeEach
    void setUp() {
        notificationService = new NotificationService(
                notificationRepository,
                notificationIdGenerator,
                20,  // defaultPageSize
                100  // maxPageSize
        );
    }

    // ==========================================
    // 1. Create Notification
    // ==========================================

    @Test
    void shouldCreateNotificationSuccessfully() {
        when(notificationIdGenerator.generateNotificationId()).thenReturn("NOTIF-ABCD1234");
        when(notificationRepository.save(any(Notification.class))).thenAnswer(i -> i.getArgument(0));

        Notification result = notificationService.createNotification(
                USER_ID, "Test Title", "Test Message",
                NotificationType.REGISTRATION, NotificationPriority.NORMAL,
                "REGISTRATION", "REG-001", "system", "SYSTEM"
        );

        assertNotNull(result);
        assertEquals("NOTIF-ABCD1234", result.getNotificationId());
        assertEquals(USER_ID, result.getUserId());
        assertEquals("Test Title", result.getTitle());
        assertFalse(result.getIsRead());
        assertNotNull(result.getCreatedAt());
        assertNull(result.getReadAt());
        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    // ==========================================
    // 2. Get Student Notifications (Paginated)
    // ==========================================

    @Test
    void shouldGetMyNotificationsPaginated() {
        Notification n1 = createTestNotification("NOTIF-001", USER_ID, "Title1");
        Notification n2 = createTestNotification("NOTIF-002", USER_ID, "Title2");
        Page<Notification> page = new PageImpl<>(List.of(n1, n2), PageRequest.of(0, 20), 2);

        when(notificationRepository.findByUserId(eq(USER_ID), any(Pageable.class))).thenReturn(page);

        NotificationPageResponse response = notificationService.getMyNotifications(USER_ID, 0, 20);

        assertEquals(2, response.getContent().size());
        assertEquals(0, response.getPage());
        assertEquals(20, response.getSize());
        assertEquals(2, response.getTotalElements());
        assertEquals(1, response.getTotalPages());
    }

    // ==========================================
    // 3. Get Unread Notifications
    // ==========================================

    @Test
    void shouldGetMyUnreadNotifications() {
        Notification n1 = createTestNotification("NOTIF-001", USER_ID, "Unread1");
        n1.setIsRead(false);

        when(notificationRepository.findByUserIdAndIsReadFalse(eq(USER_ID), any(Sort.class)))
                .thenReturn(List.of(n1));

        List<NotificationResponse> responses = notificationService.getMyUnreadNotifications(USER_ID);

        assertEquals(1, responses.size());
        assertFalse(responses.get(0).isRead());
    }

    // ==========================================
    // 4. Get Unread Count
    // ==========================================

    @Test
    void shouldGetUnreadCount() {
        when(notificationRepository.countByUserIdAndIsReadFalse(USER_ID)).thenReturn(4L);

        UnreadCountResponse response = notificationService.getUnreadCount(USER_ID);

        assertEquals(4, response.getUnreadCount());
    }

    // ==========================================
    // 5. Mark Notification as Read
    // ==========================================

    @Test
    void shouldMarkNotificationAsRead() {
        Notification notification = createTestNotification("NOTIF-001", USER_ID, "Test");
        notification.setIsRead(false);

        when(notificationRepository.findByNotificationId("NOTIF-001")).thenReturn(Optional.of(notification));
        when(notificationRepository.save(any(Notification.class))).thenAnswer(i -> i.getArgument(0));

        NotificationResponse response = notificationService.markAsRead(USER_ID, "NOTIF-001");

        assertTrue(response.isRead());
        assertNotNull(response.getReadAt());
        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    @Test
    void shouldNotSaveWhenAlreadyRead() {
        Notification notification = createTestNotification("NOTIF-001", USER_ID, "Test");
        notification.setIsRead(true);
        notification.setReadAt(Instant.now());

        when(notificationRepository.findByNotificationId("NOTIF-001")).thenReturn(Optional.of(notification));

        NotificationResponse response = notificationService.markAsRead(USER_ID, "NOTIF-001");

        assertTrue(response.isRead());
        verify(notificationRepository, never()).save(any(Notification.class));
    }

    // ==========================================
    // 6. Mark All as Read
    // ==========================================

    @Test
    void shouldMarkAllNotificationsAsRead() {
        Notification n1 = createTestNotification("NOTIF-001", USER_ID, "Title1");
        Notification n2 = createTestNotification("NOTIF-002", USER_ID, "Title2");

        when(notificationRepository.findByUserIdAndIsReadFalse(eq(USER_ID), any(Sort.class)))
                .thenReturn(List.of(n1, n2));

        notificationService.markAllAsRead(USER_ID);

        assertTrue(n1.getIsRead());
        assertTrue(n2.getIsRead());
        assertNotNull(n1.getReadAt());
        assertNotNull(n2.getReadAt());
        verify(notificationRepository, times(1)).saveAll(anyList());
    }

    // ==========================================
    // 7. Delete Notification
    // ==========================================

    @Test
    void shouldDeleteNotification() {
        Notification notification = createTestNotification("NOTIF-001", USER_ID, "Test");

        when(notificationRepository.findByNotificationId("NOTIF-001")).thenReturn(Optional.of(notification));

        notificationService.deleteNotification(USER_ID, "NOTIF-001");

        verify(notificationRepository, times(1)).delete(notification);
    }

    // ==========================================
    // 8. Student Cannot Access Another's Notification
    // ==========================================

    @Test
    void shouldThrowWhenStudentAccessesAnotherStudentsNotification() {
        Notification notification = createTestNotification("NOTIF-001", OTHER_USER_ID, "Other's notif");

        when(notificationRepository.findByNotificationId("NOTIF-001")).thenReturn(Optional.of(notification));

        assertThrows(NotificationNotOwnedException.class, () ->
                notificationService.getNotificationById(USER_ID, "NOTIF-001"));
    }

    @Test
    void shouldThrowWhenStudentDeletesAnotherStudentsNotification() {
        Notification notification = createTestNotification("NOTIF-001", OTHER_USER_ID, "Other's notif");

        when(notificationRepository.findByNotificationId("NOTIF-001")).thenReturn(Optional.of(notification));

        assertThrows(NotificationNotOwnedException.class, () ->
                notificationService.deleteNotification(USER_ID, "NOTIF-001"));
    }

    @Test
    void shouldThrowWhenStudentMarksAnotherStudentsNotificationAsRead() {
        Notification notification = createTestNotification("NOTIF-001", OTHER_USER_ID, "Other's notif");

        when(notificationRepository.findByNotificationId("NOTIF-001")).thenReturn(Optional.of(notification));

        assertThrows(NotificationNotOwnedException.class, () ->
                notificationService.markAsRead(USER_ID, "NOTIF-001"));
    }

    // ==========================================
    // 9. Admin Can Create Notification
    // ==========================================

    @Test
    void shouldAdminCreateNotification() {
        when(notificationIdGenerator.generateNotificationId()).thenReturn("NOTIF-ADMIN001");
        when(notificationRepository.save(any(Notification.class))).thenAnswer(i -> i.getArgument(0));

        CreateNotificationRequest request = new CreateNotificationRequest(
                USER_ID, "Admission Approved", "Your admission has been approved.",
                NotificationType.ADMISSION, NotificationPriority.HIGH
        );
        request.setRelatedEntityType("ADMISSION");
        request.setRelatedEntityId("APP-123");

        NotificationResponse response = notificationService.createAdminNotification(ADMIN_USER_ID, request);

        assertNotNull(response);
        assertEquals("NOTIF-ADMIN001", response.getNotificationId());
        assertEquals(USER_ID, response.getUserId());
        assertEquals("Admission Approved", response.getTitle());
        assertEquals("ADMIN", response.getCreatedByRole());
        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    // ==========================================
    // 10. Broadcast Notification
    // ==========================================

    @Test
    void shouldBroadcastNotificationToMultipleUsers() {
        when(notificationIdGenerator.generateNotificationId())
                .thenReturn("NOTIF-B1", "NOTIF-B2", "NOTIF-B3");
        when(notificationRepository.save(any(Notification.class))).thenAnswer(i -> i.getArgument(0));

        BroadcastNotificationRequest request = new BroadcastNotificationRequest();
        request.setUserIds(List.of("user-1", "user-2", "user-3"));
        request.setTitle("Course Registration Opens");
        request.setMessage("Course registration is now open.");
        request.setType(NotificationType.ANNOUNCEMENT);
        request.setPriority(NotificationPriority.HIGH);

        List<NotificationResponse> responses = notificationService.broadcastNotification(ADMIN_USER_ID, request);

        assertEquals(3, responses.size());
        verify(notificationRepository, times(3)).save(any(Notification.class));
    }

    // ==========================================
    // 11-12. Invalid Type/Priority (handled by Jackson deserialization, tested at controller level)
    // ==========================================

    // ==========================================
    // 13. Notification Not Found
    // ==========================================

    @Test
    void shouldThrowWhenNotificationNotFound() {
        when(notificationRepository.findByNotificationId("NOTIF-NONEXIST")).thenReturn(Optional.empty());

        assertThrows(NotificationNotFoundException.class, () ->
                notificationService.getNotificationById(USER_ID, "NOTIF-NONEXIST"));
    }

    @Test
    void shouldThrowWhenMarkingNonExistentNotificationAsRead() {
        when(notificationRepository.findByNotificationId("NOTIF-NONEXIST")).thenReturn(Optional.empty());

        assertThrows(NotificationNotFoundException.class, () ->
                notificationService.markAsRead(USER_ID, "NOTIF-NONEXIST"));
    }

    @Test
    void shouldThrowWhenDeletingNonExistentNotification() {
        when(notificationRepository.findByNotificationId("NOTIF-NONEXIST")).thenReturn(Optional.empty());

        assertThrows(NotificationNotFoundException.class, () ->
                notificationService.deleteNotification(USER_ID, "NOTIF-NONEXIST"));
    }

    // ==========================================
    // 14. Pagination Defaults and Limits
    // ==========================================

    @Test
    void shouldClampPageSizeToMax() {
        Page<Notification> emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 100), 0);
        when(notificationRepository.findByUserId(eq(USER_ID), any(Pageable.class))).thenReturn(emptyPage);

        // Request size of 200 should be clamped to maxPageSize (100)
        notificationService.getMyNotifications(USER_ID, 0, 200);

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(notificationRepository).findByUserId(eq(USER_ID), captor.capture());
        assertEquals(100, captor.getValue().getPageSize());
    }

    @Test
    void shouldUseDefaultPageSizeForInvalidSize() {
        Page<Notification> emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 20), 0);
        when(notificationRepository.findByUserId(eq(USER_ID), any(Pageable.class))).thenReturn(emptyPage);

        // Request size of 0 should use default (20)
        notificationService.getMyNotifications(USER_ID, 0, 0);

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(notificationRepository).findByUserId(eq(USER_ID), captor.capture());
        assertEquals(20, captor.getValue().getPageSize());
    }

    // ==========================================
    // 15. Notifications Sorted by createdAt DESC
    // ==========================================

    @Test
    void shouldSortNotificationsByCreatedAtDesc() {
        Page<Notification> emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 20), 0);
        when(notificationRepository.findByUserId(eq(USER_ID), any(Pageable.class))).thenReturn(emptyPage);

        notificationService.getMyNotifications(USER_ID, 0, 20);

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(notificationRepository).findByUserId(eq(USER_ID), captor.capture());
        Sort sort = captor.getValue().getSort();
        Sort.Order order = sort.getOrderFor("createdAt");
        assertNotNull(order);
        assertEquals(Sort.Direction.DESC, order.getDirection());
    }

    // ==========================================
    // 16. Internal Notification
    // ==========================================

    @Test
    void shouldCreateInternalNotification() {
        when(notificationIdGenerator.generateNotificationId()).thenReturn("NOTIF-INT001");
        when(notificationRepository.save(any(Notification.class))).thenAnswer(i -> i.getArgument(0));

        InternalNotificationRequest request = new InternalNotificationRequest(
                USER_ID, "Registration Successful", "You registered for CS301.",
                NotificationType.REGISTRATION, NotificationPriority.NORMAL,
                "REGISTRATION", "REG-123", "registration-service"
        );

        NotificationResponse response = notificationService.createInternalNotification(request);

        assertNotNull(response);
        assertEquals("NOTIF-INT001", response.getNotificationId());
        assertEquals(USER_ID, response.getUserId());
        assertEquals("SYSTEM", response.getCreatedByRole());
        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    // ==========================================
    // Helpers
    // ==========================================

    private Notification createTestNotification(String notificationId, String userId, String title) {
        Notification notification = new Notification(
                notificationId, userId, title, "Test message",
                NotificationType.SYSTEM, NotificationPriority.NORMAL,
                null, null, "system", "SYSTEM"
        );
        return notification;
    }
}

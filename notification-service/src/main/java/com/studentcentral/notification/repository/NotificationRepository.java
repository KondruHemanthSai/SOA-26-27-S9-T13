package com.studentcentral.notification.repository;

import com.studentcentral.notification.model.Notification;
import com.studentcentral.notification.model.NotificationPriority;
import com.studentcentral.notification.model.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationRepository extends MongoRepository<Notification, String> {

    Page<Notification> findByUserId(String userId, Pageable pageable);

    List<Notification> findByUserIdAndIsReadFalse(String userId, Sort sort);

    long countByUserIdAndIsReadFalse(String userId);

    Optional<Notification> findByNotificationId(String notificationId);

    Optional<Notification> findByUserIdAndNotificationId(String userId, String notificationId);

    long deleteByUserIdAndNotificationId(String userId, String notificationId);

    List<Notification> findByUserId(String userId, Sort sort);

    // Admin query methods
    Page<Notification> findByType(NotificationType type, Pageable pageable);

    Page<Notification> findByPriority(NotificationPriority priority, Pageable pageable);

    Page<Notification> findByIsRead(boolean isRead, Pageable pageable);

    Page<Notification> findByUserIdAndType(String userId, NotificationType type, Pageable pageable);

    @Query("{ $and: [ "
            + "{ $or: [ { 'userId': ?0 }, { $expr: { $eq: [?0, null] } } ] }, "
            + "{ $or: [ { 'type': ?1 }, { $expr: { $eq: [?1, null] } } ] }, "
            + "{ $or: [ { 'priority': ?2 }, { $expr: { $eq: [?2, null] } } ] }, "
            + "{ $or: [ { 'isRead': ?3 }, { $expr: { $eq: [?3, null] } } ] } "
            + "] }")
    Page<Notification> findByFilters(String userId, NotificationType type,
                                     NotificationPriority priority, Boolean isRead,
                                     Pageable pageable);
}

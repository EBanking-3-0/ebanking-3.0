package com.ebanking.notification.repository;

import com.ebanking.notification.entity.Notification;
import com.ebanking.notification.enums.NotificationChannel;
import com.ebanking.notification.enums.NotificationStatus;
import com.ebanking.notification.enums.NotificationType;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/** Repository for Notification entity operations. */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

  /**
   * Find notifications by user ID.
   *
   * @param userId User ID
   * @param pageable Pagination information
   * @return Page of notifications
   */
  Page<Notification> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);

  /**
   * Find notifications by user ID and status.
   *
   * @param userId User ID
   * @param status Notification status
   * @param pageable Pagination information
   * @return Page of notifications
   */
  Page<Notification> findByUserIdAndStatusOrderByCreatedAtDesc(
      String userId, NotificationStatus status, Pageable pageable);

  /**
   * Find notifications by user ID and type.
   *
   * @param userId User ID
   * @param type Notification type
   * @param pageable Pagination information
   * @return Page of notifications
   */
  Page<Notification> findByUserIdAndTypeOrderByCreatedAtDesc(
      String userId, NotificationType type, Pageable pageable);

  /**
   * Find unread in-app notifications for a user.
   *
   * @param userId User ID
   * @param channel Notification channel
   * @return List of unread notifications
   */
  @Query(
      "SELECT n FROM Notification n WHERE n.userId = :userId AND n.channel = :channel "
          + "AND n.status NOT IN ('READ', 'FAILED') ORDER BY n.createdAt DESC")
  List<Notification> findUnreadInAppNotifications(
      @Param("userId") String userId, @Param("channel") NotificationChannel channel);

  /**
   * Find failed notifications that need retry.
   *
   * @param maxRetries Maximum retry count
   * @param beforeDate Only get notifications created before this date
   * @return List of failed notifications
   */
  @Query(
      "SELECT n FROM Notification n WHERE n.status IN ('FAILED', 'RETRYING') "
          + "AND n.retryCount < :maxRetries AND n.createdAt >= :beforeDate")
  List<Notification> findFailedNotificationsForRetry(
      @Param("maxRetries") int maxRetries, @Param("beforeDate") LocalDateTime beforeDate);

  /**
   * Find scheduled notifications that are ready to be sent.
   *
   * @param now Current timestamp
   * @return List of scheduled notifications
   */
  @Query(
      "SELECT n FROM Notification n WHERE n.status = 'PENDING' "
          + "AND n.scheduledFor IS NOT NULL AND n.scheduledFor <= :now")
  List<Notification> findScheduledNotificationsReadyToSend(@Param("now") LocalDateTime now);

  /**
   * Count unread in-app notifications for a user.
   *
   * @param userId User ID
   * @param channel Notification channel
   * @return Count of unread notifications
   */
  @Query(
      "SELECT COUNT(n) FROM Notification n WHERE n.userId = :userId AND n.channel = :channel "
          + "AND n.status NOT IN ('READ', 'FAILED')")
  long countUnreadInAppNotifications(
      @Param("userId") String userId, @Param("channel") NotificationChannel channel);

  /**
   * Find notifications by event ID.
   *
   * @param eventId Event ID
   * @return List of notifications
   */
  List<Notification> findByEventId(String eventId);

  /**
   * Find notifications by reference ID.
   *
   * @param referenceId Reference ID
   * @return List of notifications
   */
  List<Notification> findByReferenceId(String referenceId);

  /**
   * Delete old notifications.
   *
   * @param beforeDate Delete notifications created before this date
   * @return Number of deleted notifications
   */
  @Query("DELETE FROM Notification n WHERE n.createdAt < :beforeDate AND n.status = 'SENT'")
  int deleteOldNotifications(@Param("beforeDate") LocalDateTime beforeDate);
}

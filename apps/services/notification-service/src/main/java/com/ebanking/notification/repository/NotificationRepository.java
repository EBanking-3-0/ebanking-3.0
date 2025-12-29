package com.ebanking.notification.repository;

import com.ebanking.notification.entity.Notification;
import com.ebanking.notification.entity.Notification.NotificationStatus;
import com.ebanking.notification.entity.Notification.NotificationType;
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

  /** Find all notifications for a specific user */
  Page<Notification> findByUserId(Long userId, Pageable pageable);

  /** Find notifications by user and status */
  List<Notification> findByUserIdAndStatus(Long userId, NotificationStatus status);

  /** Find notifications by status */
  List<Notification> findByStatus(NotificationStatus status);

  /** Find pending or retrying notifications for retry processing */
  @Query(
      "SELECT n FROM Notification n WHERE n.status IN ('PENDING', 'RETRYING') AND n.retryCount < :maxRetries")
  List<Notification> findNotificationsForRetry(@Param("maxRetries") int maxRetries);

  /** Find failed notifications that are eligible for retry */
  @Query(
      "SELECT n FROM Notification n WHERE n.status IN ('FAILED', 'RETRYING') AND n.retryCount < :maxRetries ORDER BY n.updatedAt ASC")
  List<Notification> findFailedNotificationsForRetry(@Param("maxRetries") int maxRetries);

  /** Find notifications by type and date range */
  @Query(
      "SELECT n FROM Notification n WHERE n.notificationType = :type AND n.createdAt BETWEEN :startDate AND :endDate")
  List<Notification> findByTypeAndDateRange(
      @Param("type") NotificationType type,
      @Param("startDate") LocalDateTime startDate,
      @Param("endDate") LocalDateTime endDate);

  /** Count notifications by status */
  long countByStatus(NotificationStatus status);

  /** Count notifications by user and status */
  long countByUserIdAndStatus(Long userId, NotificationStatus status);

  /** Find notifications by user ordered by creation date descending */
  List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);
}

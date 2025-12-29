package com.ebanking.notification.repository;

import com.ebanking.notification.entity.Notification;
import com.ebanking.notification.entity.NotificationPreference;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Repository for NotificationPreference entity operations. */
@Repository
public interface NotificationPreferenceRepository
    extends JpaRepository<NotificationPreference, Long> {

  /** Find all preferences for a user */
  List<NotificationPreference> findByUserId(Long userId);

  /** Find specific preference for a user and notification type */
  Optional<NotificationPreference> findByUserIdAndNotificationType(
      Long userId, Notification.NotificationType notificationType);

  /** Delete all preferences for a user */
  void deleteByUserId(Long userId);
}

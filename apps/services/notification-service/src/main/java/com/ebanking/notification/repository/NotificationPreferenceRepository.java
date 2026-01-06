package com.ebanking.notification.repository;

import com.ebanking.notification.entity.NotificationPreference;
import com.ebanking.notification.enums.NotificationChannel;
import com.ebanking.notification.enums.NotificationType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/** Repository for NotificationPreference entity operations. */
@Repository
public interface NotificationPreferenceRepository
    extends JpaRepository<NotificationPreference, Long> {

  /**
   * Find all preferences for a user.
   *
   * @param userId User ID
   * @return List of preferences
   */
  List<NotificationPreference> findByUserId(Long userId);

  /**
   * Find preferences for a user and notification type.
   *
   * @param userId User ID
   * @param notificationType Notification type
   * @return List of preferences
   */
  List<NotificationPreference> findByUserIdAndNotificationType(
      Long userId, NotificationType notificationType);

  /**
   * Find a specific preference.
   *
   * @param userId User ID
   * @param notificationType Notification type
   * @param channel Notification channel
   * @return Optional preference
   */
  Optional<NotificationPreference> findByUserIdAndNotificationTypeAndChannel(
      Long userId, NotificationType notificationType, NotificationChannel channel);

  /**
   * Find enabled channels for a user and notification type.
   *
   * @param userId User ID
   * @param notificationType Notification type
   * @return List of enabled channels
   */
  @Query(
      "SELECT np.channel FROM NotificationPreference np WHERE np.userId = :userId "
          + "AND np.notificationType = :notificationType AND np.enabled = true")
  List<NotificationChannel> findEnabledChannels(
      @Param("userId") Long userId, @Param("notificationType") NotificationType notificationType);

  /**
   * Check if a channel is enabled for a user and notification type.
   *
   * @param userId User ID
   * @param notificationType Notification type
   * @param channel Notification channel
   * @return True if enabled
   */
  @Query(
      "SELECT CASE WHEN COUNT(np) > 0 THEN true ELSE false END FROM NotificationPreference np "
          + "WHERE np.userId = :userId AND np.notificationType = :notificationType "
          + "AND np.channel = :channel AND np.enabled = true")
  boolean isChannelEnabled(
      @Param("userId") Long userId,
      @Param("notificationType") NotificationType notificationType,
      @Param("channel") NotificationChannel channel);

  /**
   * Delete all preferences for a user.
   *
   * @param userId User ID
   */
  void deleteByUserId(Long userId);
}

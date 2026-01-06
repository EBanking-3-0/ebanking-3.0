package com.ebanking.notification.repository;

import com.ebanking.notification.entity.NotificationTemplate;
import com.ebanking.notification.enums.NotificationChannel;
import com.ebanking.notification.enums.NotificationType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/** Repository for NotificationTemplate entity operations. */
@Repository
public interface NotificationTemplateRepository extends JpaRepository<NotificationTemplate, Long> {

  /**
   * Find template by type, channel, and locale.
   *
   * @param notificationType Notification type
   * @param channel Notification channel
   * @param locale Locale
   * @return Optional template
   */
  Optional<NotificationTemplate> findByNotificationTypeAndChannelAndLocaleAndActiveTrue(
      NotificationType notificationType, NotificationChannel channel, String locale);

  /**
   * Find template by type and channel (default locale).
   *
   * @param notificationType Notification type
   * @param channel Notification channel
   * @return Optional template
   */
  @Query(
      "SELECT nt FROM NotificationTemplate nt WHERE nt.notificationType = :notificationType "
          + "AND nt.channel = :channel AND nt.locale = 'en' AND nt.active = true")
  Optional<NotificationTemplate> findDefaultTemplate(
      @Param("notificationType") NotificationType notificationType,
      @Param("channel") NotificationChannel channel);

  /**
   * Find all templates for a notification type.
   *
   * @param notificationType Notification type
   * @return List of templates
   */
  List<NotificationTemplate> findByNotificationTypeAndActiveTrue(NotificationType notificationType);

  /**
   * Find all templates for a channel.
   *
   * @param channel Notification channel
   * @return List of templates
   */
  List<NotificationTemplate> findByChannelAndActiveTrue(NotificationChannel channel);

  /**
   * Find template by name.
   *
   * @param name Template name
   * @return Optional template
   */
  Optional<NotificationTemplate> findByNameAndActiveTrue(String name);

  /**
   * Check if a template exists.
   *
   * @param notificationType Notification type
   * @param channel Notification channel
   * @param locale Locale
   * @return True if exists
   */
  boolean existsByNotificationTypeAndChannelAndLocaleAndActiveTrue(
      NotificationType notificationType, NotificationChannel channel, String locale);
}

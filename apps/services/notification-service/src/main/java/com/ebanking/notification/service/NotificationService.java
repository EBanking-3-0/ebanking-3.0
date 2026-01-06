package com.ebanking.notification.service;

import com.ebanking.notification.dto.NotificationDTO;
import com.ebanking.notification.dto.SendNotificationRequest;
import com.ebanking.notification.dto.UserContactDTO;
import com.ebanking.notification.entity.Notification;
import com.ebanking.notification.enums.NotificationChannel;
import com.ebanking.notification.enums.NotificationStatus;
import com.ebanking.notification.enums.NotificationType;
import com.ebanking.notification.exception.NotificationException;
import com.ebanking.notification.exception.NotificationSendException;
import com.ebanking.notification.mapper.NotificationMapper;
import com.ebanking.notification.repository.NotificationRepository;
import com.ebanking.notification.strategy.NotificationStrategy;
import com.ebanking.shared.kafka.KafkaTopics;
import com.ebanking.shared.kafka.events.NotificationFailedEvent;
import com.ebanking.shared.kafka.events.NotificationSentEvent;
import com.ebanking.shared.kafka.producer.EventProducer;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Core notification service that orchestrates the notification sending process. Handles
 * notification creation, channel selection, template rendering, and delivery through appropriate
 * strategies.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

  private final NotificationRepository notificationRepository;
  private final NotificationStrategyFactory strategyFactory;
  private final TemplateService templateService;
  private final PreferenceService preferenceService;
  private final UserService userService;
  private final EventProducer eventProducer;
  private final NotificationMapper notificationMapper;

  /**
   * Send a notification based on request.
   *
   * @param request Send notification request
   * @return Notification DTO
   */
  @Transactional
  public NotificationDTO sendNotification(SendNotificationRequest request) {
    log.info(
        "Processing notification request for user: {}, type: {}, channel: {}",
        request.getUserId(),
        request.getType(),
        request.getChannel());

    try {
      // Validate channel is enabled for user
      if (!preferenceService.isChannelEnabled(
          request.getUserId(), request.getType(), request.getChannel())) {
        log.warn(
            "Channel {} is disabled for user: {} and type: {}",
            request.getChannel(),
            request.getUserId(),
            request.getType());
        throw new NotificationException("Notification channel is disabled for this user");
      }

      // Get user contact information
      UserContactDTO userContact = userService.getUserContact(request.getUserId());

      // Determine recipient
      String recipient = determineRecipient(request, userContact);

      // Render template or use provided content
      String content = renderContent(request, userContact);
      String subject =
          (request.getSubject() != null)
              ? request.getSubject()
              : templateService.getSubject(
                  request.getType(), request.getChannel(), request.getTemplateVariables());

      // Create notification entity
      Notification notification = createNotification(request, recipient, subject, content);

      // Save notification
      notification = notificationRepository.save(notification);

      // Send through appropriate channel
      sendThroughChannel(notification, recipient, subject, content);

      // Publish success event
      publishNotificationSentEvent(notification);

      return toDTO(notification);

    } catch (NotificationSendException e) {
      log.error("Failed to send notification", e);
      throw e;
    } catch (Exception e) {
      log.error("Unexpected error processing notification", e);
      throw new NotificationException("Failed to process notification: " + e.getMessage(), e);
    }
  }

  /**
   * Send notification to all enabled channels for a user.
   *
   * @param userId User ID
   * @param type Notification type
   * @param variables Template variables
   * @return List of sent notifications
   */
  @Transactional
  public List<NotificationDTO> sendToAllChannels(
      Long userId, NotificationType type, Map<String, Object> variables) {

    log.info("Sending notification to all enabled channels for user: {}, type: {}", userId, type);

    List<NotificationChannel> enabledChannels = preferenceService.getEnabledChannels(userId, type);

    return enabledChannels.stream()
        .map(
            channel -> {
              SendNotificationRequest request =
                  SendNotificationRequest.builder()
                      .userId(userId)
                      .type(type)
                      .channel(channel)
                      .subject(templateService.getSubject(type, channel, variables))
                      .content("") // Will be rendered from template
                      .templateVariables(variables)
                      .build();

              try {
                return sendNotification(request);
              } catch (Exception e) {
                log.error("Failed to send notification via channel: {}", channel, e);
                return null;
              }
            })
        .filter(java.util.Objects::nonNull)
        .toList();
  }

  /**
   * Get notifications for a user.
   *
   * @param userId User ID
   * @param pageable Pagination
   * @return Page of notifications
   */
  public Page<NotificationDTO> getUserNotifications(Long userId, Pageable pageable) {
    return notificationRepository
        .findByUserIdOrderByCreatedAtDesc(userId, pageable)
        .map(this::toDTO);
  }

  /**
   * Get unread in-app notifications for a user.
   *
   * @param userId User ID
   * @return List of unread notifications
   */
  public List<NotificationDTO> getUnreadInAppNotifications(Long userId) {
    return notificationRepository
        .findUnreadInAppNotifications(userId, NotificationChannel.IN_APP)
        .stream()
        .map(this::toDTO)
        .toList();
  }

  /**
   * Get unread notification count.
   *
   * @param userId User ID
   * @return Count of unread notifications
   */
  public long getUnreadCount(Long userId) {
    return notificationRepository.countUnreadInAppNotifications(userId, NotificationChannel.IN_APP);
  }

  /**
   * Mark notification as read.
   *
   * @param notificationId Notification ID
   * @param userId User ID (for authorization)
   * @return Updated notification DTO
   */
  @Transactional
  public NotificationDTO markAsRead(Long notificationId, Long userId) {
    Notification notification =
        notificationRepository
            .findById(notificationId)
            .orElseThrow(
                () -> new NotificationException("Notification not found: " + notificationId));

    // Verify notification belongs to user
    if (!notification.getUserId().equals(userId)) {
      throw new NotificationException("Notification does not belong to user");
    }

    notification.markAsRead();
    notification = notificationRepository.save(notification);

    log.info("Marked notification {} as read for user {}", notificationId, userId);

    return toDTO(notification);
  }

  /**
   * Mark all notifications as read for a user.
   *
   * @param userId User ID
   */
  @Transactional
  public void markAllAsRead(Long userId) {
    List<Notification> unread =
        notificationRepository.findUnreadInAppNotifications(userId, NotificationChannel.IN_APP);

    unread.forEach(Notification::markAsRead);
    notificationRepository.saveAll(unread);

    log.info("Marked {} notifications as read for user {}", unread.size(), userId);
  }

  /**
   * Retry a failed notification.
   *
   * @param notificationId Notification ID
   * @return Updated notification DTO
   */
  @Transactional
  public NotificationDTO retryNotification(Long notificationId) {
    Notification notification =
        notificationRepository
            .findById(notificationId)
            .orElseThrow(
                () -> new NotificationException("Notification not found: " + notificationId));

    if (notification.getStatus() != NotificationStatus.FAILED) {
      throw new NotificationException("Only failed notifications can be retried");
    }

    log.info("Retrying notification: {}", notificationId);

    try {
      notification.incrementRetryCount();
      notificationRepository.save(notification);

      sendThroughChannel(
          notification,
          notification.getRecipient(),
          notification.getSubject(),
          notification.getContent());

      publishNotificationSentEvent(notification);

    } catch (Exception e) {
      notification.markAsFailed(e.getMessage());
      notificationRepository.save(notification);
      publishNotificationFailedEvent(notification, e.getMessage());
      throw e;
    }

    return toDTO(notification);
  }

  /**
   * Get failed notifications for retry.
   *
   * @param maxRetries Maximum retry count
   * @param hoursBack Only get notifications from last N hours
   * @return List of failed notifications
   */
  public List<Notification> getFailedNotificationsForRetry(int maxRetries, int hoursBack) {
    LocalDateTime beforeDate = LocalDateTime.now().minusHours(hoursBack);
    return notificationRepository.findFailedNotificationsForRetry(maxRetries, beforeDate);
  }

  /**
   * Delete old notifications.
   *
   * @param daysOld Delete notifications older than this many days
   * @return Number of deleted notifications
   */
  @Transactional
  public int deleteOldNotifications(int daysOld) {
    LocalDateTime beforeDate = LocalDateTime.now().minusDays(daysOld);
    int deleted = notificationRepository.deleteOldNotifications(beforeDate);
    log.info("Deleted {} old notifications (older than {} days)", deleted, daysOld);
    return deleted;
  }

  /**
   * Determine recipient address based on channel.
   *
   * @param request Notification request
   * @param userContact User contact information
   * @return Recipient address
   */
  private String determineRecipient(SendNotificationRequest request, UserContactDTO userContact) {
    // Use provided recipient if available
    if (request.getRecipient() != null && !request.getRecipient().isEmpty()) {
      return request.getRecipient();
    }

    // Determine based on channel
    return switch (request.getChannel()) {
      case EMAIL -> userContact.getEmail();
      case SMS -> userContact.getPhoneNumber();
      case IN_APP -> String.valueOf(userContact.getUserId());
      case PUSH -> null; // Push requires device token - will be retrieved from UserService when
        // implemented
    };
  }

  /**
   * Render notification content.
   *
   * @param request Notification request
   * @param userContact User contact information
   * @return Rendered content
   */
  private String renderContent(SendNotificationRequest request, UserContactDTO userContact) {
    // If content is provided directly, use it
    if (request.getContent() != null && !request.getContent().isEmpty()) {
      return request.getContent();
    }

    // Add user info to template variables - create mutable copy to avoid
    // UnsupportedOperationException
    Map<String, Object> variables =
        new HashMap<>(
            (request.getTemplateVariables() != null) ? request.getTemplateVariables() : Map.of());

    variables.put("firstName", userContact.getFirstName());
    variables.put("lastName", userContact.getLastName());
    variables.put("email", userContact.getEmail());

    // Render from template
    return templateService.renderTemplate(
        request.getType(), request.getChannel(), variables, userContact.getPreferredLanguage());
  }

  /**
   * Create notification entity.
   *
   * @param request Notification request
   * @param recipient Recipient address
   * @param subject Subject
   * @param content Content
   * @return Notification entity
   */
  private Notification createNotification(
      SendNotificationRequest request, String recipient, String subject, String content) {

    return Notification.builder()
        .userId(request.getUserId())
        .type(request.getType())
        .channel(request.getChannel())
        .priority(request.getPriority())
        .subject(subject)
        .content(content)
        .recipient(recipient)
        .eventId(request.getEventId())
        .source(request.getSource())
        .referenceId(request.getReferenceId())
        .scheduledFor(request.getScheduledFor())
        .status(NotificationStatus.PENDING)
        .build();
  }

  /**
   * Send notification through appropriate channel strategy.
   *
   * @param notification Notification entity
   * @param recipient Recipient address
   * @param subject Subject
   * @param content Content
   */
  private void sendThroughChannel(
      Notification notification, String recipient, String subject, String content) {

    try {
      NotificationStrategy strategy = strategyFactory.getStrategy(notification.getChannel());

      // Validate recipient
      if (!strategy.supports(recipient)) {
        throw new NotificationSendException(
            "Invalid recipient format for channel: " + notification.getChannel());
      }

      // Update status to sending
      notification.setStatus(NotificationStatus.SENDING);
      notificationRepository.save(notification);

      // Send through strategy
      strategy.send(notification, recipient, subject, content);

      // Mark as sent
      notification.markAsSent();
      notificationRepository.save(notification);

      log.info(
          "Notification sent successfully via {}: {} (ID: {})",
          notification.getChannel(),
          recipient,
          notification.getId());

    } catch (Exception e) {
      log.error("Failed to send notification via {}", notification.getChannel(), e);

      notification.markAsFailed(e.getMessage());
      notificationRepository.save(notification);

      publishNotificationFailedEvent(notification, e.getMessage());

      throw new NotificationSendException("Failed to send notification: " + e.getMessage(), e);
    }
  }

  /**
   * Publish notification sent event.
   *
   * @param notification Notification entity
   */
  private void publishNotificationSentEvent(Notification notification) {
    try {
      NotificationSentEvent event =
          NotificationSentEvent.builder()
              .notificationId(notification.getId())
              .userId(notification.getUserId())
              .recipient(notification.getRecipient())
              .notificationType(notification.getType().toString())
              .subject(notification.getSubject())
              .status(notification.getStatus().toString())
              .channel(notification.getChannel().toString())
              .timestamp(java.time.Instant.now())
              .source("notification-service")
              .build();

      eventProducer.publishEvent(KafkaTopics.NOTIFICATION_SENT, event);

    } catch (Exception e) {
      log.error("Failed to publish notification sent event", e);
      // Don't fail the notification if event publishing fails
    }
  }

  /**
   * Publish notification failed event.
   *
   * @param notification Notification entity
   * @param errorMessage Error message
   */
  private void publishNotificationFailedEvent(Notification notification, String errorMessage) {
    try {
      NotificationFailedEvent event =
          NotificationFailedEvent.builder()
              .userId(notification.getUserId())
              .recipient(notification.getRecipient())
              .notificationType(notification.getType().toString())
              .channel(notification.getChannel().toString())
              .errorMessage(errorMessage)
              .retryCount(notification.getRetryCount())
              .timestamp(java.time.Instant.now())
              .source("notification-service")
              .build();

      eventProducer.publishEvent(KafkaTopics.NOTIFICATION_FAILED, event);

    } catch (Exception e) {
      log.error("Failed to publish notification failed event", e);
      // Don't fail if event publishing fails
    }
  }

  /**
   * Convert entity to DTO using MapStruct mapper.
   *
   * @param notification Notification entity
   * @return Notification DTO
   */
  private NotificationDTO toDTO(Notification notification) {
    return notificationMapper.toDTO(notification);
  }
}

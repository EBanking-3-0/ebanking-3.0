package com.ebanking.notification.service;

import com.ebanking.notification.config.TemplateConfig;
import com.ebanking.notification.dto.SendNotificationRequest;
import com.ebanking.notification.entity.Notification;
import com.ebanking.notification.entity.NotificationPreference;
import com.ebanking.notification.repository.NotificationPreferenceRepository;
import com.ebanking.notification.repository.NotificationRepository;
import com.ebanking.shared.kafka.events.NotificationFailedEvent;
import com.ebanking.shared.kafka.events.NotificationSentEvent;
import com.ebanking.shared.kafka.producer.TypedEventProducer;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Main orchestration service for sending notifications. Handles persistence,
 * preference checking,
 * and delegation to channel-specific services.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

  private final NotificationRepository notificationRepository;
  private final NotificationPreferenceRepository preferenceRepository;
  private final EmailService emailService;
  private final SmsService smsService;
  private final TemplateService templateService;
  private final TypedEventProducer eventProducer;
  private final TemplateConfig templateConfig;

  /** Send a notification based on the request */
  public Notification sendNotification(SendNotificationRequest request) {
    log.info(
        "Sending notification - Type: {}, Channel: {}, Recipient: {}",
        request.getNotificationType(),
        request.getChannel(),
        request.getRecipient());

    Notification notification = createNotification(request);
    // Check user preferences
    if (!isNotificationAllowed(
        request.getUserId(), request.getNotificationType(), request.getChannel())) {
      log.info(
          "Notification blocked by user preference - User: {}, Type: {}, Channel: {}",
          request.getUserId(),
          request.getNotificationType(),
          request.getChannel());

      notification.setStatus(Notification.NotificationStatus.CANCELLED);
      notification.setErrorMessage("Blocked by user preference");
      return notificationRepository.save(notification);
    }

    // Create notification record
    notification.setStatus(Notification.NotificationStatus.PENDING);
    notification = notificationRepository.save(notification);

    try {
      // Prepare content
      String content = prepareContent(request);
      notification.setContent(content);

      // Send via appropriate channel
      switch (request.getChannel()) {
        case EMAIL:
          sendEmailNotification(request.getRecipient(), request.getSubject(), content);
          break;
        case SMS:
          sendSmsNotification(request.getRecipient(), content);
          break;
        case PUSH:
          // Push notification not implemented yet
          log.warn("Push notifications not implemented yet");
          throw new UnsupportedOperationException("Push notifications not supported yet");
        case IN_APP:
          // In-app notifcation not implemented yet
          log.warn("In-app notifications not implemented yet");
          throw new UnsupportedOperationException("In-app notifications not supported yet");
        default:
          throw new IllegalArgumentException("Unsupported channel: " + request.getChannel());
      }

      // Update notification status
      notification.setStatus(Notification.NotificationStatus.SENT);
      notification.setSentAt(LocalDateTime.now());
      notification = notificationRepository.save(notification);

      // Publish success event
      publishNotificationSentEvent(notification);

      log.info("Notification sent successfully - ID: {}", notification.getId());
      return notification;

    } catch (Exception e) {
      log.error("Failed to send notification - ID: {}", notification.getId(), e);

      // Update notification with error
      notification.setStatus(Notification.NotificationStatus.FAILED);
      // Truncate error message to prevent database column overflow (max 1000 chars)
      String errorMsg = e.getMessage();
      notification.setErrorMessage(
          errorMsg != null && errorMsg.length() > 1000
              ? errorMsg.substring(0, 997) + "..."
              : errorMsg);

      notification = notificationRepository.save(notification);

      // Publish failure event
      publishNotificationFailedEvent(notification, e);

      throw new RuntimeException("Failed to send notification", e);
    }
  }

  /** Send email notification using template */
  public void sendTemplatedEmail(
      Long userId, String email, String templateCode, Map<String, Object> data) {
    sendTemplatedEmail(userId, email, templateCode, null, data);
  }

  /** Send email notification using template with custom subject */
  @Transactional
  public void sendTemplatedEmail(
      Long userId, String email, String templateCode, String subject, Map<String, Object> data) {
    SendNotificationRequest request = SendNotificationRequest.builder()
        .userId(userId)
        .recipient(email)
        .channel(Notification.NotificationChannel.EMAIL)
        .templateCode(templateCode)
        .subject(subject)
        .templateData(data)
        .build();

    // Determine notification type from template code
    request.setNotificationType(getNotificationTypeFromTemplate(templateCode));

    sendNotification(request);
  }

  /** Send simple email notification */
  @Transactional
  public void sendSimpleEmail(
      Long userId,
      String email,
      String subject,
      String content,
      Notification.NotificationType type) {
    SendNotificationRequest request = SendNotificationRequest.builder()
        .userId(userId)
        .recipient(email)
        .notificationType(type)
        .channel(Notification.NotificationChannel.EMAIL)
        .subject(subject)
        .content(content)
        .build();

    sendNotification(request);
  }

  /** Send SMS notification */
  @Transactional
  public void sendSms(
      Long userId, String phoneNumber, String message, Notification.NotificationType type) {
    SendNotificationRequest request = SendNotificationRequest.builder()
        .userId(userId)
        .recipient(phoneNumber)
        .notificationType(type)
        .channel(Notification.NotificationChannel.SMS)
        .content(message)
        .build();

    sendNotification(request);
  }

  /** Get notification history for a user */
  public List<Notification> getNotificationHistory(Long userId) {
    return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
  }

  /**
   * Check if notification is allowed based on user preferences.
   * Checks global channel preferences (not per-type).
   */
  private boolean isNotificationAllowed(
      Long userId, Notification.NotificationType type, Notification.NotificationChannel channel) {
    Optional<NotificationPreference> preference = preferenceRepository.findByUserId(userId);

    if (preference.isEmpty()) {
      // If no preference set, allow by default
      return true;
    }

    NotificationPreference pref = preference.get();
    return switch (channel) {
      case EMAIL -> pref.getEmailEnabled();
      case SMS -> pref.getSmsEnabled();
      case PUSH -> pref.getPushEnabled();
      case IN_APP -> pref.getInAppEnabled();
    };
  }

  /** Prepare notification content from template or direct content */
  private String prepareContent(SendNotificationRequest request) {
    if (request.getTemplateCode() != null) {
      // Use template
      return renderTemplate(request.getTemplateCode(), request.getTemplateData());
    } else {
      // Use direct content
      return request.getContent();
    }
  }

  /**
   * Render template with data.
   * Uses file-based Thymeleaf templates from resources/templates/notifications/
   */
  private String renderTemplate(String templateCode, Map<String, Object> data) {
    log.debug("Rendering template: {}", templateCode);
    return templateService.renderTemplate(templateCode, data);
  }

  /** Send email via email service */
  private void sendEmailNotification(String to, String subject, String content) {
    emailService.sendHtmlEmail(to, subject, content);
  }

  /** Send SMS via SMS service */
  private void sendSmsNotification(String to, String content) {
    smsService.sendSms(to, content);
  }

  /** Create notification entity from request */
  private Notification createNotification(SendNotificationRequest request) {
    return Notification.builder()
        .userId(request.getUserId())
        .recipient(request.getRecipient())
        .notificationType(request.getNotificationType())
        .channel(request.getChannel())
        .subject(request.getSubject())
        .content(request.getContent())
        .eventId(request.getEventId())
        .retryCount(0)
        .build();
  }

  /** Publish notification sent event to Kafka */
  private void publishNotificationSentEvent(Notification notification) {
    try {
      NotificationSentEvent event = NotificationSentEvent.builder()
          .userId(notification.getUserId())
          .recipient(notification.getRecipient())
          .notificationType(notification.getNotificationType().name())
          .subject(notification.getSubject())
          .status(Notification.NotificationStatus.SENT.name())
          .channel(notification.getChannel().name())
          .source("notification-service")
          .build();

      eventProducer.publishNotificationSent(event);
    } catch (Exception e) {
      log.error("Failed to publish notification sent event", e);
    }
  }

  /** Publish notification failed event to Kafka */
  private void publishNotificationFailedEvent(Notification notification, Exception error) {
    try {
      NotificationFailedEvent event = NotificationFailedEvent.builder()
          .userId(notification.getUserId())
          .recipient(notification.getRecipient())
          .notificationType(notification.getNotificationType().name())
          .channel(notification.getChannel().name())
          .errorMessage(error.getMessage())
          .retryCount(notification.getRetryCount())
          .source("notification-service")
          .build();

      eventProducer.publishNotificationFailed(event);
    } catch (Exception e) {
      log.error("Failed to publish notification failed event", e);
    }
  }

  /**
   * Retry failed notifications with exponential backoff based on configuration
   */
  public void retryFailedNotifications() {
    if (!templateConfig.isRetryEnabled()) {
      log.debug("Retry is disabled in configuration");
      return;
    }

    log.info("Starting retry process for failed notifications");

    // Find notifications that need retry
    List<Notification> failedNotifications = notificationRepository
        .findFailedNotificationsForRetry(templateConfig.getMaxRetries());

    for (Notification notification : failedNotifications) {
      try {
        // Calculate exponential backoff delay: delay * 2^retryCount
        long delay = (long) (templateConfig.getRetryDelayMillis() * Math.pow(2, notification.getRetryCount()));

        if (notification.getUpdatedAt().plusSeconds(delay / 1000).isAfter(LocalDateTime.now())) {
          log.debug("Notification {} not ready for retry yet", notification.getId());
          continue;
        }

        log.info(
            "Retrying notification - ID: {}, Retry count: {}",
            notification.getId(),
            notification.getRetryCount() + 1);

        // Create retry request
        SendNotificationRequest retryRequest = SendNotificationRequest.builder()
            .userId(notification.getUserId())
            .recipient(notification.getRecipient())
            .notificationType(notification.getNotificationType())
            .channel(notification.getChannel())
            .subject(notification.getSubject())
            .content(notification.getContent())
            .eventId(notification.getEventId())
            .build();

        resendNotification(retryRequest, notification);

      } catch (Exception e) {
        log.error("Retry failed for notification ID: {}", notification.getId(), e);
      }
    }
  }

  /** Resend a notification based on a retry request */
  public Notification resendNotification(SendNotificationRequest request, Notification notification) {
    log.info(
        "Resending notification - Type: {}, Channel: {}, Recipient: {}",
        request.getNotificationType(),
        request.getChannel(),
        request.getRecipient());

    // Check user preferences
    if (!isNotificationAllowed(
        request.getUserId(), request.getNotificationType(), request.getChannel())) {
      log.info(
          "Notification blocked by user preference - User: {}, Type: {}, Channel: {}",
          request.getUserId(),
          request.getNotificationType(),
          request.getChannel());

      notification.setStatus(Notification.NotificationStatus.CANCELLED);
      notification.setErrorMessage("Blocked by user preference");
      return notificationRepository.save(notification);
    }

    // Create notification record
    notification.setStatus(Notification.NotificationStatus.PENDING);
    notification = notificationRepository.save(notification);

    try {
      // Send via appropriate channel
      switch (request.getChannel()) {
        case EMAIL:
          sendEmailNotification(request.getRecipient(), request.getSubject(), notification.getContent());
          break;
        case SMS:
          sendSmsNotification(request.getRecipient(), notification.getContent());
          break;
        case PUSH:
          // Push notification not implemented yet
          log.warn("Push notifications not implemented yet");
          throw new UnsupportedOperationException("Push notifications not supported yet");
        case IN_APP:
          // In-app notifcation not implemented yet
          log.warn("In-app notifications not implemented yet");
          throw new UnsupportedOperationException("In-app notifications not supported yet");
        default:
          throw new IllegalArgumentException("Unsupported channel: " + request.getChannel());
      }

      // Update notification status
      notification.setStatus(Notification.NotificationStatus.SENT);
      notification.setSentAt(LocalDateTime.now());
      notification.setRetryCount(notification.getRetryCount() + 1);
      notification = notificationRepository.save(notification);

      // Publish success event
      publishNotificationSentEvent(notification);

      log.info("Notification sent successfully - ID: {}", notification.getId());
      return notification;

    } catch (Exception e) {
      log.error("Failed to send notification - ID: {}", notification.getId(), e);

      // Update notification with error
      notification.setStatus(Notification.NotificationStatus.FAILED);
      // Truncate error message to prevent database column overflow (max 1000 chars)
      String errorMsg = e.getMessage();
      notification.setErrorMessage(
          errorMsg != null && errorMsg.length() > 1000
              ? errorMsg.substring(0, 997) + "..."
              : errorMsg);

      notification = notificationRepository.save(notification);

      // Publish failure event
      publishNotificationFailedEvent(notification, e);

      throw new RuntimeException("Failed to send notification", e);
    }
  }

  /** Determine notification type from template code */
  private Notification.NotificationType getNotificationTypeFromTemplate(String templateCode) {
    if (templateCode == null) {
      return Notification.NotificationType.GENERIC;
    }

    String code = templateCode.toLowerCase();
    if (code.contains("welcome"))
      return Notification.NotificationType.WELCOME;
    if (code.contains("transaction"))
      return Notification.NotificationType.TRANSACTION;
    if (code.contains("fraud"))
      return Notification.NotificationType.FRAUD_ALERT;
    if (code.contains("payment"))
      return Notification.NotificationType.PAYMENT_FAILED;
    if (code.contains("crypto"))
      return Notification.NotificationType.CRYPTO_TRADE;
    if (code.contains("alert"))
      return Notification.NotificationType.ALERT;
    if (code.contains("mfa"))
      return Notification.NotificationType.MFA;
    if (code.contains("password"))
      return Notification.NotificationType.PASSWORD_RESET;

    return Notification.NotificationType.GENERIC;
  }
}

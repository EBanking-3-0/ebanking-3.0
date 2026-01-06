package com.ebanking.notification.strategy;

import com.ebanking.notification.entity.Notification;
import com.ebanking.notification.exception.NotificationSendException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Strategy for sending push notifications. This is a placeholder implementation for future push
 * notification integration (e.g., Firebase Cloud Messaging, Apple Push Notification Service).
 *
 * <p>To implement: - Integrate with FCM/APNS - Store device tokens - Handle platform-specific
 * payloads - Manage notification badges and sounds
 */
@Slf4j
@Component
@ConditionalOnProperty(
    prefix = "notification.push",
    name = "enabled",
    havingValue = "true",
    matchIfMissing = false)
public class PushNotificationStrategy implements NotificationStrategy {

  @Override
  public void send(Notification notification, String recipient, String subject, String content)
      throws NotificationSendException {

    log.info("Push notification strategy called for user: {}", notification.getUserId());

    // IMPLEMENTATION NOTE: Push notification logic will be added when FCM/APNS is
    // integrated
    // This placeholder ensures the service architecture supports push notifications
    // Future implementation should:
    // - Send to FCM/APNS with notification payload
    // - Handle platform-specific formatting
    // - Manage delivery receipts and retries

    log.warn(
        "Push notification not implemented yet. Notification ID: {}, User: {}",
        notification.getId(),
        notification.getUserId());

    throw new NotificationSendException(
        "Push notification service not yet implemented. Please configure FCM/APNS integration.");
  }

  @Override
  public boolean supports(String recipient) {
    // IMPLEMENTATION NOTE: Device token validation will be added with FCM/APNS
    // integration
    // For now, accept any non-empty string to allow architecture testing
    return recipient != null && !recipient.isEmpty();
  }

  @Override
  public String getChannelName() {
    return "PUSH";
  }
}

package com.ebanking.notification.strategy;

import com.ebanking.notification.entity.Notification;
import com.ebanking.notification.enums.NotificationStatus;
import com.ebanking.notification.exception.NotificationSendException;
import com.ebanking.notification.mapper.NotificationMapper;
import com.ebanking.notification.repository.NotificationRepository;
import com.ebanking.notification.service.WebSocketNotificationPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Strategy for in-app notifications. Persists notification to database and publishes via WebSocket
 * for real-time delivery to connected clients.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class InAppNotificationStrategy implements NotificationStrategy {

  private final NotificationRepository notificationRepository;
  private final WebSocketNotificationPublisher webSocketPublisher;
  private final NotificationMapper notificationMapper;

  @Override
  public void send(Notification notification, String recipient, String subject, String content)
      throws NotificationSendException {

    try {
      log.info(
          "Creating in-app notification for user: {} (type: {})",
          notification.getUserId(),
          notification.getType());

      // For in-app notifications, we mark it as sent and persist
      notification.setStatus(NotificationStatus.SENT);
      notification.markAsSent();

      Notification savedNotification = notificationRepository.save(notification);

      // Publish to WebSocket for real-time delivery
      try {
        webSocketPublisher.publishToUser(
            savedNotification.getUserId(), notificationMapper.toDTO(savedNotification));

        // Also update the unread count
        long unreadCount =
            notificationRepository.countUnreadInAppNotifications(
                savedNotification.getUserId(),
                com.ebanking.notification.enums.NotificationChannel.IN_APP);
        webSocketPublisher.publishCountUpdate(savedNotification.getUserId(), unreadCount);

      } catch (Exception wsError) {
        // WebSocket publish failure shouldn't fail the notification
        log.warn(
            "Failed to publish notification via WebSocket, notification saved in DB: {}",
            wsError.getMessage());
      }

      log.info("In-app notification created successfully (ID: {})", savedNotification.getId());

    } catch (Exception e) {
      log.error("Failed to create in-app notification for user: {}", notification.getUserId(), e);
      throw new NotificationSendException(
          "Failed to create in-app notification: " + e.getMessage(), e);
    }
  }

  @Override
  public boolean supports(String recipient) {
    // In-app notifications don't require a specific recipient format
    // We use userId which is always present
    return true;
  }

  @Override
  public String getChannelName() {
    return "IN_APP";
  }
}

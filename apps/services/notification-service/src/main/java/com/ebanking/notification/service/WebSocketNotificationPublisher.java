package com.ebanking.notification.service;

import com.ebanking.notification.dto.NotificationDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

/**
 * Service for publishing notifications to WebSocket clients in real-time. Uses STOMP protocol to
 * send notifications to user-specific destinations.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WebSocketNotificationPublisher {

  private final SimpMessagingTemplate messagingTemplate;

  /**
   * Publish notification to a specific user via WebSocket. Frontend should subscribe to:
   * /user/queue/notifications
   *
   * @param userId User ID
   * @param notification Notification to send
   */
  public void publishToUser(Long userId, NotificationDTO notification) {
    try {
      log.debug(
          "Publishing notification {} to user {} via WebSocket", notification.getId(), userId);

      // Send to user-specific queue: /user/{userId}/queue/notifications
      messagingTemplate.convertAndSendToUser(
          userId.toString(), "/queue/notifications", notification);

      log.info(
          "Published notification {} to user {} via WebSocket successfully",
          notification.getId(),
          userId);

    } catch (Exception e) {
      // Don't fail the notification if WebSocket publish fails
      log.error(
          "Failed to publish notification {} to user {} via WebSocket: {}",
          notification.getId(),
          userId,
          e.getMessage());
    }
  }

  /**
   * Publish notification count update to user. Frontend should subscribe to:
   * /user/queue/notification-count
   *
   * @param userId User ID
   * @param count Unread notification count
   */
  public void publishCountUpdate(Long userId, Long count) {
    try {
      log.debug("Publishing notification count {} to user {} via WebSocket", count, userId);

      messagingTemplate.convertAndSendToUser(userId.toString(), "/queue/notification-count", count);

    } catch (Exception e) {
      log.error(
          "Failed to publish notification count to user {} via WebSocket: {}",
          userId,
          e.getMessage());
    }
  }

  /**
   * Broadcast notification to all connected users (for system-wide alerts). Frontend should
   * subscribe to: /topic/notifications/broadcast
   *
   * @param notification Notification to broadcast
   */
  public void broadcast(NotificationDTO notification) {
    try {
      log.debug("Broadcasting notification {} to all users", notification.getId());

      messagingTemplate.convertAndSend("/topic/notifications/broadcast", notification);

      log.info("Broadcasted notification {} successfully", notification.getId());

    } catch (Exception e) {
      log.error("Failed to broadcast notification {}: {}", notification.getId(), e.getMessage());
    }
  }
}

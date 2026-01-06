package com.ebanking.notification.controller;

import com.ebanking.notification.dto.NotificationDTO;
import com.ebanking.notification.service.NotificationService;
import com.ebanking.notification.util.JwtUtils;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

/**
 * WebSocket controller for handling real-time notification subscriptions and interactions. Clients
 * can send messages to mark notifications as read or request notification lists.
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class WebSocketNotificationController {

  private final NotificationService notificationService;
  private final JwtUtils jwtUtils;

  /**
   * Handle client request for unread notifications. Client sends to: /app/notifications/unread
   * Response sent to: /user/queue/notifications/list
   *
   * @param userId User ID from payload
   * @param authentication Authenticated user
   * @return List of unread notifications
   */
  @MessageMapping("/notifications/unread")
  @SendToUser("/queue/notifications/list")
  public List<NotificationDTO> getUnreadNotifications(
      @Payload Long userId, Authentication authentication) {
    log.debug("WebSocket request for unread notifications from user: {}", userId);

    // Validate that the authenticated user matches the requested userId
    Long authenticatedUserId = jwtUtils.extractUserId(authentication);
    if (authenticatedUserId == null || !authenticatedUserId.equals(userId)) {
      log.warn(
          "Unauthorized WebSocket request: authenticated userId {} doesn't match requested userId {}",
          authenticatedUserId,
          userId);
      return List.of();
    }

    return notificationService.getUnreadInAppNotifications(userId);
  }

  /**
   * Handle client request to mark notification as read. Client sends to:
   * /app/notifications/mark-read
   *
   * @param notificationId Notification ID from payload
   * @param authentication Authenticated user
   */
  @MessageMapping("/notifications/mark-read")
  public void markAsRead(@Payload Long notificationId, Authentication authentication) {
    log.debug("WebSocket request to mark notification {} as read", notificationId);

    Long userId = jwtUtils.extractUserId(authentication);
    if (userId == null) {
      log.warn("Could not extract userId from authentication for notification {}", notificationId);
      return;
    }

    notificationService.markAsRead(notificationId, userId);
  }
}

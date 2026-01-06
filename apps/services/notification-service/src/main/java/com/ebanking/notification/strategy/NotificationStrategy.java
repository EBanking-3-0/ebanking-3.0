package com.ebanking.notification.strategy;

import com.ebanking.notification.entity.Notification;
import com.ebanking.notification.exception.NotificationSendException;

/**
 * Strategy interface for sending notifications through different channels. Implementations of this
 * interface provide channel-specific logic for sending notifications.
 */
public interface NotificationStrategy {

  /**
   * Send a notification using this strategy.
   *
   * @param notification Notification to send
   * @param recipient Recipient address (email, phone, device token, etc.)
   * @param subject Subject of the notification
   * @param content Content of the notification
   * @throws NotificationSendException if sending fails
   */
  void send(Notification notification, String recipient, String subject, String content)
      throws NotificationSendException;

  /**
   * Check if this strategy supports sending to the given recipient. For example, email strategy
   * checks if recipient is a valid email.
   *
   * @param recipient Recipient address
   * @return true if supported
   */
  boolean supports(String recipient);

  /**
   * Get the channel name for this strategy.
   *
   * @return Channel name
   */
  String getChannelName();
}

package com.ebanking.notification.enums;

/** Status of a notification delivery attempt. */
public enum NotificationStatus {
  /** Notification is pending to be sent */
  PENDING,

  /** Notification is being sent */
  SENDING,

  /** Notification was successfully sent */
  SENT,

  /** Notification failed to send */
  FAILED,

  /** Notification was read by the user (for in-app) */
  READ,

  /** Notification delivery is being retried */
  RETRYING
}

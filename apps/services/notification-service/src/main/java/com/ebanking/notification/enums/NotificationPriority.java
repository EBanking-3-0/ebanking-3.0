package com.ebanking.notification.enums;

/** Priority level for notifications. */
public enum NotificationPriority {
  /** Low priority - informational */
  LOW,

  /** Normal priority - standard notifications */
  NORMAL,

  /** High priority - important notifications */
  HIGH,

  /** Critical priority - urgent notifications (e.g., fraud alerts) */
  CRITICAL
}

package com.ebanking.notification.enums;

/** Available notification delivery channels. */
public enum NotificationChannel {
  /** Email notification */
  EMAIL,

  /** In-app notification (stored in database, shown in UI) */
  IN_APP,

  /** SMS notification */
  SMS,

  /** Push notification (mobile/web) */
  PUSH
}

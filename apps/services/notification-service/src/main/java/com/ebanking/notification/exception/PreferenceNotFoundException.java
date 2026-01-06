package com.ebanking.notification.exception;

/** Exception thrown when user notification preferences are not found. */
public class PreferenceNotFoundException extends NotificationException {

  public PreferenceNotFoundException(String message) {
    super(message);
  }

  public PreferenceNotFoundException(Long userId) {
    super("Notification preferences not found for user: " + userId);
  }
}

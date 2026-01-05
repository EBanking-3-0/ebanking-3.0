package com.ebanking.notification.exception;

/** Exception thrown when a notification template is not found or invalid. */
public class TemplateException extends NotificationException {

  public TemplateException(String message) {
    super(message);
  }

  public TemplateException(String message, Throwable cause) {
    super(message, cause);
  }
}

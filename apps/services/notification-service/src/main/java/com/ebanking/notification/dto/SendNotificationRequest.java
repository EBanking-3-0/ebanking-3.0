package com.ebanking.notification.dto;

import com.ebanking.notification.enums.NotificationChannel;
import com.ebanking.notification.enums.NotificationPriority;
import com.ebanking.notification.enums.NotificationType;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** DTO for sending notification requests. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SendNotificationRequest {

  @NotNull(message = "User ID is required")
  private String userId;

  @NotNull(message = "Notification type is required")
  private NotificationType type;

  @NotNull(message = "Notification channel is required")
  private NotificationChannel channel;

  @Builder.Default private NotificationPriority priority = NotificationPriority.NORMAL;

  @NotNull(message = "Subject is required")
  private String subject;

  @NotNull(message = "Content is required")
  private String content;

  /** Recipient address override (optional, will fetch from user service if not provided) */
  private String recipient;

  /** Event ID that triggered this notification */
  private String eventId;

  /** Source service that generated this notification */
  private String source;

  /** Reference ID (transaction ID, account ID, etc.) */
  private String referenceId;

  /** Template variables for dynamic content */
  private Map<String, Object> templateVariables;

  /** Schedule notification for later */
  private LocalDateTime scheduledFor;
}

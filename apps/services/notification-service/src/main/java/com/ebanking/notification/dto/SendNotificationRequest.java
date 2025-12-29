package com.ebanking.notification.dto;

import com.ebanking.notification.entity.Notification;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Request DTO for sending notifications */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SendNotificationRequest {

  @NotNull(message = "User ID is required")
  private Long userId;

  // recipient could be email address, phone number, etc.
  @NotBlank(message = "Recipient is required")
  private String recipient;

  @NotNull(message = "Notification type is required")
  private Notification.NotificationType notificationType;

  @NotNull(message = "Channel is required")
  private Notification.NotificationChannel channel;

  private String subject;

  private String content;

  private String templateCode;

  private Map<String, Object> templateData;

  private String eventId;
}

package com.ebanking.notification.dto;

import com.ebanking.notification.enums.NotificationChannel;
import com.ebanking.notification.enums.NotificationType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** DTO for notification preference requests and responses. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationPreferenceDTO {
  private Long id;

  @NotNull(message = "User ID is required")
  private Long userId;

  @NotNull(message = "Notification type is required")
  private NotificationType notificationType;

  @NotNull(message = "Channel is required")
  private NotificationChannel channel;

  @Builder.Default private Boolean enabled = true;
}

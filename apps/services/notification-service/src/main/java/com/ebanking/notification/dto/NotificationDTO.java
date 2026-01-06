package com.ebanking.notification.dto;

import com.ebanking.notification.enums.NotificationChannel;
import com.ebanking.notification.enums.NotificationPriority;
import com.ebanking.notification.enums.NotificationStatus;
import com.ebanking.notification.enums.NotificationType;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** DTO for notification responses. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDTO {
  private Long id;
  private Long userId;
  private NotificationType type;
  private NotificationChannel channel;
  private NotificationStatus status;
  private NotificationPriority priority;
  private String subject;
  private String content;
  private String recipient;
  private String eventId;
  private String source;
  private String referenceId;
  private Integer retryCount;
  private String errorMessage;
  private LocalDateTime sentAt;
  private LocalDateTime readAt;
  private LocalDateTime scheduledFor;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}

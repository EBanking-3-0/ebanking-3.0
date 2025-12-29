package com.ebanking.notification.dto;

import com.ebanking.notification.entity.Notification;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for Notification response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDTO {

    private Long id;
    private Long userId;
    private String recipient;
    private Notification.NotificationType notificationType;
    private Notification.NotificationChannel channel;
    private String subject;
    private String content;
    private Notification.NotificationStatus status;
    private String errorMessage;
    private Integer retryCount;
    private LocalDateTime createdAt;
    private LocalDateTime sentAt;

    /**
     * Convert entity to DTO
     */
    public static NotificationDTO fromEntity(Notification notification) {
        return NotificationDTO.builder()
                .id(notification.getId())
                .userId(notification.getUserId())
                .recipient(notification.getRecipient())
                .notificationType(notification.getNotificationType())
                .channel(notification.getChannel())
                .subject(notification.getSubject())
                .content(notification.getContent())
                .status(notification.getStatus())
                .errorMessage(notification.getErrorMessage())
                .retryCount(notification.getRetryCount())
                .createdAt(notification.getCreatedAt())
                .sentAt(notification.getSentAt())
                .build();
    }
}

package com.ebanking.notification.entity;

import com.ebanking.notification.enums.NotificationChannel;
import com.ebanking.notification.enums.NotificationPriority;
import com.ebanking.notification.enums.NotificationStatus;
import com.ebanking.notification.enums.NotificationType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

/**
 * Entity representing a notification in the system. Tracks all notifications sent to users through
 * various channels.
 */
@Entity
@Table(
    name = "notifications",
    indexes = {
      @Index(name = "idx_user_id", columnList = "userId"),
      @Index(name = "idx_status", columnList = "status"),
      @Index(name = "idx_user_status", columnList = "userId, status"),
      @Index(name = "idx_created_at", columnList = "createdAt")
    })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  /** User ID this notification is for */
  @Column(nullable = false)
  private String userId;

  /** Type of notification */
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 50)
  private NotificationType type;

  /** Channel through which notification was sent */
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private NotificationChannel channel;

  /** Current status of the notification */
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  @Builder.Default
  private NotificationStatus status = NotificationStatus.PENDING;

  /** Priority level of the notification */
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  @Builder.Default
  private NotificationPriority priority = NotificationPriority.NORMAL;

  /** Subject/title of the notification */
  @Column(nullable = false, length = 255)
  private String subject;

  /** Main content/body of the notification */
  @Column(nullable = false, columnDefinition = "TEXT")
  private String content;

  /** Recipient address (email, phone number, device token, etc.) */
  @Column(length = 255)
  private String recipient;

  /** ID of the event that triggered this notification */
  @Column(length = 100)
  private String eventId;

  /** Source of the event (e.g., "payment-service", "auth-service") */
  @Column(length = 100)
  private String source;

  /** Reference ID (transaction ID, account ID, etc.) */
  @Column(length = 100)
  private String referenceId;

  /** Number of retry attempts */
  @Column(nullable = false)
  @Builder.Default
  private Integer retryCount = 0;

  /** Error message if sending failed */
  @Column(columnDefinition = "TEXT")
  private String errorMessage;

  /** Timestamp when notification was sent successfully */
  private LocalDateTime sentAt;

  /** Timestamp when notification was read (for in-app) */
  private LocalDateTime readAt;

  /** Timestamp when the notification should be sent (for scheduled notifications) */
  private LocalDateTime scheduledFor;

  @CreationTimestamp
  @Column(nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(nullable = false)
  private LocalDateTime updatedAt;

  /** Mark notification as sent */
  public void markAsSent() {
    this.status = NotificationStatus.SENT;
    this.sentAt = LocalDateTime.now();
  }

  /** Mark notification as failed */
  public void markAsFailed(String errorMessage) {
    this.status = NotificationStatus.FAILED;
    this.errorMessage = errorMessage;
  }

  /** Mark notification as read (for in-app) */
  public void markAsRead() {
    this.status = NotificationStatus.READ;
    this.readAt = LocalDateTime.now();
  }

  /** Increment retry count */
  public void incrementRetryCount() {
    this.retryCount++;
    this.status = NotificationStatus.RETRYING;
  }
}

package com.ebanking.notification.entity;

import com.ebanking.notification.enums.NotificationChannel;
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
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

/**
 * Entity representing user notification preferences. Defines which notification channels a user
 * wants to receive notifications through for specific notification types.
 */
@Entity
@Table(
    name = "notification_preferences",
    uniqueConstraints = {
      @UniqueConstraint(
          name = "uk_user_type_channel",
          columnNames = {"userId", "notificationType", "channel"})
    },
    indexes = {@Index(name = "idx_user_id", columnList = "userId")})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationPreference {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  /** User ID these preferences belong to */
  @Column(nullable = false)
  private String userId;

  /** Type of notification */
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 50)
  private NotificationType notificationType;

  /** Channel through which user wants to receive this type */
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private NotificationChannel channel;

  /** Whether this preference is enabled */
  @Column(nullable = false)
  @Builder.Default
  private Boolean enabled = true;

  @CreationTimestamp
  @Column(nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(nullable = false)
  private LocalDateTime updatedAt;
}

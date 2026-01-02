package com.ebanking.notification.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

/**
 * Entity representing user notification preferences.
 * Stores contact information and global channel preferences for each user.
 * One record per user (not per notification type).
 */
@Entity
@Table(name = "notification_preferences", indexes = {
    @Index(name = "idx_user_id", columnList = "user_id", unique = true) })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationPreference {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "user_id", nullable = false, unique = true)
  private Long userId;

  // Contact Information - Single source of truth for user contact details
  @Column(name = "email_address", unique = true)
  private String emailAddress;

  @Column(name = "phone_number", unique = true)
  private String phoneNumber;

  @Column(name = "push_token")
  private String pushToken;

  // Channel Enable/Disable Flags
  @Builder.Default
  @Column(name = "email_enabled", nullable = false)
  private Boolean emailEnabled = true;

  @Builder.Default
  @Column(name = "sms_enabled", nullable = false)
  private Boolean smsEnabled = false;

  @Builder.Default
  @Column(name = "push_enabled", nullable = false)
  private Boolean pushEnabled = false;

  @Builder.Default
  @Column(name = "in_app_enabled", nullable = false)
  private Boolean inAppEnabled = true;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at")
  private LocalDateTime updatedAt;
}

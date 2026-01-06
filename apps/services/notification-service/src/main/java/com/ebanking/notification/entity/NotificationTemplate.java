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
 * Entity representing notification templates. Stores templates for different notification types and
 * channels.
 */
@Entity
@Table(
    name = "notification_templates",
    uniqueConstraints = {
      @UniqueConstraint(
          name = "uk_type_channel_locale",
          columnNames = {"notificationType", "channel", "locale"})
    },
    indexes = {@Index(name = "idx_type_channel", columnList = "notificationType, channel")})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationTemplate {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  /** Type of notification this template is for */
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 50)
  private NotificationType notificationType;

  /** Channel this template is for */
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private NotificationChannel channel;

  /** Template name/identifier */
  @Column(nullable = false, length = 100)
  private String name;

  /** Subject template (for email/push) */
  @Column(length = 255)
  private String subject;

  /** Template content (can be plain text, HTML, or template path) */
  @Column(nullable = false, columnDefinition = "TEXT")
  private String content;

  /** Locale/language for this template */
  @Column(nullable = false, length = 10)
  @Builder.Default
  private String locale = "en";

  /** Whether this template is active */
  @Column(nullable = false)
  @Builder.Default
  private Boolean active = true;

  @CreationTimestamp
  @Column(nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(nullable = false)
  private LocalDateTime updatedAt;
}

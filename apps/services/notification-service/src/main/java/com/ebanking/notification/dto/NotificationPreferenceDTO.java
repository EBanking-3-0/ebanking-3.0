package com.ebanking.notification.dto;

import com.ebanking.notification.entity.Notification;
import com.ebanking.notification.entity.NotificationPreference;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** DTO for Notification Preference */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationPreferenceDTO {

  private Long id;
  private Long userId;
  private Notification.NotificationType notificationType;
  private Boolean emailEnabled;
  private Boolean smsEnabled;
  private Boolean pushEnabled;
  private Boolean inAppEnabled;

  /** Convert entity to DTO */
  public static NotificationPreferenceDTO fromEntity(NotificationPreference preference) {
    return NotificationPreferenceDTO.builder()
        .id(preference.getId())
        .userId(preference.getUserId())
        .notificationType(preference.getNotificationType())
        .emailEnabled(preference.getEmailEnabled())
        .smsEnabled(preference.getSmsEnabled())
        .pushEnabled(preference.getPushEnabled())
        .inAppEnabled(preference.getInAppEnabled())
        .build();
  }

  /** Convert DTO to entity */
  public NotificationPreference toEntity() {
    return NotificationPreference.builder()
        .id(this.id)
        .userId(this.userId)
        .notificationType(this.notificationType)
        .emailEnabled(this.emailEnabled)
        .smsEnabled(this.smsEnabled)
        .pushEnabled(this.pushEnabled)
        .inAppEnabled(this.inAppEnabled)
        .build();
  }
}

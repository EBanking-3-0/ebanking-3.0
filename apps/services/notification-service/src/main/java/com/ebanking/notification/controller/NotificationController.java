package com.ebanking.notification.controller;

import com.ebanking.notification.dto.NotificationDTO;
import com.ebanking.notification.dto.NotificationPreferenceDTO;
import com.ebanking.notification.dto.UserContactDTO;
import com.ebanking.notification.entity.Notification;
import com.ebanking.notification.entity.NotificationPreference;
import com.ebanking.notification.repository.NotificationPreferenceRepository;
import com.ebanking.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for notification management. Provides endpoints for viewing
 * notification history
 * and managing preferences.
 */
@Slf4j
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

  private final NotificationRepository notificationRepository;
  private final NotificationPreferenceRepository preferenceRepository;

  /** Get notification history for a user */
  @GetMapping("/user/{userId}")
  @PreAuthorize("hasRole('USER')")
  public ResponseEntity<Page<NotificationDTO>> getUserNotifications(
      @PathVariable Long userId,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {

    log.info("Fetching notifications for user: {}", userId);

    PageRequest pageRequest = PageRequest.of(page, size, Sort.by("createdAt").descending());
    Page<Notification> notifications = notificationRepository.findByUserId(userId, pageRequest);

    Page<NotificationDTO> dtoPage = notifications.map(NotificationDTO::fromEntity);

    return ResponseEntity.ok(dtoPage);
  }

  /** Get user's notification preferences */
  @GetMapping("/preferences/{userId}")
  @PreAuthorize("hasRole('USER')")
  public ResponseEntity<NotificationPreferenceDTO> getUserPreferences(@PathVariable Long userId) {
    log.info("Fetching notification preferences for user: {}", userId);

    NotificationPreference preference = preferenceRepository
        .findByUserId(userId)
        .orElseThrow(
            () -> new RuntimeException("No notification preferences found for user: " + userId));

    return ResponseEntity.ok(NotificationPreferenceDTO.fromEntity(preference));
  }

  /** Get user's contact information */
  @GetMapping("/contacts/{userId}")
  @PreAuthorize("hasRole('USER')")
  public ResponseEntity<UserContactDTO> getUserContactInfo(@PathVariable Long userId) {
    log.info("Fetching contact information for user: {}", userId);

    NotificationPreference preference = preferenceRepository
        .findByUserId(userId)
        .orElseThrow(
            () -> new RuntimeException("No notification preferences found for user: " + userId));

    UserContactDTO contactDTO = UserContactDTO.builder()
        .emailAddress(preference.getEmailAddress())
        .phoneNumber(preference.getPhoneNumber())
        .pushToken(preference.getPushToken())
        .build();

    return ResponseEntity.ok(contactDTO);
  }

  /** Update user's contact information */
  @PutMapping("/contacts/{userId}")
  @PreAuthorize("hasRole('USER')")
  public ResponseEntity<String> updateUserContactInfo(
      @PathVariable Long userId, @RequestBody UserContactDTO contactDTO) {

    log.info("Updating contact information for user: {}", userId);

    NotificationPreference preference = preferenceRepository
        .findByUserId(userId)
        .orElse(
            NotificationPreference.builder()
                .userId(userId)
                .emailEnabled(true)
                .smsEnabled(contactDTO.getPhoneNumber() != null)
                .pushEnabled(contactDTO.getPushToken() != null)
                .inAppEnabled(true)
                .build());

    // Update contact info
    if (contactDTO.getEmailAddress() != null) {
      preference.setEmailAddress(contactDTO.getEmailAddress());
    }
    if (contactDTO.getPhoneNumber() != null) {
      preference.setPhoneNumber(contactDTO.getPhoneNumber());
      preference.setSmsEnabled(true); // Auto-enable SMS when phone is added
    }
    if (contactDTO.getPushToken() != null) {
      preference.setPushToken(contactDTO.getPushToken());
      preference.setPushEnabled(true); // Auto-enable push when token is added
    }

    preferenceRepository.save(preference);

    log.info("Contact information updated successfully for user: {}", userId);
    return ResponseEntity.ok("Contact information updated successfully");
  }

  /** Update user's notification preference */
  @PutMapping("/preferences")
  @PreAuthorize("hasRole('USER')")
  public ResponseEntity<NotificationPreferenceDTO> updatePreference(
      @RequestBody NotificationPreferenceDTO preferenceDTO) {

    log.info("Updating notification preference for user: {}", preferenceDTO.getUserId());

    NotificationPreference preference = preferenceRepository
        .findByUserId(preferenceDTO.getUserId())
        .orElse(
            NotificationPreference.builder().userId(preferenceDTO.getUserId()).build());

    // Update channel preferences
    if (preferenceDTO.getEmailEnabled() != null) {
      preference.setEmailEnabled(preferenceDTO.getEmailEnabled());
    }
    if (preferenceDTO.getSmsEnabled() != null) {
      preference.setSmsEnabled(preferenceDTO.getSmsEnabled());
    }
    if (preferenceDTO.getPushEnabled() != null) {
      preference.setPushEnabled(preferenceDTO.getPushEnabled());
    }
    if (preferenceDTO.getInAppEnabled() != null) {
      preference.setInAppEnabled(preferenceDTO.getInAppEnabled());
    }

    // Update contact info if provided
    if (preferenceDTO.getEmailAddress() != null) {
      preference.setEmailAddress(preferenceDTO.getEmailAddress());
    }
    if (preferenceDTO.getPhoneNumber() != null) {
      preference.setPhoneNumber(preferenceDTO.getPhoneNumber());
    }
    if (preferenceDTO.getPushToken() != null) {
      preference.setPushToken(preferenceDTO.getPushToken());
    }

    preference = preferenceRepository.save(preference);

    return ResponseEntity.ok(NotificationPreferenceDTO.fromEntity(preference));
  }

  /** Get notification statistics for a user */
  @GetMapping("/stats/{userId}")
  @PreAuthorize("hasRole('USER')")
  public ResponseEntity<NotificationStats> getUserStats(@PathVariable Long userId) {
    log.info("Fetching notification statistics for user: {}", userId);

    long total = notificationRepository
        .findByUserId(userId, PageRequest.of(0, Integer.MAX_VALUE))
        .getTotalElements();
    long sent = notificationRepository.countByUserIdAndStatus(userId, Notification.NotificationStatus.SENT);
    long failed = notificationRepository.countByUserIdAndStatus(
        userId, Notification.NotificationStatus.FAILED);
    long pending = notificationRepository.countByUserIdAndStatus(
        userId, Notification.NotificationStatus.PENDING);

    return ResponseEntity.ok(new NotificationStats(total, sent, failed, pending));
  }

  /** Stats DTO */
  public record NotificationStats(long total, long sent, long failed, long pending) {
  }
}

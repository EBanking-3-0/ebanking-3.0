package com.ebanking.notification.controller;

import com.ebanking.notification.dto.NotificationDTO;
import com.ebanking.notification.dto.NotificationPreferenceDTO;
import com.ebanking.notification.entity.Notification;
import com.ebanking.notification.entity.NotificationPreference;
import com.ebanking.notification.repository.NotificationPreferenceRepository;
import com.ebanking.notification.repository.NotificationRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for notification management. Provides endpoints for viewing notification history
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
  public ResponseEntity<List<NotificationPreferenceDTO>> getUserPreferences(
      @PathVariable Long userId) {
    log.info("Fetching notification preferences for user: {}", userId);

    List<NotificationPreference> preferences = preferenceRepository.findByUserId(userId);
    List<NotificationPreferenceDTO> dtos =
        preferences.stream()
            .map(NotificationPreferenceDTO::fromEntity)
            .collect(Collectors.toList());

    return ResponseEntity.ok(dtos);
  }

  /** Update user's notification preference */
  @PutMapping("/preferences")
  @PreAuthorize("hasRole('USER')")
  public ResponseEntity<NotificationPreferenceDTO> updatePreference(
      @RequestBody NotificationPreferenceDTO preferenceDTO) {

    log.info("Updating notification preference for user: {}", preferenceDTO.getUserId());

    NotificationPreference preference =
        preferenceRepository
            .findByUserIdAndNotificationType(
                preferenceDTO.getUserId(), preferenceDTO.getNotificationType())
            .orElse(
                NotificationPreference.builder()
                    .userId(preferenceDTO.getUserId())
                    .notificationType(preferenceDTO.getNotificationType())
                    .build());

    preference.setEmailEnabled(preferenceDTO.getEmailEnabled());
    preference.setSmsEnabled(preferenceDTO.getSmsEnabled());
    preference.setPushEnabled(preferenceDTO.getPushEnabled());
    preference.setInAppEnabled(preferenceDTO.getInAppEnabled());

    preference = preferenceRepository.save(preference);

    return ResponseEntity.ok(NotificationPreferenceDTO.fromEntity(preference));
  }

  /** Get notification statistics for a user */
  @GetMapping("/stats/{userId}")
  @PreAuthorize("hasRole('USER')")
  public ResponseEntity<NotificationStats> getUserStats(@PathVariable Long userId) {
    log.info("Fetching notification statistics for user: {}", userId);

    long total =
        notificationRepository
            .findByUserId(userId, PageRequest.of(0, Integer.MAX_VALUE))
            .getTotalElements();
    long sent =
        notificationRepository.countByUserIdAndStatus(userId, Notification.NotificationStatus.SENT);
    long failed =
        notificationRepository.countByUserIdAndStatus(
            userId, Notification.NotificationStatus.FAILED);
    long pending =
        notificationRepository.countByUserIdAndStatus(
            userId, Notification.NotificationStatus.PENDING);

    return ResponseEntity.ok(new NotificationStats(total, sent, failed, pending));
  }

  /** Stats DTO */
  public record NotificationStats(long total, long sent, long failed, long pending) {}
}

package com.ebanking.notification.controller;

import com.ebanking.notification.dto.NotificationDTO;
import com.ebanking.notification.dto.NotificationPreferenceDTO;
import com.ebanking.notification.dto.SendNotificationRequest;
import com.ebanking.notification.service.NotificationService;
import com.ebanking.notification.service.PreferenceService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for notification management. Provides endpoints for sending notifications,
 * querying notification history, and managing preferences.
 */
@Slf4j
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

  private final NotificationService notificationService;
  private final PreferenceService preferenceService;

  /**
   * Send a notification.
   *
   * @param request Send notification request
   * @return Sent notification DTO
   */
  @PostMapping
  public ResponseEntity<NotificationDTO> sendNotification(
      @Valid @RequestBody SendNotificationRequest request) {

    log.info("Received request to send notification for user: {}", request.getUserId());

    NotificationDTO notification = notificationService.sendNotification(request);

    return ResponseEntity.status(HttpStatus.CREATED).body(notification);
  }

  /**
   * Get notifications for a user.
   *
   * @param userId User ID
   * @param pageable Pagination parameters
   * @return Page of notifications
   */
  @GetMapping("/user/{userId}")
  public ResponseEntity<Page<NotificationDTO>> getUserNotifications(
      @PathVariable Long userId, Pageable pageable) {

    log.debug("Fetching notifications for user: {}", userId);

    Page<NotificationDTO> notifications =
        notificationService.getUserNotifications(userId, pageable);

    return ResponseEntity.ok(notifications);
  }

  /**
   * Get unread in-app notifications for a user.
   *
   * @param userId User ID
   * @return List of unread notifications
   */
  @GetMapping("/user/{userId}/unread")
  public ResponseEntity<List<NotificationDTO>> getUnreadNotifications(@PathVariable Long userId) {
    log.debug("Fetching unread notifications for user: {}", userId);

    List<NotificationDTO> notifications = notificationService.getUnreadInAppNotifications(userId);

    return ResponseEntity.ok(notifications);
  }

  /**
   * Get unread notification count for a user.
   *
   * @param userId User ID
   * @return Unread count
   */
  @GetMapping("/user/{userId}/unread/count")
  public ResponseEntity<Long> getUnreadCount(@PathVariable Long userId) {
    log.debug("Fetching unread count for user: {}", userId);

    long count = notificationService.getUnreadCount(userId);

    return ResponseEntity.ok(count);
  }

  /**
   * Mark a notification as read.
   *
   * @param notificationId Notification ID
   * @param userId User ID (from auth context or header)
   * @return Updated notification DTO
   */
  @PutMapping("/{notificationId}/read")
  public ResponseEntity<NotificationDTO> markAsRead(
      @PathVariable Long notificationId, @RequestParam Long userId) {

    log.info("Marking notification {} as read for user {}", notificationId, userId);

    NotificationDTO notification = notificationService.markAsRead(notificationId, userId);

    return ResponseEntity.ok(notification);
  }

  /**
   * Mark all notifications as read for a user.
   *
   * @param userId User ID
   * @return Success message
   */
  @PutMapping("/user/{userId}/read-all")
  public ResponseEntity<String> markAllAsRead(@PathVariable Long userId) {
    log.info("Marking all notifications as read for user {}", userId);

    notificationService.markAllAsRead(userId);

    return ResponseEntity.ok("All notifications marked as read");
  }

  /**
   * Retry a failed notification.
   *
   * @param notificationId Notification ID
   * @return Retried notification DTO
   */
  @PostMapping("/{notificationId}/retry")
  public ResponseEntity<NotificationDTO> retryNotification(@PathVariable Long notificationId) {
    log.info("Retrying notification: {}", notificationId);

    NotificationDTO notification = notificationService.retryNotification(notificationId);

    return ResponseEntity.ok(notification);
  }

  /**
   * Get user notification preferences.
   *
   * @param userId User ID
   * @return List of preferences
   */
  @GetMapping("/preferences/user/{userId}")
  public ResponseEntity<List<NotificationPreferenceDTO>> getUserPreferences(
      @PathVariable Long userId) {

    log.debug("Fetching preferences for user: {}", userId);

    List<NotificationPreferenceDTO> preferences = preferenceService.getUserPreferences(userId);

    return ResponseEntity.ok(preferences);
  }

  /**
   * Save or update notification preference.
   *
   * @param preference Preference DTO
   * @return Saved preference DTO
   */
  @PostMapping("/preferences")
  public ResponseEntity<NotificationPreferenceDTO> savePreference(
      @Valid @RequestBody NotificationPreferenceDTO preference) {

    log.info("Saving preference for user: {}", preference.getUserId());

    NotificationPreferenceDTO saved = preferenceService.savePreference(preference);

    return ResponseEntity.ok(saved);
  }

  /**
   * Save multiple preferences at once.
   *
   * @param preferences List of preferences
   * @return List of saved preferences
   */
  @PostMapping("/preferences/batch")
  public ResponseEntity<List<NotificationPreferenceDTO>> savePreferences(
      @Valid @RequestBody List<NotificationPreferenceDTO> preferences) {

    log.info("Saving {} preferences", preferences.size());

    List<NotificationPreferenceDTO> saved = preferenceService.savePreferences(preferences);

    return ResponseEntity.ok(saved);
  }

  /**
   * Initialize default preferences for a user.
   *
   * @param userId User ID
   * @return Success message
   */
  @PostMapping("/preferences/user/{userId}/init")
  public ResponseEntity<String> initializePreferences(@PathVariable Long userId) {
    log.info("Initializing default preferences for user: {}", userId);

    preferenceService.initializeDefaultPreferences(userId);

    return ResponseEntity.ok("Default preferences initialized");
  }

  /**
   * Delete a preference.
   *
   * @param preferenceId Preference ID
   * @return Success message
   */
  @DeleteMapping("/preferences/{preferenceId}")
  public ResponseEntity<String> deletePreference(@PathVariable Long preferenceId) {
    log.info("Deleting preference: {}", preferenceId);

    preferenceService.deletePreference(preferenceId);

    return ResponseEntity.ok("Preference deleted");
  }
}

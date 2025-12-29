package com.ebanking.notification.controller;

import com.ebanking.notification.dto.SendNotificationRequest;
import com.ebanking.notification.entity.Notification;
import com.ebanking.notification.service.NotificationService;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** REST Controller for testing notification functionality */
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationTestController {

  private final NotificationService notificationService;

  /** Send a simple test notification */
  @PostMapping("/test/simple")
  public ResponseEntity<Notification> sendSimpleNotification(
      @RequestBody SendNotificationRequest request) {
    Notification result = notificationService.sendNotification(request);
    return ResponseEntity.ok(result);
  }

  /** Send templated email for testing */
  @PostMapping("/test/email/template")
  public ResponseEntity<String> sendTemplatedEmail(
      @RequestParam Long userId,
      @RequestParam String email,
      @RequestParam String templateCode,
      @RequestParam(required = false) String subject,
      @RequestBody Map<String, Object> data) {

    notificationService.sendTemplatedEmail(userId, email, templateCode, subject, data);
    return ResponseEntity.ok("Templated email sent successfully");
  }

  /** Send simple email for testing */
  @PostMapping("/test/email/simple")
  public ResponseEntity<String> sendSimpleEmail(
      @RequestParam Long userId,
      @RequestParam String email,
      @RequestParam String subject,
      @RequestParam String content) {

    notificationService.sendSimpleEmail(
        userId, email, subject, content, Notification.NotificationType.GENERIC);
    return ResponseEntity.ok("Simple email sent successfully");
  }

  /** Send SMS for testing */
  @PostMapping("/test/sms")
  public ResponseEntity<String> sendSms(
      @RequestParam Long userId, @RequestParam String phoneNumber, @RequestParam String message) {

    notificationService.sendSms(
        userId, phoneNumber, message, Notification.NotificationType.GENERIC);
    return ResponseEntity.ok("SMS sent successfully");
  }

  /** Trigger retry mechanism for testing */
  @PostMapping("/test/retry")
  public ResponseEntity<String> triggerRetry() {
    notificationService.retryFailedNotifications();
    return ResponseEntity.ok("Retry process triggered");
  }

  /** Get notification history for testing */
  @GetMapping("/history/{userId}")
  public ResponseEntity<List<Notification>> getNotificationHistory(@PathVariable Long userId) {
    List<Notification> notifications = notificationService.getNotificationHistory(userId);
    return ResponseEntity.ok(notifications);
  }

  /** Health check endpoint */
  @GetMapping("/health")
  public ResponseEntity<Map<String, Object>> healthCheck() {
    return ResponseEntity.ok(
        Map.of(
            "status", "UP",
            "service", "notification-service",
            "timestamp", java.time.LocalDateTime.now()));
  }
}

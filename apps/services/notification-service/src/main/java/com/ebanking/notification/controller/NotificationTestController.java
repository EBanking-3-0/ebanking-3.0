package com.ebanking.notification.controller;

import com.ebanking.notification.dto.NotificationDTO;
import com.ebanking.notification.dto.SendNotificationRequest;
import com.ebanking.notification.enums.NotificationChannel;
import com.ebanking.notification.enums.NotificationPriority;
import com.ebanking.notification.enums.NotificationType;
import com.ebanking.notification.service.NotificationService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Test controller for manually triggering notifications. Used for testing and demonstration
 * purposes.
 */
@Slf4j
@RestController
@RequestMapping("/api/notifications/test")
@RequiredArgsConstructor
public class NotificationTestController {

  private final NotificationService notificationService;

  /**
   * Test welcome notification.
   *
   * @param userId User ID
   * @return Sent notifications
   */
  @PostMapping("/welcome")
  public ResponseEntity<List<NotificationDTO>> testWelcomeNotification(@RequestParam Long userId) {
    log.info("Testing welcome notification for user: {}", userId);

    Map<String, Object> variables = new HashMap<>();
    variables.put("username", "testuser");
    variables.put("firstName", "Test");
    variables.put("lastName", "User");

    List<NotificationDTO> notifications =
        notificationService.sendToAllChannels(userId, NotificationType.WELCOME, variables);

    return ResponseEntity.ok(notifications);
  }

  /**
   * Test transaction notification.
   *
   * @param userId User ID
   * @return Sent notifications
   */
  @PostMapping("/transaction")
  public ResponseEntity<List<NotificationDTO>> testTransactionNotification(
      @RequestParam Long userId) {

    log.info("Testing transaction notification for user: {}", userId);

    Map<String, Object> variables = new HashMap<>();
    variables.put("transactionId", "TXN-12345");
    variables.put("amount", "USD 1,250.00");
    variables.put("currency", "USD");
    variables.put("transactionType", "TRANSFER");
    variables.put("fromAccount", "****1234");
    variables.put("toAccount", "****5678");
    variables.put("description", "Payment to vendor");
    variables.put("status", "COMPLETED");

    List<NotificationDTO> notifications =
        notificationService.sendToAllChannels(userId, NotificationType.TRANSACTION, variables);

    return ResponseEntity.ok(notifications);
  }

  /**
   * Test fraud alert notification.
   *
   * @param userId User ID
   * @return Sent notifications
   */
  @PostMapping("/fraud-alert")
  public ResponseEntity<List<NotificationDTO>> testFraudAlert(@RequestParam Long userId) {
    log.info("Testing fraud alert notification for user: {}", userId);

    Map<String, Object> variables = new HashMap<>();
    variables.put("transactionId", "TXN-99999");
    variables.put("amount", "USD 5,000.00");
    variables.put("currency", "USD");
    variables.put("fraudType", "SUSPICIOUS_AMOUNT");
    variables.put("severity", "HIGH");
    variables.put("description", "Unusual transaction amount detected from foreign location");
    variables.put("accountId", "ACC-1234");

    List<NotificationDTO> notifications =
        notificationService.sendToAllChannels(userId, NotificationType.FRAUD_ALERT, variables);

    return ResponseEntity.ok(notifications);
  }

  /**
   * Test alert notification.
   *
   * @param userId User ID
   * @return Sent notifications
   */
  @PostMapping("/alert")
  public ResponseEntity<List<NotificationDTO>> testAlert(@RequestParam Long userId) {
    log.info("Testing alert notification for user: {}", userId);

    Map<String, Object> variables = new HashMap<>();
    variables.put("alertType", "LOW_BALANCE");
    variables.put("severity", "WARNING");
    variables.put("message", "Your account balance is below the threshold");
    variables.put("threshold", "USD 500.00");
    variables.put("currentValue", "USD 245.50");
    variables.put("accountNumber", "****1234");

    List<NotificationDTO> notifications =
        notificationService.sendToAllChannels(userId, NotificationType.ALERT, variables);

    return ResponseEntity.ok(notifications);
  }

  /**
   * Test custom notification with specific channel.
   *
   * @param request Custom notification request
   * @return Sent notification
   */
  @PostMapping("/custom")
  public ResponseEntity<NotificationDTO> testCustomNotification(
      @RequestBody SendNotificationRequest request) {

    log.info("Testing custom notification for user: {}", request.getUserId());

    // Set defaults if not provided
    if (request.getPriority() == null) {
      request.setPriority(NotificationPriority.NORMAL);
    }

    if (request.getType() == null) {
      request.setType(NotificationType.CUSTOM);
    }

    NotificationDTO notification = notificationService.sendNotification(request);

    return ResponseEntity.ok(notification);
  }

  /**
   * Test email notification directly.
   *
   * @param userId User ID
   * @param subject Email subject
   * @param content Email content
   * @return Sent notification
   */
  @PostMapping("/email")
  public ResponseEntity<NotificationDTO> testEmail(
      @RequestParam Long userId, @RequestParam String subject, @RequestParam String content) {

    log.info("Testing email notification for user: {}", userId);

    SendNotificationRequest request =
        SendNotificationRequest.builder()
            .userId(userId)
            .type(NotificationType.CUSTOM)
            .channel(NotificationChannel.EMAIL)
            .priority(NotificationPriority.NORMAL)
            .subject(subject)
            .content(content)
            .build();

    NotificationDTO notification = notificationService.sendNotification(request);

    return ResponseEntity.ok(notification);
  }

  /**
   * Test in-app notification directly.
   *
   * @param userId User ID
   * @param subject Notification subject
   * @param content Notification content
   * @return Sent notification
   */
  @PostMapping("/in-app")
  public ResponseEntity<NotificationDTO> testInApp(
      @RequestParam Long userId, @RequestParam String subject, @RequestParam String content) {

    log.info("Testing in-app notification for user: {}", userId);

    SendNotificationRequest request =
        SendNotificationRequest.builder()
            .userId(userId)
            .type(NotificationType.CUSTOM)
            .channel(NotificationChannel.IN_APP)
            .priority(NotificationPriority.NORMAL)
            .subject(subject)
            .content(content)
            .build();

    NotificationDTO notification = notificationService.sendNotification(request);

    return ResponseEntity.ok(notification);
  }

  /**
   * Health check endpoint.
   *
   * @return Health status
   */
  @GetMapping("/health")
  public ResponseEntity<Map<String, String>> health() {
    Map<String, String> response = new HashMap<>();
    response.put("status", "UP");
    response.put("service", "notification-service");
    return ResponseEntity.ok(response);
  }
}

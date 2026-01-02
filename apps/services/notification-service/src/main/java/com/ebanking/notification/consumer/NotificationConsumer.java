package com.ebanking.notification.consumer;

import com.ebanking.notification.entity.Notification;
import com.ebanking.notification.entity.NotificationPreference;
import com.ebanking.notification.repository.NotificationPreferenceRepository;
import com.ebanking.notification.service.NotificationService;
import com.ebanking.shared.kafka.KafkaTopics;
import com.ebanking.shared.kafka.events.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * for now only email notifications are implemented
 * TODO: extend to SMS, push notifications, etc.
 * Consumer for notification-related events. Sends notifications
 * (email/SMS/push) when events occur.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationConsumer {

  private final NotificationService notificationService;
  private final NotificationPreferenceRepository preferenceRepository;

  @KafkaListener(topics = KafkaTopics.USER_CREATED)
  public void handleUserCreated(@Payload UserCreatedEvent event, Acknowledgment acknowledgment) {
    try {
      log.info("Received user.created event for user: {}", event.getUserId());

      // Create single notification preference record with contact information
      NotificationPreference preference = preferenceRepository
          .findByUserId(event.getUserId())
          .orElse(
              NotificationPreference.builder()
                  .userId(event.getUserId())
                  .build());

      // Store contact information (single source of truth)
      preference.setEmailAddress(event.getEmail());
      preference.setEmailEnabled(true); // Default: email enabled
      preference.setSmsEnabled(false); // User can enable via API
      preference.setPushEnabled(false);
      preference.setInAppEnabled(true);

      preferenceRepository.save(preference);

      log.info("Created notification preference with contact info for user: {}", event.getUserId());

      // Send welcome email using template
      Map<String, Object> templateData = new HashMap<>();
      templateData.put("firstName", event.getFirstName());
      templateData.put("email", event.getEmail());
      templateData.put("userId", event.getUserId());
      templateData.put("registrationDate", LocalDateTime.now());

      notificationService.sendTemplatedEmail(
          event.getUserId(), event.getEmail(), "welcome-email", templateData);

      acknowledgment.acknowledge();
      log.info("Processed user.created event for user: {}", event.getUserId());

    } catch (Exception e) {
      log.error("Failed to process user.created event: {}", event.getEventId(), e);
      acknowledgment.acknowledge(); // Acknowledge to prevent blocking
    }
  }

  @KafkaListener(topics = KafkaTopics.TRANSACTION_COMPLETED)
  public void handleTransactionCompleted(
      @Payload TransactionCompletedEvent event, Acknowledgment acknowledgment) {
    try {
      log.info("Received transaction.completed event: {}", event.getTransactionId());

      // Send transaction notification using template
      Map<String, Object> templateData = new HashMap<>();
      templateData.put("transactionId", event.getTransactionId());
      templateData.put("fromAccount", formatAccountId(event.getFromAccountId()));
      templateData.put("toAccount", formatAccountId(event.getToAccountId()));
      templateData.put("amount", event.getAmount());
      templateData.put("currency", event.getCurrency());
      templateData.put("transactionDate", LocalDateTime.now());

      // Lookup user contact info from NotificationPreference
      Long userId = event.getToAccountId();
      String email = getUserEmail(userId);

      if (email != null) {
        notificationService.sendTemplatedEmail(userId, email, "transaction-email", templateData);
      } else {
        log.warn("No email found for user: {}, skipping notification", userId);
      }

      acknowledgment.acknowledge();

    } catch (Exception e) {
      log.error("Failed to process transaction.completed event: {}", event.getEventId(), e);
      acknowledgment.acknowledge();
    }
  }

  @KafkaListener(topics = KafkaTopics.PAYMENT_FAILED)
  public void handlePaymentFailed(
      @Payload PaymentFailedEvent event, Acknowledgment acknowledgment) {
    try {
      log.info("Received payment.failed event: {}", event.getTransactionId());

      // Send payment failure notification
      String message = String.format(
          "Your payment transaction %s has failed. Reason: %s. Please try again or contact support.",
          event.getTransactionId(), event.getFailureReason());

      // Lookup user contact info from NotificationPreference
      Long userId = event.getUserId();
      String email = getUserEmail(userId);

      if (email != null) {
        notificationService.sendSimpleEmail(
            userId, email, "Payment Failed", message, Notification.NotificationType.PAYMENT_FAILED);
      } else {
        log.warn("No email found for user: {}, skipping notification", userId);
      }

      acknowledgment.acknowledge();

    } catch (Exception e) {
      log.error("Failed to process payment.failed event: {}", event.getEventId(), e);
      acknowledgment.acknowledge();
    }
  }

  @KafkaListener(topics = KafkaTopics.FRAUD_DETECTED)
  public void handleFraudDetected(
      @Payload FraudDetectedEvent event, Acknowledgment acknowledgment) {
    try {
      log.warn(
          "Received fraud.detected event: {} - Severity: {}",
          event.getTransactionId(),
          event.getSeverity());

      // Send fraud alert using template
      Map<String, Object> templateData = new HashMap<>();
      templateData.put("fraudType", event.getFraudType());
      templateData.put("severity", event.getSeverity());
      templateData.put("transactionId", event.getTransactionId());
      templateData.put("detectedAt", LocalDateTime.now());
      templateData.put("description", event.getDescription());

      // Lookup user contact info from NotificationPreference
      Long userId = event.getUserId();
      String email = getUserEmail(userId);

      if (email != null) {
        notificationService.sendTemplatedEmail(userId, email, "fraud-alert-email", templateData);
      } else {
        log.warn("No email found for user: {}, skipping fraud alert for user: {}", userId);
      }

      acknowledgment.acknowledge();

    } catch (Exception e) {
      log.error("Failed to process fraud.detected event: {}", event.getEventId(), e);
      acknowledgment.acknowledge();
    }
  }

  @KafkaListener(topics = KafkaTopics.CRYPTO_TRADE_EXECUTED)
  public void handleCryptoTradeExecuted(
      @Payload CryptoTradeExecutedEvent event, Acknowledgment acknowledgment) {
    try {
      log.info("Received crypto.trade.executed event: {}", event.getTradeId());

      // Send trade confirmation
      String message = String.format(
          """
              Your crypto trade has been executed successfully.
              Trade ID: %s
              Type: %s
              Cryptocurrency: %s
              Amount: %s
              Price: %s %s
              """,
          event.getTradeId(),
          event.getTradeType(),
          event.getCryptoCurrency(),
          event.getCryptoAmount(),
          event.getFiatAmount(),
          event.getFiatCurrency());

      // Lookup user contact info from NotificationPreference
      Long userId = event.getUserId();
      String email = getUserEmail(userId);

      if (email != null) {
        notificationService.sendSimpleEmail(
            userId,
            email,
            "Crypto Trade Executed",
            message,
            Notification.NotificationType.CRYPTO_TRADE);
      } else {
        log.warn(
            "No email found for user: {}, skipping crypto trade notification", userId);
      }

      acknowledgment.acknowledge();

    } catch (Exception e) {
      log.error("Failed to process crypto.trade.executed event: {}", event.getEventId(), e);
      acknowledgment.acknowledge();
    }
  }

  @KafkaListener(topics = KafkaTopics.ALERT_TRIGGERED)
  public void handleAlertTriggered(
      @Payload AlertTriggeredEvent event, Acknowledgment acknowledgment) {
    try {
      log.info(
          "Received alert.triggered event: {} - Type: {}",
          event.getAlertId(),
          event.getAlertType());

      // Send alert notification using template
      Map<String, Object> templateData = new HashMap<>();
      templateData.put("subject", "Alert: " + event.getAlertType());
      templateData.put("alertType", event.getAlertType());
      templateData.put("message", event.getMessage());

      // Lookup user contact info from NotificationPreference
      Long userId = event.getUserId();
      String email = getUserEmail(userId);

      if (email != null) {
        notificationService.sendTemplatedEmail(userId, email, "alert-email", templateData);
      } else {
        log.warn("No email found for user: {}, skipping alert notification", userId);
      }

      acknowledgment.acknowledge();

    } catch (Exception e) {
      log.error("Failed to process alert.triggered event: {}", event.getEventId(), e);
      acknowledgment.acknowledge();
    }
  }

  /** Helper method to format account ID for display */
  private String formatAccountId(Long accountId) {
    if (accountId == null) {
      return "****";
    }
    String id = accountId.toString();
    if (id.length() > 4) {
      return "****" + id.substring(id.length() - 4);
    }
    return "****" + id;
  }

  /**
   * Helper method to get user's email address from NotificationPreference.
   *
   * @param userId The user ID
   * @return Email address or null if not found
   */
  private String getUserEmail(Long userId) {
    return preferenceRepository
        .findByUserId(userId)
        .map(NotificationPreference::getEmailAddress)
        .orElse(null);
  }

  /**
   * Helper method to get user's phone number from NotificationPreference.
   *
   * @param userId The user ID
   * @return Phone number or null if not found
   */
  private String getUserPhone(Long userId) {
    return preferenceRepository
        .findByUserId(userId)
        .map(NotificationPreference::getPhoneNumber)
        .orElse(null);
  }
}

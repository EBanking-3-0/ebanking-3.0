package com.ebanking.notification.consumer;

import com.ebanking.shared.kafka.KafkaTopics;
import com.ebanking.shared.kafka.events.*;
import com.ebanking.shared.kafka.producer.TypedEventProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * Consumer for notification-related events. Sends notifications (email/SMS/push) when events occur.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationConsumer {

  private final TypedEventProducer eventProducer;

  @KafkaListener(topics = KafkaTopics.USER_CREATED)
  public void handleUserCreated(@Payload UserCreatedEvent event, Acknowledgment acknowledgment) {
    try {
      log.info("Received user.created event for user: {}", event.getUserId());

      // Send welcome email
      sendWelcomeEmail(event.getEmail(), event.getFirstName());

      // Publish notification sent event
      NotificationSentEvent notificationEvent =
          NotificationSentEvent.builder()
              .userId(event.getUserId())
              .recipient(event.getEmail())
              .notificationType("EMAIL")
              .subject("Welcome to E-Banking")
              .status("SENT")
              .channel("EMAIL")
              .source("notification-service")
              .build();

      eventProducer.publishNotificationSent(notificationEvent);

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

      // Send transaction notification
      sendTransactionNotification(event.getToAccountId(), event.getAmount(), event.getCurrency());

      NotificationSentEvent notificationEvent =
          NotificationSentEvent.builder()
              .userId(String.valueOf(event.getToAccountId())) // Using account ID as placeholder
              .recipient("user@example.com") // Would get from account
              .notificationType("EMAIL")
              .subject("Transaction Completed")
              .status("SENT")
              .channel("EMAIL")
              .source("notification-service")
              .build();

      eventProducer.publishNotificationSent(notificationEvent);

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

      // Send failure notification
      sendFailureNotification(event.getAccountId(), event.getFailureReason());

      NotificationSentEvent notificationEvent =
          NotificationSentEvent.builder()
              .userId(String.valueOf(event.getAccountId()))
              .recipient("user@example.com")
              .notificationType("EMAIL")
              .subject("Payment Failed")
              .status("SENT")
              .channel("EMAIL")
              .source("notification-service")
              .build();

      eventProducer.publishNotificationSent(notificationEvent);

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

      // Send fraud alert
      sendFraudAlert(event.getAccountId(), event.getFraudType(), event.getSeverity());

      NotificationSentEvent notificationEvent =
          NotificationSentEvent.builder()
              .userId(String.valueOf(event.getAccountId()))
              .recipient("user@example.com")
              .notificationType("EMAIL")
              .subject("Fraud Alert")
              .status("SENT")
              .channel("EMAIL")
              .source("notification-service")
              .build();

      eventProducer.publishNotificationSent(notificationEvent);

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
      sendTradeConfirmation(event.getUserId(), event.getCryptoCurrency(), event.getTradeType());

      NotificationSentEvent notificationEvent =
          NotificationSentEvent.builder()
              .userId(event.getUserId())
              .recipient("user@example.com")
              .notificationType("EMAIL")
              .subject("Crypto Trade Executed")
              .status("SENT")
              .channel("EMAIL")
              .source("notification-service")
              .build();

      eventProducer.publishNotificationSent(notificationEvent);

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

      // Send alert notification
      sendAlertNotification(event.getUserId(), event.getAlertType(), event.getMessage());

      NotificationSentEvent notificationEvent =
          NotificationSentEvent.builder()
              .userId(event.getUserId())
              .recipient("user@example.com")
              .notificationType("EMAIL")
              .subject("Alert: " + event.getAlertType())
              .status("SENT")
              .channel("EMAIL")
              .source("notification-service")
              .build();

      eventProducer.publishNotificationSent(notificationEvent);

      acknowledgment.acknowledge();

    } catch (Exception e) {
      log.error("Failed to process alert.triggered event: {}", event.getEventId(), e);
      acknowledgment.acknowledge();
    }
  }

  // Placeholder methods - would be implemented with actual email/SMS/push services
  private void sendWelcomeEmail(String email, String firstName) {
    log.info("Sending welcome email to: {} for user: {}", email, firstName);
    // Implementation would use email service
  }

  private void sendTransactionNotification(
      Long accountId, java.math.BigDecimal amount, String currency) {
    log.info(
        "Sending transaction notification for account: {} - Amount: {} {}",
        accountId,
        amount,
        currency);
    // Implementation would use notification service
  }

  private void sendFailureNotification(Long accountId, String reason) {
    log.info("Sending failure notification for account: {} - Reason: {}", accountId, reason);
    // Implementation would use notification service
  }

  private void sendFraudAlert(Long accountId, String fraudType, String severity) {
    log.warn(
        "Sending fraud alert for account: {} - Type: {} - Severity: {}",
        accountId,
        fraudType,
        severity);
    // Implementation would use notification service
  }

  private void sendTradeConfirmation(String userId, String cryptoCurrency, String tradeType) {
    log.info(
        "Sending trade confirmation for user: {} - Crypto: {} - Type: {}",
        userId,
        cryptoCurrency,
        tradeType);
    // Implementation would use notification service
  }

  private void sendAlertNotification(String userId, String alertType, String message) {
    log.info(
        "Sending alert notification for user: {} - Type: {} - Message: {}",
        userId,
        alertType,
        message);
    // Implementation would use notification service
  }
}

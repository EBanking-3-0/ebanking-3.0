package com.ebanking.notification.consumer;

import com.ebanking.notification.client.AccountServiceClient;
import com.ebanking.notification.client.PaymentServiceClient;
import com.ebanking.notification.enums.NotificationType;
import com.ebanking.notification.service.NotificationService;
import com.ebanking.notification.service.PreferenceService;
import com.ebanking.shared.kafka.events.AccountCreatedEvent;
import com.ebanking.shared.kafka.events.AlertTriggeredEvent;
import com.ebanking.shared.kafka.events.AuthLoginEvent;
import com.ebanking.shared.kafka.events.FraudDetectedEvent;
import com.ebanking.shared.kafka.events.PaymentFailedEvent;
import com.ebanking.shared.kafka.events.TransactionCompletedEvent;
import com.ebanking.shared.kafka.events.UserCreatedEvent;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * Kafka consumer for processing events and sending notifications. Listens to various topics and
 * triggers notifications based on event types.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationConsumer {

  private final NotificationService notificationService;
  private final PreferenceService preferenceService;
  private final AccountServiceClient accountServiceClient;
  private final PaymentServiceClient paymentServiceClient;

  /**
   * Handle user created events - send welcome notification.
   *
   * @param event User created event
   * @param acknowledgment Kafka acknowledgment
   */
  @KafkaListener(
      topics = "user.created",
      groupId = "${spring.kafka.consumer.group-id}",
      containerFactory = "kafkaListenerContainerFactory")
  public void handleUserCreated(
      @Payload UserCreatedEvent event,
      @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
      Acknowledgment acknowledgment) {

    try {
      log.info("Received UserCreatedEvent for user: {} from topic: {}", event.getUserId(), topic);

      // Initialize default preferences for new user
      preferenceService.initializeDefaultPreferences(event.getUserId());

      // Prepare template variables
      Map<String, Object> variables = new HashMap<>();
      variables.put("username", event.getUsername());
      variables.put("firstName", event.getFirstName());
      variables.put("lastName", event.getLastName());
      variables.put("email", event.getEmail());

      // Send welcome notification to all enabled channels
      notificationService.sendToAllChannels(event.getUserId(), NotificationType.WELCOME, variables);

      acknowledgment.acknowledge();
      log.info("Successfully processed UserCreatedEvent for user: {}", event.getUserId());

    } catch (Exception e) {
      log.error("Error processing UserCreatedEvent for user: {}", event.getUserId(), e);
      // Don't acknowledge to retry
    }
  }

  /**
   * Handle transaction completed events - send transaction notification.
   *
   * @param event Transaction completed event
   * @param acknowledgment Kafka acknowledgment
   */
  @KafkaListener(
      topics = "transaction.completed",
      groupId = "${spring.kafka.consumer.group-id}",
      containerFactory = "kafkaListenerContainerFactory")
  public void handleTransactionCompleted(
      @Payload TransactionCompletedEvent event,
      @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
      Acknowledgment acknowledgment) {

    try {
      log.info(
          "Received TransactionCompletedEvent for transaction: {} from topic: {}",
          event.getTransactionId(),
          topic);

      // Get account owner ID from account service
      String userId = accountServiceClient.getAccountOwnerId(event.getFromAccountId());

      // Prepare template variables
      Map<String, Object> variables = new HashMap<>();
      variables.put("transactionId", event.getTransactionId());
      variables.put("amount", formatCurrency(event.getAmount(), event.getCurrency()));
      variables.put("currency", event.getCurrency());
      variables.put("transactionType", event.getTransactionType());
      variables.put("fromAccount", event.getFromAccountNumber());
      variables.put("toAccount", event.getToAccountNumber());
      variables.put("description", event.getDescription());
      variables.put("status", event.getStatus());

      // Send transaction notification to all enabled channels
      notificationService.sendToAllChannels(userId, NotificationType.TRANSACTION, variables);

      acknowledgment.acknowledge();
      log.info(
          "Successfully processed TransactionCompletedEvent for transaction: {}",
          event.getTransactionId());

    } catch (Exception e) {
      log.error(
          "Error processing TransactionCompletedEvent for transaction: {}",
          event.getTransactionId(),
          e);
    }
  }

  /**
   * Handle fraud detected events - send urgent fraud alert.
   *
   * @param event Fraud detected event
   * @param acknowledgment Kafka acknowledgment
   */
  @KafkaListener(
      topics = "fraud.detected",
      groupId = "${spring.kafka.consumer.group-id}",
      containerFactory = "kafkaListenerContainerFactory")
  public void handleFraudDetected(
      @Payload FraudDetectedEvent event,
      @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
      Acknowledgment acknowledgment) {

    try {
      log.info("Received FraudDetectedEvent for user: {} from topic: {}", event.getUserId(), topic);

      // Prepare template variables
      Map<String, Object> variables = new HashMap<>();
      variables.put("transactionId", event.getTransactionId());
      variables.put("amount", formatCurrency(event.getAmount(), event.getCurrency()));
      variables.put("currency", event.getCurrency());
      variables.put("fraudType", event.getFraudType());
      variables.put("severity", event.getSeverity());
      variables.put("description", event.getDescription());
      variables.put("accountId", event.getAccountId());

      // Send fraud alert to ALL channels (critical priority)
      notificationService.sendToAllChannels(
          event.getUserId(), NotificationType.FRAUD_ALERT, variables);

      acknowledgment.acknowledge();
      log.info("Successfully processed FraudDetectedEvent for user: {}", event.getUserId());

    } catch (Exception e) {
      log.error("Error processing FraudDetectedEvent for user: {}", event.getUserId(), e);
    }
  }

  /**
   * Handle alert triggered events - send account alerts.
   *
   * @param event Alert triggered event
   * @param acknowledgment Kafka acknowledgment
   */
  @KafkaListener(
      topics = "alert.triggered",
      groupId = "${spring.kafka.consumer.group-id}",
      containerFactory = "kafkaListenerContainerFactory")
  public void handleAlertTriggered(
      @Payload AlertTriggeredEvent event,
      @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
      Acknowledgment acknowledgment) {

    try {
      log.info(
          "Received AlertTriggeredEvent for user: {} from topic: {}", event.getUserId(), topic);

      // Prepare template variables
      Map<String, Object> variables = new HashMap<>();
      variables.put("alertType", event.getAlertType());
      variables.put("severity", event.getSeverity());
      variables.put("message", event.getMessage());
      variables.put("threshold", formatCurrency(event.getThreshold(), "USD"));
      variables.put("currentValue", formatCurrency(event.getCurrentValue(), "USD"));
      variables.put("accountNumber", event.getAccountNumber());

      // Send alert notification
      notificationService.sendToAllChannels(event.getUserId(), NotificationType.ALERT, variables);

      acknowledgment.acknowledge();
      log.info("Successfully processed AlertTriggeredEvent for user: {}", event.getUserId());

    } catch (Exception e) {
      log.error("Error processing AlertTriggeredEvent for user: {}", event.getUserId(), e);
    }
  }

  /**
   * Handle account created events - send account creation notification.
   *
   * @param event Account created event
   * @param acknowledgment Kafka acknowledgment
   */
  @KafkaListener(
      topics = "account.created",
      groupId = "${spring.kafka.consumer.group-id}",
      containerFactory = "kafkaListenerContainerFactory")
  public void handleAccountCreated(
      @Payload AccountCreatedEvent event,
      @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
      Acknowledgment acknowledgment) {

    try {
      log.info(
          "Received AccountCreatedEvent for account: {} from topic: {}",
          event.getAccountId(),
          topic);

      // Prepare template variables
      Map<String, Object> variables = new HashMap<>();
      variables.put("accountNumber", event.getAccountNumber());
      variables.put("accountType", event.getAccountType());
      variables.put("currency", event.getCurrency());
      variables.put("balance", formatCurrency(event.getInitialBalance(), event.getCurrency()));

      // Send account creation notification
      notificationService.sendToAllChannels(
          event.getUserId(), NotificationType.ACCOUNT_CREATED, variables);

      acknowledgment.acknowledge();
      log.info("Successfully processed AccountCreatedEvent for account: {}", event.getAccountId());

    } catch (Exception e) {
      log.error("Error processing AccountCreatedEvent for account: {}", event.getAccountId(), e);
    }
  }

  /**
   * Handle payment failed events - send payment failure notification.
   *
   * @param event Payment failed event
   * @param acknowledgment Kafka acknowledgment
   */
  @KafkaListener(
      topics = "payment.failed",
      groupId = "${spring.kafka.consumer.group-id}",
      containerFactory = "kafkaListenerContainerFactory")
  public void handlePaymentFailed(
      @Payload PaymentFailedEvent event,
      @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
      Acknowledgment acknowledgment) {

    try {
      log.info(
          "Received PaymentFailedEvent for transaction: {} from topic: {}",
          event.getTransactionId(),
          topic);

      // Prepare template variables
      Map<String, Object> variables = new HashMap<>();
      variables.put("paymentId", event.getTransactionId());
      variables.put("amount", formatCurrency(event.getAmount(), event.getCurrency()));
      variables.put("currency", event.getCurrency());
      variables.put("reason", event.getFailureReason());
      variables.put("errorCode", event.getErrorCode());
      variables.put("accountNumber", event.getAccountNumber());

      // Send payment failed notification
      notificationService.sendToAllChannels(
          event.getUserId(), NotificationType.PAYMENT_FAILED, variables);

      acknowledgment.acknowledge();
      log.info(
          "Successfully processed PaymentFailedEvent for transaction: {}",
          event.getTransactionId());

    } catch (Exception e) {
      log.error(
          "Error processing PaymentFailedEvent for transaction: {}", event.getTransactionId(), e);
    }
  }

  /**
   * Handle auth login events - send login notification.
   *
   * @param event Auth login event
   * @param acknowledgment Kafka acknowledgment
   */
  @KafkaListener(
      topics = "auth.login",
      groupId = "${spring.kafka.consumer.group-id}",
      containerFactory = "kafkaListenerContainerFactory")
  public void handleAuthLogin(
      @Payload AuthLoginEvent event,
      @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
      Acknowledgment acknowledgment) {

    try {
      log.info("Received AuthLoginEvent for user: {} from topic: {}", event.getUserId(), topic);

      // Prepare template variables
      Map<String, Object> variables = new HashMap<>();
      variables.put("ipAddress", event.getIpAddress());
      variables.put("userAgent", event.getUserAgent());
      variables.put("loginMethod", event.getLoginMethod());
      variables.put("email", event.getEmail());
      variables.put("success", event.isSuccess());

      // Send login notification (usually only in-app by default)
      notificationService.sendToAllChannels(event.getUserId(), NotificationType.LOGIN, variables);

      acknowledgment.acknowledge();
      log.info("Successfully processed AuthLoginEvent for user: {}", event.getUserId());

    } catch (Exception e) {
      log.error("Error processing AuthLoginEvent for user: {}", event.getUserId(), e);
    }
  }

  /**
   * Format currency amount with symbol.
   *
   * @param amount Amount
   * @param currency Currency code
   * @return Formatted string
   */
  private String formatCurrency(BigDecimal amount, String currency) {
    if (amount == null) {
      return "0.00";
    }
    return String.format("%s %.2f", currency, amount);
  }
}

package com.ebanking.shared.kafka.producer;

import com.ebanking.shared.kafka.KafkaTopics;
import com.ebanking.shared.kafka.events.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Type-safe event producer that automatically maps event types to topics. Provides convenience
 * methods for publishing specific event types.
 *
 * <p>This class is automatically configured via KafkaEventsAutoConfiguration. We can also create it
 * manually if needed.
 */
@Slf4j
@RequiredArgsConstructor
public class TypedEventProducer {

  private final EventProducer eventProducer;

  public void publishUserCreated(UserCreatedEvent event) {
    eventProducer.publishEvent(KafkaTopics.USER_CREATED, event);
  }

  public void publishUserUpdated(UserUpdatedEvent event) {
    eventProducer.publishEvent(KafkaTopics.USER_UPDATED, event);
  }

  public void publishAccountCreated(AccountCreatedEvent event) {
    eventProducer.publishEvent(KafkaTopics.ACCOUNT_CREATED, event);
  }

  public void publishBalanceUpdated(BalanceUpdatedEvent event) {
    eventProducer.publishEvent(KafkaTopics.BALANCE_UPDATED, event);
  }

  public void publishTransactionCompleted(TransactionCompletedEvent event) {
    eventProducer.publishEvent(KafkaTopics.TRANSACTION_COMPLETED, event);
  }

  public void publishPaymentFailed(PaymentFailedEvent event) {
    eventProducer.publishEvent(KafkaTopics.PAYMENT_FAILED, event);
  }

  public void publishFraudDetected(FraudDetectedEvent event) {
    eventProducer.publishEvent(KafkaTopics.FRAUD_DETECTED, event);
  }

  public void publishAuthLogin(AuthLoginEvent event) {
    eventProducer.publishEvent(KafkaTopics.AUTH_LOGIN, event);
  }

  public void publishMfaVerified(MfaVerifiedEvent event) {
    eventProducer.publishEvent(KafkaTopics.MFA_VERIFIED, event);
  }

  public void publishCryptoTradeExecuted(CryptoTradeExecutedEvent event) {
    eventProducer.publishEvent(KafkaTopics.CRYPTO_TRADE_EXECUTED, event);
  }

  public void publishNotificationSent(NotificationSentEvent event) {
    eventProducer.publishEvent(KafkaTopics.NOTIFICATION_SENT, event);
  }

  public void publishNotificationFailed(NotificationFailedEvent event) {
    eventProducer.publishEvent(KafkaTopics.NOTIFICATION_FAILED, event);
  }

  public void publishAlertTriggered(AlertTriggeredEvent event) {
    eventProducer.publishEvent(KafkaTopics.ALERT_TRIGGERED, event);
  }
}

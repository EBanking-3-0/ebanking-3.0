package com.ebanking.analytics.consumer;

import com.ebanking.shared.kafka.KafkaTopics;
import com.ebanking.shared.kafka.events.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/** Consumer for analytics events. Aggregates data for dashboards and metrics. */
@Slf4j
@Component
@RequiredArgsConstructor
public class AnalyticsConsumer {

  // Would inject AnalyticsService here
  // private final AnalyticsService analyticsService;

  @KafkaListener(topics = KafkaTopics.USER_CREATED)
  public void handleUserCreated(@Payload UserCreatedEvent event, Acknowledgment acknowledgment) {
    try {
      log.info("Received user.created event for analytics: {}", event.getUserId());
      // Aggregate user growth metrics
      acknowledgment.acknowledge();
    } catch (Exception e) {
      log.error("Failed to process user.created event: {}", event.getEventId(), e);
      acknowledgment.acknowledge();
    }
  }

  @KafkaListener(topics = KafkaTopics.ACCOUNT_CREATED)
  public void handleAccountCreated(
      @Payload AccountCreatedEvent event, Acknowledgment acknowledgment) {
    try {
      log.info("Received account.created event for analytics: {}", event.getAccountId());
      // Aggregate account creation metrics
      acknowledgment.acknowledge();
    } catch (Exception e) {
      log.error("Failed to process account.created event: {}", event.getEventId(), e);
      acknowledgment.acknowledge();
    }
  }

  @KafkaListener(topics = KafkaTopics.TRANSACTION_COMPLETED)
  public void handleTransactionCompleted(
      @Payload TransactionCompletedEvent event, Acknowledgment acknowledgment) {
    try {
      log.info("Received transaction.completed event for analytics: {}", event.getTransactionId());
      // Aggregate transaction data for dashboards
      acknowledgment.acknowledge();
    } catch (Exception e) {
      log.error("Failed to process transaction.completed event: {}", event.getEventId(), e);
      acknowledgment.acknowledge();
    }
  }

  @KafkaListener(topics = KafkaTopics.CRYPTO_TRADE_EXECUTED)
  public void handleCryptoTradeExecuted(
      @Payload CryptoTradeExecutedEvent event, Acknowledgment acknowledgment) {
    try {
      log.info("Received crypto.trade.executed event for analytics: {}", event.getTradeId());
      // Track crypto activity metrics
      acknowledgment.acknowledge();
    } catch (Exception e) {
      log.error("Failed to process crypto.trade.executed event: {}", event.getEventId(), e);
      acknowledgment.acknowledge();
    }
  }
}

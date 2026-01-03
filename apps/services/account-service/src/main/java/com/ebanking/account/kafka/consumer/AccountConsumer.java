package com.ebanking.account.kafka.consumer;

import com.ebanking.shared.kafka.KafkaTopics;
import com.ebanking.shared.kafka.events.TransactionCompletedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/** Consumer for transaction events. Updates account balances when transactions complete. */
@Slf4j
@Component
@RequiredArgsConstructor
public class AccountConsumer {

  // Would inject AccountService here
  // private final AccountService accountService;

  @KafkaListener(topics = KafkaTopics.TRANSACTION_COMPLETED)
  public void handleTransactionCompleted(
      @Payload TransactionCompletedEvent event, Acknowledgment acknowledgment) {
    try {
      log.info("Received transaction.completed event: {}", event.getTransactionId());

      // Update account balances based on transaction
      // accountService.updateBalance(...);

      log.info("Processed transaction.completed event: {}", event.getTransactionId());
      acknowledgment.acknowledge();

    } catch (Exception e) {
      log.error("Failed to process transaction.completed event: {}", event.getEventId(), e);
      acknowledgment.acknowledge(); // Acknowledge to prevent blocking
    }
  }
}

package com.ebanking.payment.service;

import com.ebanking.shared.kafka.events.FraudDetectedEvent;
import com.ebanking.shared.kafka.events.PaymentFailedEvent;
import com.ebanking.shared.kafka.events.TransactionCompletedEvent;
import com.ebanking.shared.kafka.producer.TypedEventProducer;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Payment service with Kafka event publishing. Publishes transaction.completed, payment.failed, and
 * fraud.detected events.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

  private final TypedEventProducer eventProducer;

  @Transactional
  public void completeTransaction(
      Long transactionId,
      Long fromAccountId,
      Long toAccountId,
      String fromAccountNumber,
      String toAccountNumber,
      BigDecimal amount,
      String currency,
      String transactionType) {
    // Transaction completion logic would go here
    log.info("Completing transaction: {}", transactionId);

    // After transaction completes, publish event
    TransactionCompletedEvent event =
        TransactionCompletedEvent.builder()
            .transactionId(transactionId)
            .fromAccountId(fromAccountId)
            .toAccountId(toAccountId)
            .fromAccountNumber(fromAccountNumber)
            .toAccountNumber(toAccountNumber)
            .amount(amount)
            .currency(currency)
            .transactionType(transactionType)
            .status("COMPLETED")
            .description("Transaction completed successfully")
            .source("payment-service")
            .build();

    eventProducer.publishTransactionCompleted(event);
    log.info("Published transaction.completed event: {}", transactionId);
  }

  @Transactional
  public void handlePaymentFailure(
      Long transactionId,
      Long accountId,
      String accountNumber,
      BigDecimal amount,
      String currency,
      String failureReason,
      String errorCode) {
    // Payment failure handling logic would go here
    log.warn("Payment failed for transaction: {} - Reason: {}", transactionId, failureReason);

    // Publish payment failed event
    PaymentFailedEvent event =
        PaymentFailedEvent.builder()
            .transactionId(transactionId)
            .accountId(accountId)
            .accountNumber(accountNumber)
            .amount(amount)
            .currency(currency)
            .failureReason(failureReason)
            .errorCode(errorCode)
            .source("payment-service")
            .build();

    eventProducer.publishPaymentFailed(event);
    log.info("Published payment.failed event: {}", transactionId);
  }

  @Transactional
  public void detectFraud(
      Long transactionId,
      Long accountId,
      String accountNumber,
      BigDecimal amount,
      String currency,
      String fraudType,
      String severity,
      String description) {
    // Fraud detection logic would go here
    log.warn(
        "Fraud detected for transaction: {} - Type: {} - Severity: {}",
        transactionId,
        fraudType,
        severity);

    // Publish fraud detected event
    FraudDetectedEvent event =
        FraudDetectedEvent.builder()
            .transactionId(transactionId)
            .accountId(accountId)
            .accountNumber(accountNumber)
            .amount(amount)
            .currency(currency)
            .fraudType(fraudType)
            .severity(severity)
            .description(description)
            .source("payment-service")
            .build();

    eventProducer.publishFraudDetected(event);
    log.info("Published fraud.detected event: {}", transactionId);
  }
}

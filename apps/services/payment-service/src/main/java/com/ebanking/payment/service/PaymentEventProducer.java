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

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentEventProducer {

  private final TypedEventProducer eventProducer;

  @Transactional
  public void completeTransaction(
      Long paymentId,
      String userId,
      Long fromAccountId,
      Long toAccountId,
      String fromIban,
      String toIban,
      BigDecimal amount,
      String currency,
      String transactionType) {

    log.info("Publishing TransactionCompletedEvent for flow: {} -> {}", fromIban, toIban);

    try {
      TransactionCompletedEvent event =
          TransactionCompletedEvent.builder()
              .transactionId(paymentId)
              .userId(userId)
              .fromAccountId(fromAccountId)
              .toAccountId(toAccountId)
              .fromAccountNumber(fromIban)
              .toAccountNumber(toIban)
              .amount(amount)
              .currency(currency)
              .transactionType(transactionType)
              .status("COMPLETED")
              .description("Banking Transaction Finalized")
              .source("payment-service")
              .build();

      eventProducer.publishTransactionCompleted(event);
    } catch (Exception e) {
      log.error(
          "Failed to publish TransactionCompletedEvent for payment {}: {}",
          paymentId,
          e.getMessage());
      // Ne pas faire échouer le paiement si Kafka est indisponible
    }
  }

  @Transactional
  public void handlePaymentFailure(
      Long paymentId,
      String userId,
      Long accountId,
      String iban,
      BigDecimal amount,
      String currency,
      String failureReason,
      String errorCode) {

    log.warn("Publishing PaymentFailedEvent for payment ID: {} - Error: {}", paymentId, errorCode);

    try {
      PaymentFailedEvent event =
          PaymentFailedEvent.builder()
              .transactionId(paymentId)
              .userId(userId)
              .accountId(accountId)
              .accountNumber(iban)
              .amount(amount)
              .currency(currency)
              .failureReason(failureReason)
              .errorCode(errorCode)
              .source("payment-service")
              .build();

      eventProducer.publishPaymentFailed(event);
    } catch (Exception e) {
      log.error(
          "Failed to publish PaymentFailedEvent for payment {}: {}", paymentId, e.getMessage());
      // Ne pas faire échouer le traitement si Kafka est indisponible
    }
  }

  @Transactional
  public void detectFraud(
      Long paymentId,
      String userId,
      Long accountId,
      String iban,
      BigDecimal amount,
      String currency,
      String fraudType,
      String severity,
      String description) {

    log.error("Publishing FraudDetectedEvent (CRITICAL) for payment ID: {}", paymentId);

    try {
      FraudDetectedEvent event =
          FraudDetectedEvent.builder()
              .transactionId(paymentId)
              .userId(userId)
              .accountId(accountId)
              .accountNumber(iban)
              .amount(amount)
              .currency(currency)
              .fraudType(fraudType)
              .severity(severity)
              .description(description)
              .source("payment-service")
              .build();

      eventProducer.publishFraudDetected(event);
    } catch (Exception e) {
      log.error(
          "Failed to publish FraudDetectedEvent for payment {}: {}", paymentId, e.getMessage());
      // Ne pas faire échouer le traitement si Kafka est indisponible
    }
  }
}

package com.ebanking.shared.kafka.events;

import com.ebanking.shared.kafka.KafkaTopics;
import java.math.BigDecimal;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

/**
 * Event published when a transaction is successfully completed. Published by: Payment Service
 * Consumed by: Notification Service, Analytics Service, Account Service, Audit Service
 */
@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class TransactionCompletedEvent extends BaseEvent {

  private Long transactionId;
  private Long userId;
  private Long fromAccountId;
  private Long toAccountId;
  private String fromAccountNumber;
  private String toAccountNumber;
  private BigDecimal amount;
  private String currency;
  private String transactionType; // TRANSFER, PAYMENT, WITHDRAWAL, DEPOSIT
  private String status;
  private String description;

  public TransactionCompletedEvent() {
    super(KafkaTopics.TRANSACTION_COMPLETED);
  }

  public TransactionCompletedEvent(
      Long transactionId,
      Long userId,
      Long fromAccountId,
      Long toAccountId,
      String fromAccountNumber,
      String toAccountNumber,
      BigDecimal amount,
      String currency,
      String transactionType,
      String status,
      String description) {
    super(KafkaTopics.TRANSACTION_COMPLETED);
    this.transactionId = transactionId;
    this.userId = userId;
    this.fromAccountId = fromAccountId;
    this.toAccountId = toAccountId;
    this.fromAccountNumber = fromAccountNumber;
    this.toAccountNumber = toAccountNumber;
    this.amount = amount;
    this.currency = currency;
    this.transactionType = transactionType;
    this.status = status;
    this.description = description;
  }
}

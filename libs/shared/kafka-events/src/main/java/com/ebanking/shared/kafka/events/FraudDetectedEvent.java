package com.ebanking.shared.kafka.events;

import com.ebanking.shared.kafka.KafkaTopics;
import java.math.BigDecimal;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

/**
 * Event published when fraud is detected in a transaction. Published by: Payment Service Consumed
 * by: Notification Service, Audit Service
 */
@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class FraudDetectedEvent extends BaseEvent {

  private Long transactionId;
  private Long userId;
  private Long accountId;
  private String accountNumber;
  private BigDecimal amount;
  private String currency;
  private String fraudType; // SUSPICIOUS_AMOUNT, UNUSUAL_LOCATION, MULTIPLE_FAILED_ATTEMPTS, etc.
  private String severity; // LOW, MEDIUM, HIGH, CRITICAL
  private String description;

  public FraudDetectedEvent() {
    super(KafkaTopics.FRAUD_DETECTED);
  }

  public FraudDetectedEvent(
      Long transactionId,
      Long userId,
      Long accountId,
      String accountNumber,
      BigDecimal amount,
      String currency,
      String fraudType,
      String severity,
      String description) {
    super(KafkaTopics.FRAUD_DETECTED);
    this.transactionId = transactionId;
    this.userId = userId;
    this.accountId = accountId;
    this.accountNumber = accountNumber;
    this.amount = amount;
    this.currency = currency;
    this.fraudType = fraudType;
    this.severity = severity;
    this.description = description;
  }
}

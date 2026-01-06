package com.ebanking.shared.kafka.events;

import com.ebanking.shared.kafka.KafkaTopics;
import java.math.BigDecimal;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

/**
 * Event published when a payment transaction fails. Published by: Payment Service Consumed by:
 * Notification Service, Audit Service
 */
@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class PaymentFailedEvent extends BaseEvent {

  private Long transactionId;
  private String userId;
  private Long accountId;
  private String accountNumber;
  private BigDecimal amount;
  private String currency;
  private String failureReason;
  private String errorCode;

  public PaymentFailedEvent() {
    super(KafkaTopics.PAYMENT_FAILED);
  }

  public PaymentFailedEvent(
      Long transactionId,
      String userId,
      Long accountId,
      BigDecimal amount,
      String currency,
      String failureReason,
      String errorCode) {
    super(KafkaTopics.PAYMENT_FAILED);
    this.transactionId = transactionId;
    this.userId = userId;
    this.accountId = accountId;
    this.amount = amount;
    this.currency = currency;
    this.failureReason = failureReason;
    this.errorCode = errorCode;
  }
}

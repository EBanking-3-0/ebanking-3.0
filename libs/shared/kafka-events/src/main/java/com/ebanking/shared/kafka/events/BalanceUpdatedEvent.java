package com.ebanking.shared.kafka.events;

import com.ebanking.shared.kafka.KafkaTopics;
import java.math.BigDecimal;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

/**
 * Event published when an account balance is updated. Published by: Account Service Consumed by:
 * Analytics Service, Audit Service
 */
@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class BalanceUpdatedEvent extends BaseEvent {

  private Long accountId;
  private String accountNumber;
  private BigDecimal previousBalance;
  private BigDecimal newBalance;
  private BigDecimal amount;
  private String operation; // DEBIT, CREDIT
  private String reason;

  public BalanceUpdatedEvent() {
    super(KafkaTopics.BALANCE_UPDATED);
  }

  public BalanceUpdatedEvent(
      Long accountId,
      String accountNumber,
      BigDecimal previousBalance,
      BigDecimal newBalance,
      BigDecimal amount,
      String operation,
      String reason) {
    super(KafkaTopics.BALANCE_UPDATED);
    this.accountId = accountId;
    this.accountNumber = accountNumber;
    this.previousBalance = previousBalance;
    this.newBalance = newBalance;
    this.amount = amount;
    this.operation = operation;
    this.reason = reason;
  }
}

package com.ebanking.shared.kafka.events;

import com.ebanking.shared.kafka.KafkaTopics;
import java.math.BigDecimal;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

/**
 * Event published when a new account is created. Published by: Account Service Consumed by: Crypto
 * Service, Analytics Service, Audit Service
 */
@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class AccountCreatedEvent extends BaseEvent {

  private Long accountId;
  private Long userId;
  private String accountNumber;
  private String accountType;
  private String currency;
  private BigDecimal initialBalance;

  public AccountCreatedEvent() {
    super(KafkaTopics.ACCOUNT_CREATED);
  }

  public AccountCreatedEvent(
      Long accountId,
      Long userId,
      String accountNumber,
      String accountType,
      String currency,
      BigDecimal initialBalance) {
    super(KafkaTopics.ACCOUNT_CREATED);
    this.accountId = accountId;
    this.userId = userId;
    this.accountNumber = accountNumber;
    this.accountType = accountType;
    this.currency = currency;
    this.initialBalance = initialBalance;
  }
}

package com.ebanking.shared.kafka.events;

import com.ebanking.shared.kafka.KafkaTopics;
import java.math.BigDecimal;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

/**
 * Event published when a cryptocurrency trade is executed. Published by: Crypto Service Consumed
 * by: Notification Service, Analytics Service, Audit Service
 */
@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class CryptoTradeExecutedEvent extends BaseEvent {

  private Long tradeId;
  private String userId;
  private Long accountId;
  private String cryptoCurrency; // BTC, ETH, etc.
  private String tradeType; // BUY, SELL
  private BigDecimal cryptoAmount;
  private BigDecimal fiatAmount;
  private String fiatCurrency;
  private BigDecimal exchangeRate;
  private String status;

  public CryptoTradeExecutedEvent() {
    super(KafkaTopics.CRYPTO_TRADE_EXECUTED);
  }

  public CryptoTradeExecutedEvent(
      Long tradeId,
      String userId,
      Long accountId,
      String cryptoCurrency,
      String tradeType,
      BigDecimal cryptoAmount,
      BigDecimal fiatAmount,
      String fiatCurrency,
      BigDecimal exchangeRate,
      String status) {
    super(KafkaTopics.CRYPTO_TRADE_EXECUTED);
    this.tradeId = tradeId;
    this.userId = userId;
    this.accountId = accountId;
    this.cryptoCurrency = cryptoCurrency;
    this.tradeType = tradeType;
    this.cryptoAmount = cryptoAmount;
    this.fiatAmount = fiatAmount;
    this.fiatCurrency = fiatCurrency;
    this.exchangeRate = exchangeRate;
    this.status = status;
  }
}

package com.ebanking.crypto.service;

import com.ebanking.shared.kafka.events.CryptoTradeExecutedEvent;
import com.ebanking.shared.kafka.producer.TypedEventProducer;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Crypto service with Kafka event publishing. Publishes crypto.trade.executed events. */
@Slf4j
@Service
@RequiredArgsConstructor
public class CryptoService {

  private final TypedEventProducer eventProducer;

  @Transactional
  public void executeTrade(
      Long tradeId,
      Long userId,
      Long accountId,
      String cryptoCurrency,
      String tradeType,
      BigDecimal cryptoAmount,
      BigDecimal fiatAmount,
      String fiatCurrency,
      BigDecimal exchangeRate,
      String status) {
    // Trade execution logic would go here
    log.info(
        "Executing crypto trade: {} - Type: {} - Amount: {} {}",
        tradeId,
        tradeType,
        cryptoAmount,
        cryptoCurrency);

    // After trade executes, publish event
    CryptoTradeExecutedEvent event =
        CryptoTradeExecutedEvent.builder()
            .tradeId(tradeId)
            .userId(userId)
            .accountId(accountId)
            .cryptoCurrency(cryptoCurrency)
            .tradeType(tradeType)
            .cryptoAmount(cryptoAmount)
            .fiatAmount(fiatAmount)
            .fiatCurrency(fiatCurrency)
            .exchangeRate(exchangeRate)
            .status(status)
            .source("crypto-service")
            .build();

    eventProducer.publishCryptoTradeExecuted(event);
    log.info("Published crypto.trade.executed event: {}", tradeId);
  }
}

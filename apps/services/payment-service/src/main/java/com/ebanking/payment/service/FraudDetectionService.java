package com.ebanking.payment.service;

import com.ebanking.payment.entity.Payment;
import com.ebanking.payment.repository.PaymentRepository;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class FraudDetectionService {

  private final PaymentRepository paymentRepository;

  @Value("${payment.fraud.high-amount-threshold:5000.00}")
  private BigDecimal highAmountThreshold;

  @Value("${payment.fraud.max-transactions-per-hour:10}")
  private int maxTransactionsPerHour;

  public FraudCheckResult checkFraud(Payment payment) {
    List<String> indicators = new ArrayList<>();

    // Montant élevé
    if (payment.getAmount().compareTo(highAmountThreshold) > 0) {
      indicators.add("HIGH_AMOUNT");
    }

    // Vélocité
    Instant oneHourAgo = Instant.now().minus(Duration.ofHours(1));
    int recentCount =
        paymentRepository.countRecentTransfers(payment.getFromAccountId(), oneHourAgo);

    if (recentCount > maxTransactionsPerHour) {
      indicators.add("HIGH_VELOCITY");
      return FraudCheckResult.blocked(indicators);
    }

    if (!indicators.isEmpty()) {
      return FraudCheckResult.requireMFA(indicators);
    }

    return FraudCheckResult.allowed();
  }

  @Data
  @lombok.Builder
  public static class FraudCheckResult {
    private boolean allowed;
    private boolean blocked;
    private boolean requiresMFA;
    private List<String> indicators;

    public static FraudCheckResult allowed() {
      return FraudCheckResult.builder()
          .allowed(true)
          .blocked(false)
          .requiresMFA(false)
          .indicators(new ArrayList<>())
          .build();
    }

    public static FraudCheckResult blocked(List<String> indicators) {
      return FraudCheckResult.builder()
          .allowed(false)
          .blocked(true)
          .requiresMFA(false)
          .indicators(indicators)
          .build();
    }

    public static FraudCheckResult requireMFA(List<String> indicators) {
      return FraudCheckResult.builder()
          .allowed(false)
          .blocked(false)
          .requiresMFA(true)
          .indicators(indicators)
          .build();
    }
  }
}

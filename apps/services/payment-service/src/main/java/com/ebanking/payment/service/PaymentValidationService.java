package com.ebanking.payment.service;

import com.ebanking.payment.dto.request.PaymentRequest;
import com.ebanking.payment.exception.DailyLimitExceededException;
import com.ebanking.payment.exception.MonthlyLimitExceededException;
import com.ebanking.payment.repository.PaymentRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentValidationService {

  private final PaymentRepository paymentRepository;
  private static final BigDecimal DAILY_LIMIT = new BigDecimal("5000.00");
  private static final BigDecimal MONTHLY_LIMIT = new BigDecimal("25000.00");

  public void validatePayment(PaymentRequest request, Long userId) {
    validateSyntax(request);
    checkLimits(userId, request.getAmount());
    performComplianceScreening(request);
    checkFraudRules(request);
  }

  private void validateSyntax(PaymentRequest request) {
    if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
      throw new IllegalArgumentException("Amount must be positive");
    }
    // Type specific syntax
    if ("SEPA_TRANSFER".equals(request.getType()) || "SCT_INSTANT".equals(request.getType())) {
      if (request.getToIban() == null
          || !request.getToIban().startsWith("FR")) { // Mock FR IBAN check
        log.warn("Non-FR IBAN detected in SEPA transfer: {}", request.getToIban());
      }
    }
  }

  public void checkLimits(Long userId, BigDecimal amount) {
    BigDecimal dailyTotal =
        paymentRepository.sumAmountByUserIdAndCreatedAtAfter(
            userId, Instant.now().minus(1, ChronoUnit.DAYS));
    if (dailyTotal.add(amount).compareTo(DAILY_LIMIT) > 0) {
      throw new DailyLimitExceededException("Daily limit of " + DAILY_LIMIT + " reached");
    }

    BigDecimal monthlyTotal =
        paymentRepository.sumAmountByUserIdAndCreatedAtAfter(
            userId, Instant.now().minus(30, ChronoUnit.DAYS));
    if (monthlyTotal.add(amount).compareTo(MONTHLY_LIMIT) > 0) {
      throw new MonthlyLimitExceededException("Monthly limit of " + MONTHLY_LIMIT + " reached");
    }
  }

  private void performComplianceScreening(PaymentRequest request) {
    log.info("Performing AML screening for beneficiary: {}", request.getBeneficiaryName());
    // Simulate AML screening against PEP/Sanction lists
    if (request.getBeneficiaryName() != null
        && request.getBeneficiaryName().contains("SANCTIONED")) {
      throw new SecurityException("Beneficiary is on a sanctions list (Compliance REJECT)");
    }
  }

  private void checkFraudRules(PaymentRequest request) {
    log.info("Checking fraud rules for payment from account: {}", request.getFromAccountId());
    // Simulate velocity check
    if (request.getAmount().compareTo(new BigDecimal("10000.00")) > 0) {
      log.warn("High value transaction detected: {}", request.getAmount());
      // In a real scenario, this would call a Fraud Service or flag for review
    }
  }
}

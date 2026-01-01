package com.ebanking.payment.service;

import com.ebanking.payment.exception.DailyLimitExceededException;
import com.ebanking.payment.exception.MonthlyLimitExceededException;
import com.ebanking.payment.repository.PaymentRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentLimitService {

  private final PaymentRepository paymentRepository;

  private static final BigDecimal DAILY_LIMIT = new BigDecimal("2000.00");
  private static final BigDecimal MONTHLY_LIMIT = new BigDecimal("10000.00");

  public void checkLimits(Long userId, BigDecimal amount) {
    checkDailyLimit(userId, amount);
    checkMonthlyLimit(userId, amount);
  }

  private void checkDailyLimit(Long userId, BigDecimal amount) {
    BigDecimal dailyTotal =
        paymentRepository.sumAmountByUserIdAndCreatedAtAfter(
            userId, Instant.now().minus(1, ChronoUnit.DAYS));

    if (dailyTotal.add(amount).compareTo(DAILY_LIMIT) > 0) {
      throw new DailyLimitExceededException("Daily payment limit of " + DAILY_LIMIT + " exceeded");
    }
  }

  private void checkMonthlyLimit(Long userId, BigDecimal amount) {
    BigDecimal monthlyTotal =
        paymentRepository.sumAmountByUserIdAndCreatedAtAfter(
            userId, Instant.now().minus(30, ChronoUnit.DAYS));

    if (monthlyTotal.add(amount).compareTo(MONTHLY_LIMIT) > 0) {
      throw new MonthlyLimitExceededException(
          "Monthly payment limit of " + MONTHLY_LIMIT + " exceeded");
    }
  }
}

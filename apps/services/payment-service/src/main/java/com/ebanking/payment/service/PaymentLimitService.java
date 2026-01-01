package com.ebanking.payment.service;

import com.ebanking.payment.exception.DailyLimitExceededException;
import com.ebanking.payment.exception.MonthlyLimitExceededException;
import com.ebanking.payment.repository.PaymentRepository;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentLimitService {

  private final PaymentRepository paymentRepository;

  @Value("${payment.limits.daily:10000.00}")
  private BigDecimal dailyLimit;

  @Value("${payment.limits.monthly:50000.00}")
  private BigDecimal monthlyLimit;

  public void checkDailyLimit(Long accountId, BigDecimal amount) {
    Instant startOfDay = Instant.now().truncatedTo(java.time.temporal.ChronoUnit.DAYS);

    BigDecimal dailySpent = paymentRepository.sumAmountSince(accountId, startOfDay);
    BigDecimal totalAfterPayment = dailySpent.add(amount);

    if (totalAfterPayment.compareTo(dailyLimit) > 0) {
      throw new DailyLimitExceededException(
          "Plafond journalier dépassé: " + dailySpent + " + " + amount + " > " + dailyLimit);
    }
  }

  public void checkMonthlyLimit(Long accountId, BigDecimal amount) {
    Instant startOfMonth =
        Instant.now().atZone(java.time.ZoneId.systemDefault()).withDayOfMonth(1).toInstant();

    BigDecimal monthlySpent = paymentRepository.sumAmountSince(accountId, startOfMonth);
    BigDecimal totalAfterPayment = monthlySpent.add(amount);

    if (totalAfterPayment.compareTo(monthlyLimit) > 0) {
      throw new MonthlyLimitExceededException("Plafond mensuel dépassé");
    }
  }
}

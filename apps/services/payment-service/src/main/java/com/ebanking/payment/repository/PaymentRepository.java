package com.ebanking.payment.repository;

import com.ebanking.payment.entity.Payment;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
  Optional<Payment> findByTransactionId(String transactionId);

  Optional<Payment> findByIdempotencyKey(String idempotencyKey);

  List<Payment> findByFromAccountId(Long fromAccountId);

  List<Payment> findByToAccountId(Long toAccountId);

  List<Payment> findByUserId(Long userId);

  @Query(
      "SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.userId = :userId AND p.createdAt >= :startDate AND p.status = 'COMPLETED'")
  BigDecimal sumAmountByUserIdAndCreatedAtAfter(Long userId, Instant startDate);
}

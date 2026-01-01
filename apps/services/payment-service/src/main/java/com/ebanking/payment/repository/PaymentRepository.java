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

  Optional<Payment> findByIdempotencyKey(String idempotencyKey);

  Optional<Payment> findByTransactionId(String transactionId);

  List<Payment> findByUserIdOrderByCreatedAtDesc(Long userId);

  @Query(
      "SELECT COUNT(p) FROM Payment p WHERE p.fromAccountId = :accountId "
          + "AND p.createdAt >= :since")
  int countRecentTransfers(Long accountId, Instant since);

  @Query(
      "SELECT COALESCE(SUM(p.amount), 0) FROM Payment p "
          + "WHERE p.userId = :userId "
          + "AND p.createdAt >= :since")
  BigDecimal sumAmountByUserIdAndCreatedAtAfter(Long userId, Instant since);

  @Query(
      "SELECT COALESCE(SUM(p.amount), 0) FROM Payment p "
          + "WHERE p.fromAccountId = :accountId "
          + "AND p.status = 'COMPLETED' "
          + "AND p.createdAt >= :since")
  BigDecimal sumAmountSince(Long accountId, Instant since);
}

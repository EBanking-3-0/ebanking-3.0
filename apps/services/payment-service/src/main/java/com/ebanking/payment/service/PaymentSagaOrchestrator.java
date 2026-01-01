package com.ebanking.payment.service;

import com.ebanking.payment.client.AccountServiceClient;
import com.ebanking.payment.client.LegacyAdapterClient;
import com.ebanking.payment.client.dto.*;
import com.ebanking.payment.dto.response.PaymentResult;
import com.ebanking.payment.entity.Payment;
import com.ebanking.payment.entity.PaymentStatus;
import com.ebanking.payment.repository.PaymentRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentSagaOrchestrator {

  private final PaymentRepository paymentRepository;
  private final AccountServiceClient accountClient;
  private final LegacyAdapterClient legacyClient;
  private final PaymentEventProducer eventProducer;

  public PaymentResult executePayment(Payment payment) {
    log.info(
        "Executing Senior Saga for {} (Type: {})",
        payment.getTransactionId(),
        payment.getPaymentType());
    try {
      // Stage 1: Funds Reservation (Blocking funds in Account Service)
      reserveFunds(payment);

      // Stage 2: Strategy Execution based on Type
      switch (payment.getPaymentType()) {
        case INTERNAL_TRANSFER -> processInternal(payment);
        case SEPA_TRANSFER -> processSepaStandard(payment);
        case SCT_INSTANT -> processSepaInstant(payment);
        case SWIFT_TRANSFER -> processSwift(payment);
        case MERCHANT_PAYMENT -> processMerchant(payment);
        case MOBILE_RECHARGE -> processMobile(payment);
        default ->
            throw new UnsupportedOperationException(
                "Type not supported: " + payment.getPaymentType());
      }

      // Stage 3: Final Settlement
      finalizePayment(payment);

      return PaymentResult.success(payment);

    } catch (Exception e) {
      log.error("Saga failure for payment {}: {}", payment.getTransactionId(), e.getMessage());
      handleGlobalFailure(payment, e);
      return PaymentResult.failure(payment, e);
    }
  }

  private void reserveFunds(Payment payment) {
    log.info("Stage: RESERVING FUNDS (Withdrawal)");
    // We use 'withdraw' to block funds. In a real Core Banking, a 'block' API would
    // be used.
    try {
      var debitResponse =
          accountClient.debit(
              payment.getFromAccountId(),
              DebitRequest.builder()
                  .amount(payment.getAmount())
                  .description(
                      "Provisioning for "
                          + payment.getPaymentType()
                          + " ["
                          + payment.getTransactionId()
                          + "]")
                  .build());
      payment.setDebitTransactionId(debitResponse.getTransactionId());
      payment.setStatus(PaymentStatus.RESERVED);
      payment.setAccountingDate(LocalDate.now());
      paymentRepository.save(payment);
    } catch (Exception e) {
      payment.setStatus(PaymentStatus.FAILED);
      payment.setFailureReason("FUNDS_RESERVATION_FAILED: " + e.getMessage());
      paymentRepository.save(payment);
      throw new RuntimeException("Funds reservation failed", e);
    }
  }

  private void processInternal(Payment payment) {
    log.info("Stage: INTERNAL CLEARING (Immediate)");
    try {
      var creditResponse =
          accountClient.credit(
              payment.getToAccountId(),
              CreditRequest.builder()
                  .amount(payment.getAmount())
                  .description("Credit from internal transfer [" + payment.getTransactionId() + "]")
                  .build());
      payment.setCreditTransactionId(creditResponse.getTransactionId());
      payment.setStatus(PaymentStatus.SETTLED);
      paymentRepository.save(payment);
    } catch (Exception e) {
      throw new RuntimeException("Internal credit failed", e);
    }
  }

  private void processSepaStandard(Payment payment) {
    log.info("Stage: SEPA SCT CLEARING (ISO 20022 pacs.008)");
    try {
      var response =
          legacyClient.initiateSepaTransfer(
              SepaTransferRequest.builder()
                  .amount(payment.getAmount())
                  .currency(payment.getCurrency())
                  .toIban(payment.getToIban())
                  .description(payment.getDescription())
                  .build());
      payment.setExternalTransactionId(response.getExternalTransactionId());
      payment.setIso20022MessageReference("MSG-SCT-" + payment.getTransactionId());
      payment.setStatus(PaymentStatus.SENT);
      paymentRepository.save(payment);
    } catch (Exception e) {
      throw new RuntimeException("SEPA SCT clearing failed", e);
    }
  }

  private void processSepaInstant(Payment payment) {
    log.info("Stage: SEPA SCT Inst CLEARING (Real-time)");
    try {
      var response =
          legacyClient.initiateInstantTransfer(
              InstantTransferRequest.builder()
                  .amount(payment.getAmount())
                  .currency(payment.getCurrency())
                  .toIban(payment.getToIban())
                  .description(payment.getDescription())
                  .build());
      payment.setExternalTransactionId(response.getExternalTransactionId());
      payment.setStatus(PaymentStatus.SETTLED); // Instant is settled immediately if ACK
      paymentRepository.save(payment);
    } catch (Exception e) {
      throw new RuntimeException("SEPA Instant clearing failed", e);
    }
  }

  private void processSwift(Payment payment) {
    log.info("Stage: SWIFT CLEARING (MT103 / pacs.008.001.09)");
    // Mock SWIFT fee logic
    payment.setFees(new BigDecimal("25.00"));
    payment.setUetr("uetr-" + UUID.randomUUID().toString());
    payment.setStatus(PaymentStatus.SENT);
    paymentRepository.save(payment);
  }

  private void processMerchant(Payment payment) {
    log.info("Stage: MERCHANT SETTLEMENT");
    payment.setStatus(PaymentStatus.SETTLED);
    paymentRepository.save(payment);
  }

  private void processMobile(Payment payment) {
    log.info("Stage: TELCO RECHARGE");
    payment.setExternalTransactionId("TELCO-" + payment.getTransactionId());
    payment.setStatus(PaymentStatus.SETTLED);
    paymentRepository.save(payment);
  }

  private void finalizePayment(Payment payment) {
    log.info("Stage: FINALIZATION");
    if (payment.getStatus() == PaymentStatus.SETTLED || payment.getStatus() == PaymentStatus.SENT) {
      // For SENT (SCT/SWIFT), we consider the business process active but reserved
      // For SETTLED / INTERNAL, we mark COMPLETED
      if (payment.getStatus() == PaymentStatus.SETTLED) {
        payment.setStatus(PaymentStatus.COMPLETED);
      }
      payment.setCompletedAt(java.time.Instant.now());
      paymentRepository.save(payment);

      eventProducer.completeTransaction(
          payment.getId(),
          payment.getFromAccountId(),
          payment.getToAccountId(),
          payment.getFromIban(),
          payment.getToIban(),
          payment.getAmount(),
          payment.getCurrency(),
          payment.getPaymentType().toString());
    }
  }

  private void handleGlobalFailure(Payment payment, Exception e) {
    log.warn("Triggering Compensation for payment {}", payment.getId());
    try {
      if (payment.getStatus() == PaymentStatus.RESERVED
          || payment.getDebitTransactionId() != null) {
        compensateFunds(payment);
        payment.setStatus(PaymentStatus.COMPENSATED);
      } else {
        payment.setStatus(PaymentStatus.FAILED);
      }
      payment.setFailureReason(e.getMessage());
      paymentRepository.save(payment);

      eventProducer.handlePaymentFailure(
          payment.getId(),
          payment.getFromAccountId(),
          payment.getFromIban(),
          payment.getAmount(),
          payment.getCurrency(),
          e.getMessage(),
          "SAGA_ABORT");
    } catch (Exception compensationError) {
      log.error("CRITICAL: Compensation failed for payment {}", payment.getId(), compensationError);
      payment.setStatus(PaymentStatus.FAILED);
      paymentRepository.save(payment);
    }
  }

  private void compensateFunds(Payment payment) {
    log.info("COMPENSATION: Crediting back {}", payment.getAmount());
    accountClient.credit(
        payment.getFromAccountId(),
        CreditRequest.builder()
            .amount(payment.getAmount())
            .description("ROLLBACK: Payment " + payment.getTransactionId() + " failed")
            .build());
  }
}

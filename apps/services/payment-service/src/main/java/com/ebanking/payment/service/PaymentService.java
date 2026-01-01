package com.ebanking.payment.service;

import com.ebanking.payment.dto.request.PaymentRequest;
import com.ebanking.payment.dto.response.PaymentResult;
import com.ebanking.payment.entity.*;
import com.ebanking.payment.repository.PaymentRepository;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

  private final PaymentRepository paymentRepository;
  private final PaymentSagaOrchestrator sagaOrchestrator;
  private final PaymentValidationService validationService;

  private static final BigDecimal SCA_THRESHOLD = new BigDecimal("100.00");

  @Transactional
  public PaymentResult initiatePayment(PaymentRequest request, Long userId) {
    log.info("Initiating {} via Senior Banking Engine - User: {}", request.getType(), userId);

    // 1. Validation Métier et Réglementaire
    validationService.validatePayment(request, userId);

    // 2. Vérifier idempotence
    var existing = paymentRepository.findByIdempotencyKey(request.getIdempotencyKey());
    if (existing.isPresent()) {
      return PaymentResult.success(existing.get());
    }

    // 3. Créer le paiement (Status: CREATED)
    Payment payment =
        Payment.builder()
            .transactionId(UUID.randomUUID().toString())
            .idempotencyKey(request.getIdempotencyKey())
            .paymentType(PaymentType.valueOf(request.getType()))
            .status(PaymentStatus.CREATED)
            .fromAccountId(request.getFromAccountId())
            .toAccountId(request.getToAccountId())
            .toIban(request.getToIban())
            .beneficiaryName(request.getBeneficiaryName())
            .amount(request.getAmount())
            .currency(request.getCurrency())
            .userId(userId)
            .description(request.getDescription())
            .reference(request.getEndToEndId())
            .ipAddress(request.getIpAddress())
            .userAgent(request.getUserAgent())
            .phoneNumber(request.getPhoneNumber())
            .operatorCode(request.getCountryCode())
            .build();

    payment = paymentRepository.save(payment);

    // 4. SCA (Strong Customer Authentication) Logic
    if (isScaRequired(payment)) {
      log.info("SCA Required (Threshold/Type) for payment {}", payment.getTransactionId());
      return PaymentResult.builder().payment(payment).success(true).message("SCA_REQUIRED").build();
    }

    // 5. Auto-validate and Execute
    payment.setStatus(PaymentStatus.AUTHORIZED);
    payment.setScaVerified(true);
    paymentRepository.save(payment);

    return sagaOrchestrator.executePayment(payment);
  }

  @Transactional
  public PaymentResult authorizePayment(Long paymentId, String otpCode, Long userId) {
    Payment payment =
        paymentRepository
            .findById(paymentId)
            .orElseThrow(() -> new IllegalArgumentException("Payment not found"));

    if (!payment.getUserId().equals(userId)) {
      throw new SecurityException("Unauthorized access to payment");
    }

    // Mock OTP validation
    if ("123456".equals(otpCode)) {
      payment.setScaVerified(true);
      payment.setStatus(PaymentStatus.AUTHORIZED);
      paymentRepository.save(payment);
      return sagaOrchestrator.executePayment(payment);
    } else {
      payment.setStatus(PaymentStatus.REJECTED);
      payment.setFailureReason("Invalid SCA OTP");
      paymentRepository.save(payment);
      return PaymentResult.failure(payment, new SecurityException("Invalid OTP"));
    }
  }

  private boolean isScaRequired(Payment payment) {
    return payment.getAmount().compareTo(SCA_THRESHOLD) > 0
        || payment.getPaymentType() == PaymentType.SWIFT_TRANSFER
        || payment.getPaymentType() == PaymentType.SEPA_TRANSFER;
  }
}

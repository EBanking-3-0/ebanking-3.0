package com.ebanking.payment.service;

import com.ebanking.payment.dto.request.PaymentRequest;
import com.ebanking.payment.dto.response.PaymentResult;
import com.ebanking.payment.entity.*;
import com.ebanking.payment.repository.PaymentRepository;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Service principal qui délègue aux services spécialisés selon le type de paiement. */
@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

  private final PaymentRepository paymentRepository;
  private final InternalTransferService internalTransferService;
  private final SepaTransferService sepaTransferService;
  private final InstantTransferService instantTransferService;
  private final MobileRechargeService mobileRechargeService;
  private final PaymentSagaOrchestrator sagaOrchestrator;

  private static final BigDecimal SCA_THRESHOLD = new BigDecimal("100.00");

  @Transactional
  public PaymentResult initiatePayment(PaymentRequest request, Long userId) {
    log.info("Initiating {} payment - User: {}", request.getType(), userId);

    // Déléguer au service spécialisé selon le type
    PaymentType paymentType = PaymentType.valueOf(request.getType());

    return switch (paymentType) {
      case INTERNAL_TRANSFER -> internalTransferService.executeInternalTransfer(request, userId);
      case SEPA_TRANSFER -> sepaTransferService.executeSepaTransfer(request, userId);
      case SCT_INSTANT -> instantTransferService.executeInstantTransfer(request, userId);
      case MOBILE_RECHARGE -> mobileRechargeService.executeMobileRecharge(request, userId);
      case SWIFT_TRANSFER, MERCHANT_PAYMENT -> {
        // Pour l'instant, utiliser la saga orchestrator pour ces types
        log.warn("Payment type {} not yet fully implemented, using saga orchestrator", paymentType);
        yield sagaOrchestrator.executePayment(createPaymentFromRequest(request, userId));
      }
    };
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

      // Exécuter selon le type
      PaymentRequest request = createRequestFromPayment(payment);
      return initiatePayment(request, userId);
    } else {
      payment.setStatus(PaymentStatus.REJECTED);
      payment.setFailureReason("Invalid SCA OTP");
      paymentRepository.save(payment);
      return PaymentResult.failure(payment, new SecurityException("Invalid OTP"));
    }
  }

  /** Crée un Payment à partir d'un PaymentRequest (pour les types non encore spécialisés). */
  private Payment createPaymentFromRequest(PaymentRequest request, Long userId) {
    return Payment.builder()
        .transactionId(java.util.UUID.randomUUID().toString())
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
  }

  /** Crée un PaymentRequest à partir d'un Payment (pour la ré-autorisation). */
  private PaymentRequest createRequestFromPayment(Payment payment) {
    PaymentRequest request = new PaymentRequest();
    request.setType(payment.getPaymentType().name());
    request.setFromAccountId(payment.getFromAccountId());
    request.setToAccountId(payment.getToAccountId());
    request.setToIban(payment.getToIban());
    request.setBeneficiaryName(payment.getBeneficiaryName());
    request.setAmount(payment.getAmount());
    request.setCurrency(payment.getCurrency());
    request.setDescription(payment.getDescription());
    request.setEndToEndId(payment.getReference());
    request.setIdempotencyKey(payment.getIdempotencyKey());
    request.setPhoneNumber(payment.getPhoneNumber());
    request.setCountryCode(payment.getOperatorCode());
    return request;
  }
}

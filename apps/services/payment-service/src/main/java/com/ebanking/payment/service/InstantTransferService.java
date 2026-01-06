package com.ebanking.payment.service;

import com.ebanking.payment.client.AccountServiceClient;
import com.ebanking.payment.client.LegacyAdapterClient;
import com.ebanking.payment.client.dto.AccountResponse;
import com.ebanking.payment.client.dto.DebitRequest;
import com.ebanking.payment.client.dto.DebitResponse;
import com.ebanking.payment.client.dto.InstantTransferRequest;
import com.ebanking.payment.client.dto.InstantTransferResponse;
import com.ebanking.payment.dto.request.PaymentRequest;
import com.ebanking.payment.dto.response.PaymentResult;
import com.ebanking.payment.entity.Payment;
import com.ebanking.payment.entity.PaymentStatus;
import com.ebanking.payment.entity.PaymentType;
import com.ebanking.payment.exception.FraudDetectedException;
import com.ebanking.payment.exception.InstantTransferRejectedException;
import com.ebanking.payment.exception.PaymentProcessingException;
import com.ebanking.payment.repository.PaymentRepository;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service spécialisé pour les virements instantanés (SCT Inst - SEPA Instant Credit Transfer).
 * Caractéristiques : - Délai : < 30 secondes - Plafond : 15,000€ - Anti-fraude obligatoire -
 * Irrévocable une fois accepté (ACK) - Timeout : 30s
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class InstantTransferService {

  private final PaymentRepository paymentRepository;
  private final PaymentValidationService validationService;
  private final PaymentStateMachine stateMachine;
  private final AccountServiceClient accountClient;
  private final LegacyAdapterClient legacyAdapter;
  private final PaymentEventProducer eventProducer;
  private final FraudDetectionService fraudDetection;
  private final PaymentSagaOrchestrator sagaOrchestrator;

  @Value("${payment.instant.max-amount:15000.00}")
  private BigDecimal maxInstantAmount;

  @Value("${payment.instant.timeout-seconds:30}")
  private int timeoutSeconds;

  @Transactional
  public PaymentResult executeInstantTransfer(PaymentRequest request, String userId) {
    log.info(
        "Processing instant transfer from account {} to IBAN {} - Amount: {} {}",
        request.getFromAccountId(),
        request.getToIban(),
        request.getAmount(),
        request.getCurrency());

    // 1. Validation métier
    validationService.validatePayment(request, userId);

    // 2. Vérifier idempotency
    Optional<Payment> existing =
        paymentRepository.findByIdempotencyKey(request.getIdempotencyKey());
    if (existing.isPresent()) {
      log.info("Duplicate payment detected (idempotency key: {})", request.getIdempotencyKey());
      return PaymentResult.success(existing.get());
    }

    // 3. Vérifier plafond SCT Inst (15,000€)
    if (request.getAmount().compareTo(maxInstantAmount) > 0) {
      throw new PaymentProcessingException(
          String.format(
              "Instant transfer amount exceeds maximum: %s (max: %s)",
              request.getAmount(), maxInstantAmount));
    }

    // 4. Récupérer l'IBAN source
    AccountResponse fromAccount = accountClient.getAccount(request.getFromAccountId());
    String fromIban = fromAccount.getIban();
    if (fromIban == null) {
      throw new PaymentProcessingException("Source account does not have an IBAN");
    }

    // 5. Créer le paiement
    Payment payment =
        Payment.builder()
            .transactionId(UUID.randomUUID().toString())
            .idempotencyKey(request.getIdempotencyKey())
            .paymentType(PaymentType.SCT_INSTANT)
            .status(PaymentStatus.CREATED)
            .fromAccountId(request.getFromAccountId())
            .fromIban(fromIban)
            .toIban(request.getToIban())
            .beneficiaryName(request.getBeneficiaryName())
            .amount(request.getAmount())
            .currency(request.getCurrency())
            .userId(userId)
            .description(request.getDescription())
            .reference(request.getEndToEndId())
            .ipAddress(request.getIpAddress())
            .userAgent(request.getUserAgent())
            .build();

    payment = paymentRepository.save(payment);
    log.info("Created instant transfer payment with ID: {}", payment.getId());

    try {
      // 6. ANTI-FRAUDE OBLIGATOIRE (plus strict que les autres types)
      stateMachine.transition(payment, PaymentStatus.VALIDATED);
      var fraudCheck = fraudDetection.checkFraud(payment);

      if (fraudCheck.isBlocked()) {
        log.warn("Instant transfer {} blocked by fraud detection", payment.getId());
        payment.setStatus(PaymentStatus.REJECTED);
        payment.setFailureReason("Fraud detected: " + fraudCheck.getIndicators());
        paymentRepository.save(payment);

        eventProducer.detectFraud(
            payment.getId(),
            payment.getUserId(),
            payment.getFromAccountId(),
            payment.getFromIban(),
            payment.getAmount(),
            payment.getCurrency(),
            "FRAUD_BLOCKED",
            "HIGH",
            "Instant transfer blocked: " + fraudCheck.getIndicators());
        throw new FraudDetectedException("Transaction bloquée par anti-fraude");
      }

      payment.setFraudCheckPassed(true);
      paymentRepository.save(payment);

      // 7. Débit du compte source
      stateMachine.transition(payment, PaymentStatus.AUTHORIZED);
      DebitRequest debitRequest =
          DebitRequest.builder()
              .amount(payment.getAmount())
              .transactionId(payment.getTransactionId())
              .idempotencyKey(payment.getIdempotencyKey())
              .description(
                  payment.getDescription() != null ? payment.getDescription() : "Instant transfer")
              .build();

      DebitResponse debitResponse = accountClient.debit(payment.getFromAccountId(), debitRequest);
      payment.setDebitTransactionId(debitResponse.getTransactionId());
      paymentRepository.save(payment);
      log.info(
          "Debited account {} for instant transfer {}",
          payment.getFromAccountId(),
          payment.getId());

      // 8. Envoyer au legacy-adapter avec timeout
      stateMachine.transition(payment, PaymentStatus.SENT);
      InstantTransferRequest instantRequest =
          InstantTransferRequest.builder()
              .fromIban(payment.getFromIban())
              .toIban(payment.getToIban())
              .amount(payment.getAmount())
              .currency(payment.getCurrency())
              .beneficiaryName(payment.getBeneficiaryName())
              .description(payment.getDescription())
              .transactionId(payment.getTransactionId())
              .idempotencyKey(payment.getIdempotencyKey())
              .build();

      // Appel avec gestion de timeout
      InstantTransferResponse instantResponse;
      try {
        instantResponse = legacyAdapter.executeInstantTransfer(instantRequest);
      } catch (Exception e) {
        if (e.getCause() instanceof java.util.concurrent.TimeoutException) {
          throw new InstantTransferRejectedException(
              "Timeout: Instant transfer did not complete within " + timeoutSeconds + " seconds");
        }
        throw e;
      }

      // 9. Traiter la réponse (ACK/NACK)
      if ("ACK".equals(instantResponse.getStatus())) {
        // ACK = Accepté, irrévocable
        payment.setExternalTransactionId(instantResponse.getExternalTransactionId());
        payment.setIso20022MessageReference(instantResponse.getIso20022Reference());
        stateMachine.transition(payment, PaymentStatus.COMPLETED);
        payment.setCompletedAt(java.time.Instant.now());
        paymentRepository.save(payment);

        eventProducer.completeTransaction(
            payment.getId(),
            payment.getUserId(),
            payment.getFromAccountId(),
            null,
            payment.getFromIban(),
            payment.getToIban(),
            payment.getAmount(),
            payment.getCurrency(),
            "SCT_INSTANT");

        log.info("Instant transfer {} completed successfully (ACK)", payment.getId());
        return PaymentResult.success(payment);
      } else {
        // NACK = Rejeté
        throw new InstantTransferRejectedException(
            "Instant transfer rejected (NACK): " + instantResponse.getRejectionReason());
      }

    } catch (FraudDetectedException | InstantTransferRejectedException e) {
      log.error("Instant transfer {} failed", payment.getId(), e);
      if (payment.getStatus() != PaymentStatus.REJECTED) {
        payment.setStatus(PaymentStatus.FAILED);
      }
      payment.setFailureReason(e.getMessage());
      paymentRepository.save(payment);

      // Compensation : rembourser le débit
      if (payment.getDebitTransactionId() != null) {
        sagaOrchestrator.compensatePayment(payment);
      }

      eventProducer.handlePaymentFailure(
          payment.getId(),
          payment.getUserId(),
          payment.getFromAccountId(),
          payment.getFromIban(),
          payment.getAmount(),
          payment.getCurrency(),
          e.getMessage(),
          e.getClass().getSimpleName());
      return PaymentResult.failure(payment, e);
    } catch (Exception e) {
      log.error("Instant transfer {} failed", payment.getId(), e);
      payment.setStatus(PaymentStatus.FAILED);
      payment.setFailureReason(e.getMessage());
      paymentRepository.save(payment);

      // Compensation
      if (payment.getDebitTransactionId() != null) {
        sagaOrchestrator.compensatePayment(payment);
      }

      eventProducer.handlePaymentFailure(
          payment.getId(),
          payment.getUserId(),
          payment.getFromAccountId(),
          payment.getFromIban(),
          payment.getAmount(),
          payment.getCurrency(),
          e.getMessage(),
          e.getClass().getSimpleName());
      return PaymentResult.failure(payment, e);
    }
  }
}

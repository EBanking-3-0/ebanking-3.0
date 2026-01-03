package com.ebanking.payment.service;

import com.ebanking.payment.client.AccountServiceClient;
import com.ebanking.payment.client.LegacyAdapterClient;
import com.ebanking.payment.client.dto.AccountResponse;
import com.ebanking.payment.client.dto.DebitRequest;
import com.ebanking.payment.client.dto.DebitResponse;
import com.ebanking.payment.client.dto.SepaTransferRequest;
import com.ebanking.payment.client.dto.SepaTransferResponse;
import com.ebanking.payment.dto.request.PaymentRequest;
import com.ebanking.payment.dto.response.PaymentResult;
import com.ebanking.payment.entity.Payment;
import com.ebanking.payment.entity.PaymentStatus;
import com.ebanking.payment.entity.PaymentType;
import com.ebanking.payment.exception.PaymentProcessingException;
import com.ebanking.payment.exception.SepaRejectionException;
import com.ebanking.payment.repository.PaymentRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service spécialisé pour les virements SEPA (Single Euro Payments Area). Caractéristiques : -
 * Délai : 1-2 jours ouvrables - Cut-off : 16h (avant = traitement immédiat, après = batch suivant)
 * - Communication avec legacy-adapter (REST → SOAP → Core Banking) - Compensation si rejeté par le
 * core banking
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SepaTransferService {

  private final PaymentRepository paymentRepository;
  private final PaymentValidationService validationService;
  private final PaymentStateMachine stateMachine;
  private final AccountServiceClient accountClient;
  private final LegacyAdapterClient legacyAdapter;
  private final PaymentEventProducer eventProducer;
  private final PaymentSagaOrchestrator sagaOrchestrator;

  private static final LocalTime CUT_OFF_TIME = LocalTime.of(16, 0); // 16h00

  @Transactional
  public PaymentResult executeSepaTransfer(PaymentRequest request, Long userId) {
    log.info(
        "Processing SEPA transfer from account {} to IBAN {} - Amount: {} {}",
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

    // 3. Récupérer l'IBAN source
    AccountResponse fromAccount = accountClient.getAccount(request.getFromAccountId());
    String fromIban = fromAccount.getIban();
    if (fromIban == null) {
      throw new PaymentProcessingException("Source account does not have an IBAN");
    }

    // 4. Créer le paiement
    Payment payment =
        Payment.builder()
            .transactionId(UUID.randomUUID().toString())
            .idempotencyKey(request.getIdempotencyKey())
            .paymentType(PaymentType.SEPA_TRANSFER)
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
    log.info("Created SEPA transfer payment with ID: {}", payment.getId());

    // 5. Vérifier cut-off (16h)
    LocalTime now = LocalTime.now();
    if (now.isAfter(CUT_OFF_TIME)) {
      log.info("After cut-off time ({}), payment will be processed in next batch", CUT_OFF_TIME);
      payment.setStatus(PaymentStatus.RESERVED); // En attente du batch
      payment.setEstimatedCompletionDate(java.time.Instant.now().plusSeconds(86400)); // +1 jour
      paymentRepository.save(payment);
      return PaymentResult.success(payment, "Payment queued for next batch processing");
    }

    // 6. Traitement immédiat (avant cut-off)
    try {
      // Débit du compte source
      stateMachine.transition(payment, PaymentStatus.VALIDATED);
      DebitRequest debitRequest =
          DebitRequest.builder()
              .amount(payment.getAmount())
              .transactionId(payment.getTransactionId())
              .idempotencyKey(payment.getIdempotencyKey())
              .description(
                  payment.getDescription() != null ? payment.getDescription() : "SEPA transfer")
              .build();

      DebitResponse debitResponse = accountClient.debit(payment.getFromAccountId(), debitRequest);
      payment.setDebitTransactionId(debitResponse.getTransactionId());
      paymentRepository.save(payment);
      log.info(
          "Debited account {} for SEPA transfer {}", payment.getFromAccountId(), payment.getId());

      // 7. Envoyer au legacy-adapter (REST → SOAP → Core Banking)
      stateMachine.transition(payment, PaymentStatus.SENT);
      SepaTransferRequest sepaRequest =
          SepaTransferRequest.builder()
              .fromIban(payment.getFromIban())
              .toIban(payment.getToIban())
              .amount(payment.getAmount())
              .currency(payment.getCurrency())
              .beneficiaryName(payment.getBeneficiaryName())
              .description(payment.getDescription())
              .transactionId(payment.getTransactionId())
              .idempotencyKey(payment.getIdempotencyKey())
              .executionDate(LocalDate.now().toString())
              .build();

      SepaTransferResponse sepaResponse = legacyAdapter.executeSepaTransfer(sepaRequest);

      // 8. Traiter la réponse
      if ("ACCEPTED".equals(sepaResponse.getStatus())) {
        payment.setExternalTransactionId(sepaResponse.getExternalTransactionId());
        payment.setIso20022MessageReference(sepaResponse.getIso20022Reference());
        stateMachine.transition(payment, PaymentStatus.SETTLED);
        payment.setCompletedAt(java.time.Instant.now());
        paymentRepository.save(payment);

        eventProducer.completeTransaction(
            payment.getId(),
            payment.getFromAccountId(),
            null,
            payment.getFromIban(),
            payment.getToIban(),
            payment.getAmount(),
            payment.getCurrency(),
            "SEPA_TRANSFER");

        log.info("SEPA transfer {} completed successfully", payment.getId());
        return PaymentResult.success(payment);
      } else {
        // Rejeté par le core banking
        throw new SepaRejectionException(
            "SEPA transfer rejected: " + sepaResponse.getRejectionReason());
      }

    } catch (SepaRejectionException e) {
      log.error("SEPA transfer {} rejected", payment.getId(), e);
      payment.setStatus(PaymentStatus.REJECTED);
      payment.setFailureReason(e.getMessage());
      paymentRepository.save(payment);

      // Compensation : rembourser le débit
      sagaOrchestrator.compensatePayment(payment);
      eventProducer.handlePaymentFailure(
          payment.getId(),
          payment.getFromAccountId(),
          payment.getFromIban(),
          payment.getAmount(),
          payment.getCurrency(),
          e.getMessage(),
          "SEPA_REJECTED");
      return PaymentResult.failure(payment, e);
    } catch (Exception e) {
      log.error("SEPA transfer {} failed", payment.getId(), e);
      payment.setStatus(PaymentStatus.FAILED);
      payment.setFailureReason(e.getMessage());
      paymentRepository.save(payment);

      // Compensation
      sagaOrchestrator.compensatePayment(payment);
      eventProducer.handlePaymentFailure(
          payment.getId(),
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

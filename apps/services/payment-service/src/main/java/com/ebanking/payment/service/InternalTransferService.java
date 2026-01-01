package com.ebanking.payment.service;

import com.ebanking.payment.client.AccountServiceClient;
import com.ebanking.payment.client.dto.AccountResponse;
import com.ebanking.payment.dto.request.PaymentRequest;
import com.ebanking.payment.dto.response.PaymentResult;
import com.ebanking.payment.entity.Payment;
import com.ebanking.payment.entity.PaymentStatus;
import com.ebanking.payment.entity.PaymentType;
import com.ebanking.payment.repository.PaymentRepository;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service spécialisé pour les virements internes (même banque). Caractéristiques : - Synchrone
 * (réponse immédiate) - 24/7 (pas de cut-off) - Débit + Crédit atomiques - Compensation automatique
 * en cas d'échec
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class InternalTransferService {

  private final PaymentRepository paymentRepository;
  private final PaymentSagaOrchestrator sagaOrchestrator;
  private final PaymentValidationService validationService;
  private final AccountServiceClient accountClient;

  @Transactional
  public PaymentResult executeInternalTransfer(PaymentRequest request, Long userId) {
    log.info(
        "Processing internal transfer from account {} to account {} - Amount: {} {}",
        request.getFromAccountId(),
        request.getToAccountId(),
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

    // 3. Vérifier que le compte destinataire existe (pour virement interne)
    if (request.getToAccountId() == null && request.getToAccountNumber() == null) {
      throw new IllegalArgumentException(
          "ToAccountId or ToAccountNumber is required for internal transfer");
    }

    // Récupérer le compte destinataire si on a seulement le numéro
    Long toAccountId = request.getToAccountId();
    if (toAccountId == null && request.getToAccountNumber() != null) {
      AccountResponse toAccount = accountClient.getAccountByNumber(request.getToAccountNumber());
      toAccountId = toAccount.getId();
    }

    // 4. Créer le paiement
    Payment payment =
        Payment.builder()
            .transactionId(UUID.randomUUID().toString())
            .idempotencyKey(request.getIdempotencyKey())
            .paymentType(PaymentType.INTERNAL_TRANSFER)
            .status(PaymentStatus.CREATED)
            .fromAccountId(request.getFromAccountId())
            .toAccountId(toAccountId)
            .toIban(request.getToAccountNumber()) // Use toIban field for account number
            .amount(request.getAmount())
            .currency(request.getCurrency())
            .userId(userId)
            .description(request.getDescription())
            .reference(request.getEndToEndId())
            .ipAddress(request.getIpAddress())
            .userAgent(request.getUserAgent())
            .build();

    payment = paymentRepository.save(payment);
    log.info("Created internal transfer payment with ID: {}", payment.getId());

    // 5. Exécuter la saga (validation, anti-fraude, débit, crédit)
    return sagaOrchestrator.executePayment(payment);
  }
}

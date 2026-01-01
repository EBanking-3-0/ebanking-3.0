package com.ebanking.payment.service;

import com.ebanking.payment.client.AccountServiceClient;
import com.ebanking.payment.client.dto.DebitRequest;
import com.ebanking.payment.client.dto.DebitResponse;
import com.ebanking.payment.dto.request.PaymentRequest;
import com.ebanking.payment.dto.response.PaymentResult;
import com.ebanking.payment.entity.Payment;
import com.ebanking.payment.entity.PaymentStatus;
import com.ebanking.payment.entity.PaymentType;
import com.ebanking.payment.exception.OperatorRechargeException;
import com.ebanking.payment.repository.PaymentRepository;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service spécialisé pour les recharges mobiles. Caractéristiques : - Validation du numéro de
 * téléphone et de l'opérateur - Communication avec système externe (opérateur) - Gestion des
 * erreurs critiques (numéro invalide, opérateur indisponible) - Compensation immédiate en cas
 * d'échec
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MobileRechargeService {

  private final PaymentRepository paymentRepository;
  private final PaymentValidationService validationService;
  private final PaymentStateMachine stateMachine;
  private final AccountServiceClient accountClient;
  private final PaymentEventProducer eventProducer;
  private final PaymentSagaOrchestrator sagaOrchestrator;

  // Patterns pour détecter l'opérateur (exemple pour la France)
  private static final Pattern ORANGE_PATTERN = Pattern.compile("^(\\+33|0)[67]");
  private static final Pattern SFR_PATTERN = Pattern.compile("^(\\+33|0)[67]");
  private static final Pattern BOUYGUES_PATTERN = Pattern.compile("^(\\+33|0)[67]");
  private static final Pattern FREE_PATTERN = Pattern.compile("^(\\+33|0)[67]");

  @Transactional
  public PaymentResult executeMobileRecharge(PaymentRequest request, Long userId) {
    log.info(
        "Processing mobile recharge for phone {} - Amount: {} {}",
        request.getPhoneNumber(),
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

    // 3. Valider le numéro de téléphone
    if (request.getPhoneNumber() == null || request.getPhoneNumber().trim().isEmpty()) {
      throw new IllegalArgumentException("Phone number is required for mobile recharge");
    }

    // 4. Détecter l'opérateur
    String operator = detectOperator(request.getPhoneNumber(), request.getCountryCode());
    if (operator == null) {
      throw new OperatorRechargeException(
          "Unable to detect operator for phone number: " + request.getPhoneNumber());
    }

    // 5. Créer le paiement
    Payment payment =
        Payment.builder()
            .transactionId(UUID.randomUUID().toString())
            .idempotencyKey(request.getIdempotencyKey())
            .paymentType(PaymentType.MOBILE_RECHARGE)
            .status(PaymentStatus.CREATED)
            .fromAccountId(request.getFromAccountId())
            .amount(request.getAmount())
            .currency(request.getCurrency())
            .userId(userId)
            .description(
                request.getDescription() != null ? request.getDescription() : "Mobile recharge")
            .phoneNumber(request.getPhoneNumber())
            .operatorCode(operator)
            .ipAddress(request.getIpAddress())
            .userAgent(request.getUserAgent())
            .build();

    payment = paymentRepository.save(payment);
    log.info(
        "Created mobile recharge payment with ID: {} for operator: {}", payment.getId(), operator);

    try {
      // 6. Débit du compte
      stateMachine.transition(payment, PaymentStatus.VALIDATED);
      DebitRequest debitRequest =
          DebitRequest.builder()
              .amount(payment.getAmount())
              .transactionId(payment.getTransactionId())
              .idempotencyKey(payment.getIdempotencyKey())
              .description("Mobile recharge: " + payment.getPhoneNumber())
              .build();

      DebitResponse debitResponse = accountClient.debit(payment.getFromAccountId(), debitRequest);
      payment.setDebitTransactionId(debitResponse.getTransactionId());
      paymentRepository.save(payment);
      log.info(
          "Debited account {} for mobile recharge {}", payment.getFromAccountId(), payment.getId());

      // 7. Envoyer la recharge à l'opérateur (simulation)
      stateMachine.transition(payment, PaymentStatus.AUTHORIZED);

      // TODO: Appel réel à l'API de l'opérateur
      // Pour l'instant, simulation
      boolean rechargeSuccess =
          simulateOperatorRecharge(payment.getPhoneNumber(), operator, payment.getAmount());

      if (!rechargeSuccess) {
        throw new OperatorRechargeException("Operator " + operator + " rejected the recharge");
      }

      // 8. Succès
      stateMachine.transition(payment, PaymentStatus.COMPLETED);
      payment.setCompletedAt(java.time.Instant.now());
      paymentRepository.save(payment);

      eventProducer.completeTransaction(
          payment.getId(),
          payment.getFromAccountId(),
          null,
          null,
          payment.getPhoneNumber(),
          payment.getAmount(),
          payment.getCurrency(),
          "MOBILE_RECHARGE");

      log.info("Mobile recharge {} completed successfully", payment.getId());
      return PaymentResult.success(payment);

    } catch (OperatorRechargeException e) {
      log.error("Mobile recharge {} failed", payment.getId(), e);
      payment.setStatus(PaymentStatus.REJECTED);
      payment.setFailureReason(e.getMessage());
      paymentRepository.save(payment);

      // Compensation immédiate
      if (payment.getDebitTransactionId() != null) {
        sagaOrchestrator.compensatePayment(payment);
      }

      eventProducer.handlePaymentFailure(
          payment.getId(),
          payment.getFromAccountId(),
          null,
          payment.getAmount(),
          payment.getCurrency(),
          e.getMessage(),
          "OPERATOR_REJECTED");
      return PaymentResult.failure(payment, e);
    } catch (Exception e) {
      log.error("Mobile recharge {} failed", payment.getId(), e);
      payment.setStatus(PaymentStatus.FAILED);
      payment.setFailureReason(e.getMessage());
      paymentRepository.save(payment);

      // Compensation
      if (payment.getDebitTransactionId() != null) {
        sagaOrchestrator.compensatePayment(payment);
      }

      eventProducer.handlePaymentFailure(
          payment.getId(),
          payment.getFromAccountId(),
          null,
          payment.getAmount(),
          payment.getCurrency(),
          e.getMessage(),
          e.getClass().getSimpleName());
      return PaymentResult.failure(payment, e);
    }
  }

  /** Détecte l'opérateur à partir du numéro de téléphone. */
  private String detectOperator(String phoneNumber, String countryCode) {
    if (phoneNumber == null) return null;

    // Simplification : utiliser le countryCode si fourni
    if (countryCode != null) {
      // Pour la France, on pourrait avoir des règles spécifiques
      if ("FR".equals(countryCode)) {
        // Logique simplifiée : utiliser le préfixe
        if (phoneNumber.startsWith("+336") || phoneNumber.startsWith("06")) {
          return "ORANGE";
        } else if (phoneNumber.startsWith("+337") || phoneNumber.startsWith("07")) {
          return "SFR";
        }
      }
    }

    // Par défaut, utiliser le countryCode comme opérateur
    return countryCode != null ? countryCode : "UNKNOWN";
  }

  /**
   * Simule l'appel à l'API de l'opérateur. TODO: Remplacer par un vrai appel HTTP à l'API de
   * l'opérateur.
   */
  private boolean simulateOperatorRecharge(
      String phoneNumber, String operator, java.math.BigDecimal amount) {
    // Simulation : toujours réussir pour l'instant
    // En production, appeler l'API de l'opérateur (Orange, SFR, etc.)
    log.info(
        "Simulating recharge for {} via operator {} - Amount: {}", phoneNumber, operator, amount);
    return true;
  }
}

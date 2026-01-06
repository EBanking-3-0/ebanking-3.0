package com.ebanking.payment.controller;

import com.ebanking.payment.dto.request.PaymentRequest;
import com.ebanking.payment.dto.request.ScaVerificationRequest;
import com.ebanking.payment.dto.response.PaymentResponse;
import com.ebanking.payment.dto.response.PaymentResult;
import com.ebanking.payment.entity.Payment;
import com.ebanking.payment.service.PaymentQueryService;
import com.ebanking.payment.service.PaymentService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

  private final PaymentQueryService paymentQueryService;
  private final PaymentService paymentService;

  @GetMapping("/test")
  public ResponseEntity<?> test() {

    return ResponseEntity.ok().body("message");
  }

  @PostMapping("/internal")
  // @PreAuthorize("hasRole('user')") // Commenté temporairement pour les tests
  public ResponseEntity<PaymentResponse> createInternalTransfer(
      @Valid @RequestBody PaymentRequest request,
      @RequestParam(required = false, defaultValue = "1") Long userId) {
    // Ajout userId en paramètre pour les tests
    request.setType("INTERNAL_TRANSFER");
    PaymentResult result = paymentService.initiatePayment(request, userId);
    return ResponseEntity.ok(mapToResponse(result));
  }

  @PostMapping("/sepa")
  // @PreAuthorize("hasRole('user')") // Commenté temporairement pour les tests
  public ResponseEntity<PaymentResponse> createSepaTransfer(
      @Valid @RequestBody PaymentRequest request,
      @RequestParam(required = false, defaultValue = "1") Long userId) {
    request.setType("SEPA_TRANSFER");
    PaymentResult result = paymentService.initiatePayment(request, userId);
    return ResponseEntity.ok(mapToResponse(result));
  }

  @PostMapping("/instant")
  // @PreAuthorize("hasRole('user')") // Commenté temporairement pour les tests
  public ResponseEntity<PaymentResponse> createInstantTransfer(
      @Valid @RequestBody PaymentRequest request,
      @RequestParam(required = false, defaultValue = "1") Long userId) {
    request.setType("SCT_INSTANT");
    PaymentResult result = paymentService.initiatePayment(request, userId);
    return ResponseEntity.ok(mapToResponse(result));
  }

  @PostMapping("/swift")
  // @PreAuthorize("hasRole('user')") // Commenté temporairement pour les tests
  public ResponseEntity<PaymentResponse> createSwiftTransfer(
      @Valid @RequestBody PaymentRequest request,
      @RequestParam(required = false, defaultValue = "1") Long userId) {
    request.setType("SWIFT_TRANSFER");
    PaymentResult result = paymentService.initiatePayment(request, userId);
    return ResponseEntity.ok(mapToResponse(result));
  }

  @PostMapping("/merchant")
  // @PreAuthorize("hasRole('user')") // Commenté temporairement pour les tests
  public ResponseEntity<PaymentResponse> createMerchantPayment(
      @Valid @RequestBody PaymentRequest request,
      @RequestParam(required = false, defaultValue = "1") Long userId) {
    request.setType("MERCHANT_PAYMENT");
    PaymentResult result = paymentService.initiatePayment(request, userId);
    return ResponseEntity.ok(mapToResponse(result));
  }

  @PostMapping("/mobile-recharge")
  // @PreAuthorize("hasRole('user')") // Commenté temporairement pour les tests
  public ResponseEntity<PaymentResponse> createMobileRecharge(
      @Valid @RequestBody PaymentRequest request,
      @RequestParam(required = false, defaultValue = "1") Long userId) {
    request.setType("MOBILE_RECHARGE");
    PaymentResult result = paymentService.initiatePayment(request, userId);
    return ResponseEntity.ok(mapToResponse(result));
  }

  @PostMapping("/{id}/authorize")
  // @PreAuthorize("hasRole('user')") // Commenté temporairement pour les tests
  public ResponseEntity<PaymentResponse> authorizePayment(
      @PathVariable Long id,
      @Valid @RequestBody ScaVerificationRequest scaRequest,
      @RequestParam(required = false, defaultValue = "1") Long userId) {
    // Long userId = extractUserId(auth); // Commenté pour les tests
    PaymentResult result = paymentService.authorizePayment(id, scaRequest.getOtpCode(), userId);
    return ResponseEntity.ok(mapToResponse(result));
  }

  private ResponseEntity<PaymentResponse> processPayment(
      PaymentRequest request, JwtAuthenticationToken auth) {
    Long userId = extractUserId(auth);
    PaymentResult result = paymentService.initiatePayment(request, userId);
    return ResponseEntity.ok(mapToResponse(result));
  }

  @GetMapping("/{id}")
  // @PreAuthorize("hasRole('user')") // Commenté temporairement pour les tests
  public ResponseEntity<PaymentResponse> getPayment(@PathVariable Long id) {
    Payment payment = paymentQueryService.getPaymentById(id);
    return ResponseEntity.ok(mapToResponse(PaymentResult.success(payment)));
  }

  @GetMapping("/user")
  // @PreAuthorize("hasRole('user')") // Commenté temporairement pour les tests
  public ResponseEntity<List<PaymentResponse>> getUserPayments(
      @RequestParam(required = false, defaultValue = "1") Long userId) {
    List<Payment> payments = paymentQueryService.getPaymentsByUserId(userId);
    return ResponseEntity.ok(
        payments.stream()
            .map(p -> mapToResponse(PaymentResult.success(p)))
            .collect(Collectors.toList()));
  }

  private PaymentResponse mapToResponse(PaymentResult result) {
    Payment p = result.getPayment();
    return PaymentResponse.builder()
        .paymentId(p.getId())
        .transactionId(p.getTransactionId())
        .status(p.getStatus().toString())
        .paymentType(p.getPaymentType() != null ? p.getPaymentType().toString() : null)
        .amount(p.getAmount())
        .currency(p.getCurrency())
        .fees(p.getFees())
        .reference(p.getReference())
        .uetr(p.getUetr())
        .message(
            result.isSuccess()
                ? (result.getMessage() != null ? result.getMessage() : "Success")
                : result.getError())
        .createdAt(p.getCreatedAt().toString())
        .estimatedCompletionDate(
            p.getEstimatedCompletionDate() != null
                ? p.getEstimatedCompletionDate().toString()
                : null)
        .build();
  }

  private Long extractUserId(JwtAuthenticationToken auth) {
    if (auth == null || auth.getToken() == null) return 1L;
    Object userIdClaim = auth.getTokenAttributes().get("userId");
    if (userIdClaim != null) return Long.valueOf(userIdClaim.toString());
    try {
      return Long.parseLong(auth.getName());
    } catch (NumberFormatException e) {
      return 1L;
    }
  }
}

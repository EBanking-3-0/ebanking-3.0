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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

  private final PaymentQueryService paymentQueryService;
  private final PaymentService paymentService;

  @PostMapping("/internal")
  @PreAuthorize("hasRole('user')")
  public ResponseEntity<PaymentResponse> createInternalTransfer(
      @Valid @RequestBody PaymentRequest request,
      @AuthenticationPrincipal JwtAuthenticationToken auth) {
    request.setType("INTERNAL_TRANSFER");
    return processPayment(request, auth);
  }

  @PostMapping("/sepa")
  @PreAuthorize("hasRole('user')")
  public ResponseEntity<PaymentResponse> createSepaTransfer(
      @Valid @RequestBody PaymentRequest request,
      @AuthenticationPrincipal JwtAuthenticationToken auth) {
    request.setType("SEPA_TRANSFER");
    return processPayment(request, auth);
  }

  @PostMapping("/instant")
  @PreAuthorize("hasRole('user')")
  public ResponseEntity<PaymentResponse> createInstantTransfer(
      @Valid @RequestBody PaymentRequest request,
      @AuthenticationPrincipal JwtAuthenticationToken auth) {
    request.setType("SCT_INSTANT");
    return processPayment(request, auth);
  }

  @PostMapping("/swift")
  @PreAuthorize("hasRole('user')")
  public ResponseEntity<PaymentResponse> createSwiftTransfer(
      @Valid @RequestBody PaymentRequest request,
      @AuthenticationPrincipal JwtAuthenticationToken auth) {
    request.setType("SWIFT_TRANSFER");
    return processPayment(request, auth);
  }

  @PostMapping("/merchant")
  @PreAuthorize("hasRole('user')")
  public ResponseEntity<PaymentResponse> createMerchantPayment(
      @Valid @RequestBody PaymentRequest request,
      @AuthenticationPrincipal JwtAuthenticationToken auth) {
    request.setType("MERCHANT_PAYMENT");
    return processPayment(request, auth);
  }

  @PostMapping("/mobile-recharge")
  @PreAuthorize("hasRole('user')")
  public ResponseEntity<PaymentResponse> createMobileRecharge(
      @Valid @RequestBody PaymentRequest request,
      @AuthenticationPrincipal JwtAuthenticationToken auth) {
    request.setType("MOBILE_RECHARGE");
    return processPayment(request, auth);
  }

  @PostMapping("/{id}/authorize")
  @PreAuthorize("hasRole('user')")
  public ResponseEntity<PaymentResponse> authorizePayment(
      @PathVariable Long id,
      @Valid @RequestBody ScaVerificationRequest scaRequest,
      @AuthenticationPrincipal JwtAuthenticationToken auth) {
    Long userId = extractUserId(auth);
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
  @PreAuthorize("hasRole('user')")
  public ResponseEntity<PaymentResponse> getPayment(@PathVariable Long id) {
    Payment payment = paymentQueryService.getPaymentById(id);
    return ResponseEntity.ok(mapToResponse(PaymentResult.success(payment)));
  }

  @GetMapping("/user")
  @PreAuthorize("hasRole('user')")
  public ResponseEntity<List<PaymentResponse>> getUserPayments(
      @AuthenticationPrincipal JwtAuthenticationToken auth) {
    Long userId = extractUserId(auth);
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

package com.ebanking.graphql.client;

import com.ebanking.graphql.model.PaymentRequest;
import com.ebanking.graphql.model.PaymentResponse;
import com.ebanking.graphql.model.ScaVerificationRequest;
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "payment-service")
public interface PaymentClient {

  @PostMapping("/api/payments/internal")
  PaymentResponse createInternalTransfer(@RequestBody PaymentRequest request);

  @PostMapping("/api/payments/sepa")
  PaymentResponse createSepaTransfer(@RequestBody PaymentRequest request);

  @PostMapping("/api/payments/instant")
  PaymentResponse createInstantTransfer(@RequestBody PaymentRequest request);

  @PostMapping("/api/payments/mobile-recharge")
  PaymentResponse createMobileRecharge(@RequestBody PaymentRequest request);

  @GetMapping("/api/payments/user")
  List<PaymentResponse> getUserPayments();

  @PostMapping("/api/payments/{id}/authorize")
  PaymentResponse authorizePayment(@PathVariable("id") Long id, @RequestBody ScaVerificationRequest request);
}

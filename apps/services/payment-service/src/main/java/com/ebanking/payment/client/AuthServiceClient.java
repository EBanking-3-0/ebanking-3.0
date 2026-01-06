package com.ebanking.payment.client;

import com.ebanking.payment.client.dto.MfaVerificationRequest;
import com.ebanking.payment.client.dto.MfaVerificationResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "auth-service", path = "/api/auth")
public interface AuthServiceClient {

  @PostMapping("/mfa/verify")
  MfaVerificationResponse verifyMFA(@RequestBody MfaVerificationRequest request);
}

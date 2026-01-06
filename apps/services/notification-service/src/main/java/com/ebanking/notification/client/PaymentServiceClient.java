package com.ebanking.notification.client;

import com.ebanking.notification.dto.PaymentDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/** REST client for communicating with Payment Service. */
@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentServiceClient {

  private final RestTemplate restTemplate;

  @Value("${payment-service.base-url:http://payment-service}")
  private String paymentServiceBaseUrl;

  /**
   * Fetch payment information.
   *
   * @param paymentId Payment ID
   * @return Payment details
   */
  public PaymentDTO getPayment(Long paymentId) {
    try {
      String url = paymentServiceBaseUrl + "/api/payments/{paymentId}";
      PaymentDTO response = restTemplate.getForObject(url, PaymentDTO.class, paymentId);

      if (response != null) {
        log.debug("Retrieved payment for paymentId: {}", paymentId);
        return response;
      }

      log.warn("Payment service returned null for paymentId: {}", paymentId);
      throw new PaymentServiceException("Payment not found: " + paymentId);

    } catch (Exception e) {
      log.error("Failed to fetch payment from payment-service for paymentId: {}", paymentId, e);
      throw new PaymentServiceException("Failed to fetch payment: " + e.getMessage(), e);
    }
  }

  /** Custom exception for payment service errors. */
  public static class PaymentServiceException extends RuntimeException {

    public PaymentServiceException(String message) {
      super(message);
    }

    public PaymentServiceException(String message, Throwable cause) {
      super(message, cause);
    }
  }
}

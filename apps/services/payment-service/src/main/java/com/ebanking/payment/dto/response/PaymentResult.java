package com.ebanking.payment.dto.response;

import com.ebanking.payment.entity.Payment;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaymentResult {
  private boolean success;
  private Payment payment;
  private String error;
  private String message;

  public static PaymentResult success(Payment payment) {
    return PaymentResult.builder().success(true).payment(payment).build();
  }

  public static PaymentResult success(Payment payment, String message) {
    return PaymentResult.builder().success(true).payment(payment).message(message).build();
  }

  public static PaymentResult failure(Payment payment, Throwable error) {
    return PaymentResult.builder()
        .success(false)
        .payment(payment)
        .error(error != null ? error.getMessage() : "Unknown error")
        .build();
  }

  public boolean isSuccess() {
    return success;
  }
}

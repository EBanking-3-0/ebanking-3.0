package com.ebanking.payment.exception;

public class FraudDetectedException extends RuntimeException {
  public FraudDetectedException(String message) {
    super(message);
  }
}

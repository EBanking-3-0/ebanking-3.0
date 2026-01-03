package com.ebanking.payment.exception;

public class MfaVerificationFailedException extends RuntimeException {
  public MfaVerificationFailedException(String message) {
    super(message);
  }
}

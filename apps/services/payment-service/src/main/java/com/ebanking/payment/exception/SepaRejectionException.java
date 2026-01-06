package com.ebanking.payment.exception;

public class SepaRejectionException extends RuntimeException {
  public SepaRejectionException(String message) {
    super(message);
  }
}

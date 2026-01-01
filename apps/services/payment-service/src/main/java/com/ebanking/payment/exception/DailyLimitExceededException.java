package com.ebanking.payment.exception;

public class DailyLimitExceededException extends RuntimeException {
  public DailyLimitExceededException(String message) {
    super(message);
  }
}

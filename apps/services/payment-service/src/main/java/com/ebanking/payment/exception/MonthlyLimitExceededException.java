package com.ebanking.payment.exception;

public class MonthlyLimitExceededException extends RuntimeException {
  public MonthlyLimitExceededException(String message) {
    super(message);
  }
}

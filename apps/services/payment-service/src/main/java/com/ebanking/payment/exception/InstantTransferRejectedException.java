package com.ebanking.payment.exception;

public class InstantTransferRejectedException extends RuntimeException {
  public InstantTransferRejectedException(String message) {
    super(message);
  }
}

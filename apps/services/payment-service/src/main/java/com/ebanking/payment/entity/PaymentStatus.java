package com.ebanking.payment.entity;

public enum PaymentStatus {
  CREATED, // Transaction objective captured
  VALIDATED, // Business rules and compliance passed
  AUTHORIZED, // MFA / SCA verified
  RESERVED, // Amount blocked in account balance
  SENT, // Dispatched to clearing or external bank
  SETTLED, // Acknowledged by the clearing system
  COMPLETED, // Final state, ledger updated
  FAILED, // Technical or business failure
  REJECTED, // Denied by compliance or fraud
  CANCELLED, // Aborted by user before execution
  COMPENSATED // Refunded after a partial failure
}

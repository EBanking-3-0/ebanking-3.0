package com.ebanking.notification.enums;

/** Types of notifications that can be sent through the system. */
public enum NotificationType {
  /** Welcome notification when user is created */
  WELCOME,

  /** Transaction completed notification */
  TRANSACTION,

  /** Fraud alert notification */
  FRAUD_ALERT,

  /** Balance or budget alert */
  ALERT,

  /** Account created notification */
  ACCOUNT_CREATED,

  /** Payment failed notification */
  PAYMENT_FAILED,

  /** Login notification */
  LOGIN,

  /** MFA verification notification */
  MFA,

  /** General system notification */
  SYSTEM,

  /** Crypto trade notification */
  CRYPTO_TRADE,

  /** Custom notification */
  CUSTOM
}

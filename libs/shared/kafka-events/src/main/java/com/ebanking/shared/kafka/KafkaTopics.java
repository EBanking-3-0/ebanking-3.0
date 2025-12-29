package com.ebanking.shared.kafka;

/**
 * Constants for Kafka topic names used across the E-Banking system.
 * All topic names should be referenced from this class to ensure consistency.
 */
public final class KafkaTopics {

    private KafkaTopics() {
        // Utility class - prevent instantiation
    }

    // User Events
    public static final String USER_CREATED = "user.created";
    public static final String USER_UPDATED = "user.updated";

    // Account Events
    public static final String ACCOUNT_CREATED = "account.created";
    public static final String BALANCE_UPDATED = "balance.updated";

    // Transaction Events
    public static final String TRANSACTION_COMPLETED = "transaction.completed";
    public static final String TRANSACTION_FAILED = "transaction.failed";

    // Payment Events
    public static final String PAYMENT_FAILED = "payment.failed";

    // Security Events
    public static final String FRAUD_DETECTED = "fraud.detected";
    public static final String AUTH_LOGIN = "auth.login";
    public static final String MFA_VERIFIED = "mfa.verified";

    // Crypto Events
    public static final String CRYPTO_TRADE_EXECUTED = "crypto.trade.executed";

    // Notification Events
    public static final String NOTIFICATION_SENT = "notification.sent";
    public static final String NOTIFICATION_FAILED = "notification.failed";

    // Analytics Events
    public static final String ALERT_TRIGGERED = "alert.triggered";

    // Audit Events (consumed by audit service)
    public static final String AUDIT_LOG_CREATED = "audit.log.created";
    public static final String COMPLIANCE_ALERT = "compliance.alert";
}

package com.ebanking.notification.consumer;

import com.ebanking.notification.entity.Notification;
import com.ebanking.notification.service.NotificationService;
import com.ebanking.shared.kafka.KafkaTopics;
import com.ebanking.shared.kafka.events.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Consumer for notification-related events. Sends notifications (email/SMS/push) when events occur.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationConsumer {

    private final NotificationService notificationService;

    @KafkaListener(topics = KafkaTopics.USER_CREATED)
    public void handleUserCreated(@Payload UserCreatedEvent event, Acknowledgment acknowledgment) {
        try {
            log.info("Received user.created event for user: {}", event.getUserId());

            // Send welcome email using template
            Map<String, Object> templateData = new HashMap<>();
            templateData.put("firstName", event.getFirstName());
            templateData.put("email", event.getEmail());
            templateData.put("userId", event.getUserId());
            templateData.put("registrationDate", LocalDateTime.now());

            notificationService.sendTemplatedEmail(
                    event.getUserId(),
                    event.getEmail(),
                    "welcome-email",
                    templateData);

            acknowledgment.acknowledge();
            log.info("Processed user.created event for user: {}", event.getUserId());

        } catch (Exception e) {
            log.error("Failed to process user.created event: {}", event.getEventId(), e);
            acknowledgment.acknowledge(); // Acknowledge to prevent blocking
        }
    }

    @KafkaListener(topics = KafkaTopics.TRANSACTION_COMPLETED)
    public void handleTransactionCompleted(@Payload TransactionCompletedEvent event, Acknowledgment acknowledgment) {
        try {
            log.info("Received transaction.completed event: {}", event.getTransactionId());
            
            // Send transaction notification using template
            Map<String, Object> templateData = new HashMap<>();
            templateData.put("transactionId", event.getTransactionId());
            templateData.put("fromAccount", formatAccountId(event.getFromAccountId()));
            templateData.put("toAccount", formatAccountId(event.getToAccountId()));
            templateData.put("amount", event.getAmount());
            templateData.put("currency", event.getCurrency());
            templateData.put("transactionDate", LocalDateTime.now());
            
            // Note: In production, fetch user email from account service
            // For now, using a placeholder
            notificationService.sendTemplatedEmail(
                event.getToAccountId(), // Using account ID as userId placeholder
                "user@example.com", // Would fetch from user/account service
                "transaction-email",
                templateData
            );
            
            acknowledgment.acknowledge();
            
        } catch (Exception e) {
            log.error("Failed to process transaction.completed event: {}", event.getEventId(), e);
            acknowledgment.acknowledge();
        }
    }

    @KafkaListener(topics = KafkaTopics.PAYMENT_FAILED)
    public void handlePaymentFailed(@Payload PaymentFailedEvent event, Acknowledgment acknowledgment) {
        try {
            log.info("Received payment.failed event: {}", event.getTransactionId());
            
            // Send payment failure notification
            String message = String.format(
                "Your payment transaction %s has failed. Reason: %s. Please try again or contact support.",
                event.getTransactionId(),
                event.getFailureReason()
            );
            
            notificationService.sendSimpleEmail(
                event.getAccountId(),
                "user@example.com", // Would fetch from account service
                "Payment Failed",
                message,
                Notification.NotificationType.PAYMENT_FAILED
            );
            
            acknowledgment.acknowledge();
            
        } catch (Exception e) {
            log.error("Failed to process payment.failed event: {}", event.getEventId(), e);
            acknowledgment.acknowledge();
        }
    }

    @KafkaListener(topics = KafkaTopics.FRAUD_DETECTED)
    public void handleFraudDetected(@Payload FraudDetectedEvent event, Acknowledgment acknowledgment) {
        try {
            log.warn("Received fraud.detected event: {} - Severity: {}", event.getTransactionId(), event.getSeverity());
            
            // Send fraud alert using template
            Map<String, Object> templateData = new HashMap<>();
            templateData.put("fraudType", event.getFraudType());
            templateData.put("severity", event.getSeverity());
            templateData.put("transactionId", event.getTransactionId());
            templateData.put("detectedAt", LocalDateTime.now());
            templateData.put("description", event.getDescription());
            
            notificationService.sendTemplatedEmail(
                event.getAccountId(),
                "user@example.com", // Would fetch from account service
                "fraud-alert-email",
                templateData
            );
            
            acknowledgment.acknowledge();
            
        } catch (Exception e) {
            log.error("Failed to process fraud.detected event: {}", event.getEventId(), e);
            acknowledgment.acknowledge();
        }
    }

    @KafkaListener(topics = KafkaTopics.CRYPTO_TRADE_EXECUTED)
    public void handleCryptoTradeExecuted(@Payload CryptoTradeExecutedEvent event, Acknowledgment acknowledgment) {
        try {
            log.info("Received crypto.trade.executed event: {}", event.getTradeId());
            
            // Send trade confirmation
            String message = String.format(
                "Your crypto trade has been executed successfully.\n" +
                "Trade ID: %s\n" +
                "Type: %s\n" +
                "Cryptocurrency: %s\n" +
                "Amount: %s\n" +
                "Price: %s %s",
                event.getTradeId(),
                event.getTradeType(),
                event.getCryptoCurrency(),
                event.getCryptoAmount(),
                event.getFiatAmount(),
                event.getFiatCurrency()
            );
            notificationService.sendSimpleEmail(
                event.getUserId(),
                "user@example.com", // Would fetch from user service
                "Crypto Trade Executed",
                message,
                Notification.NotificationType.CRYPTO_TRADE
            );
            
            acknowledgment.acknowledge();
            
        } catch (Exception e) {
            log.error("Failed to process crypto.trade.executed event: {}", event.getEventId(), e);
            acknowledgment.acknowledge();
        }
    }

    @KafkaListener(topics = KafkaTopics.ALERT_TRIGGERED)
    public void handleAlertTriggered(@Payload AlertTriggeredEvent event, Acknowledgment acknowledgment) {
        try {
            log.info("Received alert.triggered event: {} - Type: {}", event.getAlertId(), event.getAlertType());
            
            // Send alert notification using template
            Map<String, Object> templateData = new HashMap<>();
            templateData.put("subject", "Alert: " + event.getAlertType());
            templateData.put("alertType", event.getAlertType());
            templateData.put("message", event.getMessage());
            
            notificationService.sendTemplatedEmail(
                event.getUserId(),
                "user@example.com", // Would fetch from user service
                "alert-email",
                templateData
            );
            
            acknowledgment.acknowledge();
            
        } catch (Exception e) {
            log.error("Failed to process alert.triggered event: {}", event.getEventId(), e);
            acknowledgment.acknowledge();
        }
    }

    /**
     * Helper method to format account ID for display
     */
    private String formatAccountId(Long accountId) {
        if (accountId == null) {
            return "****";
        }
        String id = accountId.toString();
        if (id.length() > 4) {
            return "****" + id.substring(id.length() - 4);
        }
        return "****" + id;
    }
}

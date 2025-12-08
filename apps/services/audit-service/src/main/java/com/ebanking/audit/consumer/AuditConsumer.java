package com.ebanking.audit.consumer;

import com.ebanking.shared.kafka.KafkaTopics;
import com.ebanking.shared.kafka.consumer.BaseEventConsumer;
import com.ebanking.shared.kafka.events.BaseEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.stereotype.Component;

/**
 * Consumer for audit events.
 * Logs all events to MongoDB for compliance and audit trail.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuditConsumer extends BaseEventConsumer {

    // Would inject AuditRepository here
    // private final AuditRepository auditRepository;

    @KafkaListener(topics = {
        KafkaTopics.USER_CREATED,
        KafkaTopics.USER_UPDATED,
        KafkaTopics.ACCOUNT_CREATED,
        KafkaTopics.BALANCE_UPDATED,
        KafkaTopics.TRANSACTION_COMPLETED,
        KafkaTopics.PAYMENT_FAILED,
        KafkaTopics.FRAUD_DETECTED,
        KafkaTopics.AUTH_LOGIN,
        KafkaTopics.MFA_VERIFIED,
        KafkaTopics.CRYPTO_TRADE_EXECUTED,
        KafkaTopics.NOTIFICATION_SENT,
        KafkaTopics.ALERT_TRIGGERED
    })
    public void handleAuditEvent(
            @Payload BaseEvent event,
            Acknowledgment acknowledgment,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {
        
        processEvent(event, acknowledgment, partition, offset, () -> {
            // Log event to audit trail
            log.info("Audit log - Event: {} | ID: {} | Source: {} | Timestamp: {}", 
                event.getEventType(), event.getEventId(), event.getSource(), event.getTimestamp());
            
            // In production, would save to MongoDB:
            // AuditLog auditLog = AuditLog.builder()
            //     .eventId(event.getEventId())
            //     .eventType(event.getEventType())
            //     .timestamp(event.getTimestamp())
            //     .source(event.getSource())
            //     .correlationId(event.getCorrelationId())
            //     .version(event.getVersion())
            //     .payload(event) // Serialized event
            //     .build();
            // auditRepository.save(auditLog);
        });
    }

    @Override
    protected void handleEvent(BaseEvent event) {
        // This method is not used since we override processEvent directly
        // But required by abstract class
    }
}


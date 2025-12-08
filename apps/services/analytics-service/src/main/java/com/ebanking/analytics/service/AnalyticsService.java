package com.ebanking.analytics.service;

import com.ebanking.shared.kafka.events.AlertTriggeredEvent;
import com.ebanking.shared.kafka.producer.TypedEventProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * Analytics service with Kafka event publishing.
 * Publishes alert.triggered events when thresholds are exceeded.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final TypedEventProducer eventProducer;

    public void triggerAlert(Long alertId, Long userId, String alertType, String severity, 
                            String message, BigDecimal threshold, BigDecimal currentValue, String accountNumber) {
        // Alert logic would go here
        log.warn("Triggering alert: {} - Type: {} - Severity: {}", alertId, alertType, severity);
        
        // Publish alert triggered event
        AlertTriggeredEvent event = AlertTriggeredEvent.builder()
            .alertId(alertId)
            .userId(userId)
            .alertType(alertType)
            .severity(severity)
            .message(message)
            .threshold(threshold)
            .currentValue(currentValue)
            .accountNumber(accountNumber)
            .source("analytics-service")
            .build();
        
        eventProducer.publishAlertTriggered(event);
        log.info("Published alert.triggered event: {}", alertId);
    }
}


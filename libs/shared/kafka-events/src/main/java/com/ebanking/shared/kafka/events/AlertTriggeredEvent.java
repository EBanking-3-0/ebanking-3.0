package com.ebanking.shared.kafka.events;

import com.ebanking.shared.kafka.KafkaTopics;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

/**
 * Event published when a budget or threshold alert is triggered.
 * Published by: Analytics Service
 * Consumed by: Notification Service
 */
@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class AlertTriggeredEvent extends BaseEvent {
    
    private Long alertId;
    private Long userId;
    private String alertType; // BUDGET_EXCEEDED, LOW_BALANCE, HIGH_SPENDING, etc.
    private String severity; // INFO, WARNING, CRITICAL
    private String message;
    private BigDecimal threshold;
    private BigDecimal currentValue;
    private String accountNumber;
    
    public AlertTriggeredEvent() {
        super(KafkaTopics.ALERT_TRIGGERED);
    }
    
    public AlertTriggeredEvent(Long alertId, Long userId, String alertType,
                              String severity, String message, BigDecimal threshold,
                              BigDecimal currentValue, String accountNumber) {
        super(KafkaTopics.ALERT_TRIGGERED);
        this.alertId = alertId;
        this.userId = userId;
        this.alertType = alertType;
        this.severity = severity;
        this.message = message;
        this.threshold = threshold;
        this.currentValue = currentValue;
        this.accountNumber = accountNumber;
    }
}


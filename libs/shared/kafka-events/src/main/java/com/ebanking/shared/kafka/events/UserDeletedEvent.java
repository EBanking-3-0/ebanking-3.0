package com.ebanking.shared.kafka.events;

import com.ebanking.shared.kafka.KafkaTopics;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

/**
 * Event published when a user is deleted (soft or hard delete).
 * Published by: User Service
 * Consumed by: Notification Service, Account Service, Analytics Service, Audit Service
 */
@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class UserDeletedEvent extends BaseEvent {

    private Long userId;
    private String reason; // optional: e.g., "user_request", "fraud", "inactive"

    public UserDeletedEvent() {
        super(KafkaTopics.USER_DELETED);
    }

    public UserDeletedEvent(Long userId, String reason) {
        super(KafkaTopics.USER_DELETED);
        this.userId = userId;
        this.reason = reason;
    }

    // Convenience constructor if reason is not needed
    public UserDeletedEvent(Long userId) {
        this(userId, null);
    }
}
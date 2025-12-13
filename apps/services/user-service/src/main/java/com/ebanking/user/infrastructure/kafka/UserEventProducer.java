/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ebanking.user.infrastructure.kafka;

import com.ebanking.user.domain.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

// For simplicity, we use String payloads here.

@Component
@RequiredArgsConstructor
@Slf4j
public class UserEventProducer {

    private static final String USER_TOPIC = "user-events";
    private final KafkaTemplate<String, String> kafkaTemplate;

    public void publishUserCreatedEvent(User user) {
        String payload = String.format("User created: ID=%d, Email=%s", user.getId(), user.getEmail());
        kafkaTemplate.send(USER_TOPIC, user.getId().toString(), payload);
        log.info("Published UserCreated event for ID: {}", user.getId());
    }
    
    public void publishKycUpdatedEvent(User user) {
        String payload = String.format("KYC updated: ID=%d, Status=%s", user.getId(), user.getKycStatus().name());
        kafkaTemplate.send(USER_TOPIC, user.getId().toString(), payload);
        log.info("Published KycUpdated event for ID: {}", user.getId());
    }
    
    public void publishUserDeletedEvent(Long userId) {
        String payload = String.format("User deleted: ID=%d", userId);
        kafkaTemplate.send(USER_TOPIC, userId.toString(), payload);
        log.warn("Published UserDeleted event for ID: {}", userId);
    }
}
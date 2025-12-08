package com.ebanking.crypto.consumer;

import com.ebanking.shared.kafka.KafkaTopics;
import com.ebanking.shared.kafka.events.AccountCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * Consumer for account events.
 * Initializes crypto wallet when account is created.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CryptoConsumer {

    // Would inject CryptoService here
    // private final CryptoService cryptoService;

    @KafkaListener(topics = KafkaTopics.ACCOUNT_CREATED)
    public void handleAccountCreated(@Payload AccountCreatedEvent event, Acknowledgment acknowledgment) {
        try {
            log.info("Received account.created event: {} for user: {}", event.getAccountId(), event.getUserId());
            
            // Initialize crypto wallet for new account
            // cryptoService.initializeWallet(event.getAccountId(), event.getUserId());
            
            log.info("Processed account.created event: {}", event.getAccountId());
            acknowledgment.acknowledge();
            
        } catch (Exception e) {
            log.error("Failed to process account.created event: {}", event.getEventId(), e);
            acknowledgment.acknowledge(); // Acknowledge to prevent blocking
        }
    }
}


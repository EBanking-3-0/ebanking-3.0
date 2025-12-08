package com.ebanking.shared.kafka.producer;

import com.ebanking.shared.kafka.events.BaseEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Generic event producer service for publishing Kafka events.
 * Provides methods to publish events to Kafka topics.
 * 
 * This class is automatically configured via KafkaEventsAutoConfiguration.
 * We can also create it manually if needed.
 */
@Slf4j
@RequiredArgsConstructor
public class EventProducer {
    
    private final KafkaTemplate<String, BaseEvent> kafkaTemplate;
    
    /**
     * Publishes an event to the specified topic.
     * 
     * @param topic The Kafka topic name
     * @param event The event to publish
     */
    public void publishEvent(String topic, BaseEvent event) {
        if (event.getEventId() == null) {
            event.setEventId(UUID.randomUUID().toString());
        }
        if (event.getTimestamp() == null) {
            event.setTimestamp(java.time.Instant.now());
        }
        
        String key = event.getEventId();
        
        log.debug("Publishing event to topic {}: {}", topic, event);
        
        CompletableFuture<SendResult<String, BaseEvent>> future = 
            kafkaTemplate.send(topic, key, event);
        
        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("Event published successfully to topic {}: offset={}", 
                    topic, result.getRecordMetadata().offset());
            } else {
                log.error("Failed to publish event to topic {}: {}", topic, ex.getMessage(), ex);
                // In production, We may want to implement retry logic or dead letter queue
            }
        });
    }
    
    /**
     * Publishes an event synchronously (blocks until completion).
     * Use with caution as it may impact performance.
     * 
     * @param topic The Kafka topic name
     * @param event The event to publish
     */
    public void publishEventSync(String topic, BaseEvent event) {
        if (event.getEventId() == null) {
            event.setEventId(UUID.randomUUID().toString());
        }
        if (event.getTimestamp() == null) {
            event.setTimestamp(java.time.Instant.now());
        }
        
        String key = event.getEventId();
        
        try {
            SendResult<String, BaseEvent> result = kafkaTemplate.send(topic, key, event).get();
            log.info("Event published synchronously to topic {}: offset={}", 
                topic, result.getRecordMetadata().offset());
        } catch (Exception e) {
            log.error("Failed to publish event synchronously to topic {}: {}", 
                topic, e.getMessage(), e);
            throw new RuntimeException("Failed to publish event", e);
        }
    }
}


package com.ebanking.shared.kafka.consumer;

import com.ebanking.shared.kafka.events.BaseEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;

/**
 * Base class for event consumers.
 * Provides common error handling and acknowledgment patterns.
 * 
 * Subclasses should use @KafkaListener annotation and call processEvent() 
 * to handle events with proper error handling and acknowledgment.
 * 
 * Example:
 * <pre>
 * {@code
 * @Component
 * public class MyEventConsumer extends BaseEventConsumer {
 *     @KafkaListener(topics = KafkaTopics.USER_CREATED)
 *     public void consume(@Payload UserCreatedEvent event, Acknowledgment ack) {
 *         processEvent(event, ack, () -> {
 *             // Business logic here
 *             handleUserCreated(event);
 *         });
 *     }
 * }
 * }
 * </pre>
 */
@Slf4j
public abstract class BaseEventConsumer {
    
    /**
     * Processes an event with error handling and acknowledgment.
     * Subclasses should call this method from their @KafkaListener methods.
     * 
     * @param event The event to process
     * @param acknowledgment Kafka acknowledgment for manual commit
     * @param handler Runnable containing the business logic
     */
    protected void processEvent(BaseEvent event, Acknowledgment acknowledgment, Runnable handler) {
        processEvent(event, acknowledgment, null, 0L, handler);
    }
    
    /**
     * Processes an event with error handling and acknowledgment.
     * Includes partition and offset information for logging.
     * 
     * @param event The event to process
     * @param acknowledgment Kafka acknowledgment for manual commit
     * @param partition Partition from which the event was received
     * @param offset Offset of the event
     * @param handler Runnable containing the business logic
     */
    protected void processEvent(BaseEvent event, Acknowledgment acknowledgment, 
                               Integer partition, Long offset, Runnable handler) {
        try {
            if (partition != null && offset != null) {
                log.debug("Received event: {} from partition {} at offset {}", 
                    event.getEventType(), partition, offset);
            } else {
                log.debug("Received event: {}", event.getEventType());
            }
            
            handler.run();
            
            // Acknowledge successful processing
            if (acknowledgment != null) {
                acknowledgment.acknowledge();
            }
            
            log.debug("Successfully processed event: {}", event.getEventId());
            
        } catch (Exception e) {
            log.error("Error processing event {}: {}", event.getEventId(), e.getMessage(), e);
            // In production, We may want to implement retry logic or send to dead letter topic
            // For now, We'll acknowledge to prevent blocking, but We may want to handle differently
            if (acknowledgment != null) {
                acknowledgment.acknowledge();
            }
            throw e; // Re-throw to allow Spring Kafka error handlers to process
        }
    }
}


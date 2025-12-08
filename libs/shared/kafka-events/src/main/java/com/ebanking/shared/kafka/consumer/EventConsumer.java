package com.ebanking.shared.kafka.consumer;

import com.ebanking.shared.kafka.events.BaseEvent;

/**
 * Functional interface for event consumers.
 * Can be used with lambda expressions for simple event handling.
 */
@FunctionalInterface
public interface EventConsumer {
    
    /**
     * Handles an event.
     * 
     * @param event The event to handle
     */
    void handle(BaseEvent event);
}


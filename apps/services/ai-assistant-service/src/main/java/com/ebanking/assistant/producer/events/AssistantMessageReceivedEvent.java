package com.ebanking.assistant.producer.events;

import com.ebanking.shared.kafka.events.BaseEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Event published when AI assistant processes a user message
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@lombok.EqualsAndHashCode(callSuper=true)
public class AssistantMessageReceivedEvent extends BaseEvent {
    private String userId;
    private String conversationId;
    private String message;
    private String response;
}

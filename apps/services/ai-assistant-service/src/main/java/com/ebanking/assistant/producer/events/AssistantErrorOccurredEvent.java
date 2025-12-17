package com.ebanking.assistant.producer.events;

import com.ebanking.shared.kafka.events.BaseEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Event published when an error occurs in the AI assistant service
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor@lombok.EqualsAndHashCode(callSuper=true)public class AssistantErrorOccurredEvent extends BaseEvent {
    private String userId;
    private String conversationId;
    private String errorType;
    private String errorMessage;
}

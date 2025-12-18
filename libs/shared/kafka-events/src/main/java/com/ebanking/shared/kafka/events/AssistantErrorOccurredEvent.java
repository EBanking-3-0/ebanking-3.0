package com.ebanking.shared.kafka.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/** Event published when an error occurs in the AI assistant service */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class AssistantErrorOccurredEvent extends BaseEvent {
    private Long userId;
    private String conversationId;
    private String errorType;
    private String errorMessage;
}

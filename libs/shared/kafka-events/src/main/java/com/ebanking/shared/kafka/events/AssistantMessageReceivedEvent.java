package com.ebanking.shared.kafka.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/** Event published when AI assistant processes a user message */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class AssistantMessageReceivedEvent extends BaseEvent {
    private Long userId;
    private String conversationId;
    private String message;
    private String response;
}

package com.ebanking.shared.kafka.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/** Event published when a new conversation is started with the AI assistant */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class AssistantConversationStartedEvent extends BaseEvent {
    private Long userId;
    private String conversationId;
    private String sessionId;
}

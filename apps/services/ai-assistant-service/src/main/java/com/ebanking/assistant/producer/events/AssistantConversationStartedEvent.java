package com.ebanking.assistant.producer.events;

import com.ebanking.shared.kafka.events.BaseEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/** Event published when a new conversation is started with the AI assistant */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@lombok.EqualsAndHashCode(callSuper = true)
public class AssistantConversationStartedEvent extends BaseEvent {
  private String userId;
  private String conversationId;
  private String sessionId;
}

package com.ebanking.assistant.producer.events;

import com.ebanking.shared.kafka.events.BaseEvent;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/** Event published when AI assistant executes an action */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@lombok.EqualsAndHashCode(callSuper = true)
public class AssistantActionExecutedEvent extends BaseEvent {
  private String userId;
  private String conversationId;
  private String actionName;
  private Map<String, Object> actionResult;
  private boolean success;
}

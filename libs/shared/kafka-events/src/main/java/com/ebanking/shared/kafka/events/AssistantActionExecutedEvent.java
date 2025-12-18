package com.ebanking.shared.kafka.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import java.util.Map;

/**
 * Event published when AI assistant executes an action
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class AssistantActionExecutedEvent extends BaseEvent {
    private Long userId;
    private String conversationId;
    private String actionName;
    private Map<String, Object> actionResult;
    private boolean success;
}

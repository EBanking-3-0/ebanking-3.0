package com.ebanking.assistant.producer;

import com.ebanking.shared.kafka.KafkaTopics;
import com.ebanking.shared.kafka.events.*;
import com.ebanking.shared.kafka.producer.EventProducer;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AssistantEventProducer {

  private final EventProducer eventProducer;

  public void publishMessageReceived(Long userId, String conversationId, String message) {
    AssistantMessageReceivedEvent event =
        AssistantMessageReceivedEvent.builder()
            .userId(userId)
            .conversationId(conversationId)
            .message(message)
            .source("ai-assistant-service")
            .eventType("assistant.message.received")
            .build();

    eventProducer.publishEvent(KafkaTopics.ASSISTANT_MESSAGE_RECEIVED, event);
    log.info("Published assistant.message.received event for user {}", userId);
  }

  public void publishActionExecuted(
      Long userId, String actionName, Map<String, Object> actionResult) {
    AssistantActionExecutedEvent event =
        AssistantActionExecutedEvent.builder()
            .userId(userId)
            .actionName(actionName)
            .actionResult(actionResult)
            .success(true)
            .source("ai-assistant-service")
            .eventType("assistant.action.executed")
            .build();

    eventProducer.publishEvent(KafkaTopics.ASSISTANT_ACTION_EXECUTED, event);
    log.info("Published assistant.action.executed event: {} for user {}", actionName, userId);
  }

  public void publishConversationStarted(Long userId, String conversationId, String sessionId) {
    AssistantConversationStartedEvent event =
        AssistantConversationStartedEvent.builder()
            .userId(userId)
            .conversationId(conversationId)
            .sessionId(sessionId)
            .source("ai-assistant-service")
            .eventType("assistant.conversation.started")
            .build();

    eventProducer.publishEvent(KafkaTopics.ASSISTANT_CONVERSATION_STARTED, event);
    log.info("Published assistant.conversation.started event for user {}", userId);
  }

  public void publishErrorOccurred(
      Long userId, String errorType, String errorMessage, String conversationId) {
    AssistantErrorOccurredEvent event =
        AssistantErrorOccurredEvent.builder()
            .userId(userId)
            .conversationId(conversationId)
            .errorType(errorType)
            .errorMessage(errorMessage)
            .source("ai-assistant-service")
            .eventType("assistant.error.occurred")
            .build();

    eventProducer.publishEvent(KafkaTopics.ASSISTANT_ERROR_OCCURRED, event);
    log.warn("Published assistant.error.occurred event: {} for user {}", errorType, userId);
  }
}

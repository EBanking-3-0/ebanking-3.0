package com.ebanking.assistant.producer;

import com.ebanking.assistant.producer.events.AssistantActionExecutedEvent;
import com.ebanking.assistant.producer.events.AssistantConversationStartedEvent;
import com.ebanking.assistant.producer.events.AssistantErrorOccurredEvent;
import com.ebanking.assistant.producer.events.AssistantMessageReceivedEvent;
import com.ebanking.shared.kafka.KafkaTopics;
import com.ebanking.shared.kafka.producer.EventProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

/**
 * Component for publishing AI Assistant events using the shared Kafka events library.
 * All events extend BaseEvent and use the standardized EventProducer.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AssistantEventProducer {
    
    private final EventProducer eventProducer;
    private static final String SERVICE_SOURCE = "ai-assistant-service";
    
    /**
     * Publish event when assistant receives and processes a user message
     */
    public void publishMessageReceived(String userId, String conversationId, String message, String response) {
        AssistantMessageReceivedEvent event = AssistantMessageReceivedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("assistant.message.received")
                .source(SERVICE_SOURCE)
                .correlationId(conversationId)
                .userId(userId)
                .conversationId(conversationId)
                .message(message)
                .response(response)
                .build();
        
        eventProducer.publishEvent(KafkaTopics.ASSISTANT_MESSAGE_RECEIVED, event);
        log.info("Published ASSISTANT_MESSAGE_RECEIVED event for user {} in conversation {}", userId, conversationId);
    }
    
    /**
     * Publish event when assistant executes an action
     */
    public void publishActionExecuted(String userId, String conversationId, String actionName, 
                                       Map<String, Object> actionResult, boolean success) {
        AssistantActionExecutedEvent event = AssistantActionExecutedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("assistant.action.executed")
                .source(SERVICE_SOURCE)
                .correlationId(conversationId)
                .userId(userId)
                .conversationId(conversationId)
                .actionName(actionName)
                .actionResult(actionResult)
                .success(success)
                .build();
        
        eventProducer.publishEvent(KafkaTopics.ASSISTANT_ACTION_EXECUTED, event);
        log.info("Published ASSISTANT_ACTION_EXECUTED event: {} for user {} (success={})", 
                 actionName, userId, success);
    }
    
    /**
     * Publish event when a new conversation is started
     */
    public void publishConversationStarted(String userId, String conversationId, String sessionId) {
        AssistantConversationStartedEvent event = AssistantConversationStartedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("assistant.conversation.started")
                .source(SERVICE_SOURCE)
                .correlationId(conversationId)
                .userId(userId)
                .conversationId(conversationId)
                .sessionId(sessionId)
                .build();
        
        eventProducer.publishEvent(KafkaTopics.ASSISTANT_CONVERSATION_STARTED, event);
        log.info("Published ASSISTANT_CONVERSATION_STARTED event for user {}, conversation {}", userId, conversationId);
    }
    
    /**
     * Publish event when an error occurs during assistant processing
     */
    public void publishErrorOccurred(String userId, String conversationId, String errorType, String errorMessage) {
        AssistantErrorOccurredEvent event = AssistantErrorOccurredEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("assistant.error.occurred")
                .source(SERVICE_SOURCE)
                .correlationId(conversationId)
                .userId(userId)
                .conversationId(conversationId)
                .errorType(errorType)
                .errorMessage(errorMessage)
                .build();
        
        eventProducer.publishEvent(KafkaTopics.ASSISTANT_ERROR_OCCURRED, event);
        log.warn("Published ASSISTANT_ERROR_OCCURRED event: {} for user {} in conversation {}", 
                 errorType, userId, conversationId);
    }
}


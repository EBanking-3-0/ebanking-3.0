package com.ebanking.assistant.producer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class AssistantEventProducer {
    
    private final KafkaTemplate<String, Object> kafkaTemplate;
    
    private static final String TOPIC_MESSAGE_RECEIVED = "assistant.message.received";
    private static final String TOPIC_ACTION_EXECUTED = "assistant.action.executed";
    private static final String TOPIC_CONVERSATION_STARTED = "assistant.conversation.started";
    private static final String TOPIC_ERROR_OCCURRED = "assistant.error.occurred";
    
    public void publishMessageReceived(Long userId, String conversationId, String message) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventId", UUID.randomUUID().toString());
        event.put("eventType", "assistant.message.received");
        event.put("timestamp", Instant.now().toString());
        event.put("userId", userId);
        event.put("conversationId", conversationId);
        event.put("message", message);
        event.put("source", "ai-assistant-service");
        
        kafkaTemplate.send(TOPIC_MESSAGE_RECEIVED, event);
        log.info("Published assistant.message.received event for user {}", userId);
    }
    
    public void publishActionExecuted(Long userId, String actionName, Map<String, Object> actionResult) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventId", UUID.randomUUID().toString());
        event.put("eventType", "assistant.action.executed");
        event.put("timestamp", Instant.now().toString());
        event.put("userId", userId);
        event.put("actionName", actionName);
        event.put("actionResult", actionResult);
        event.put("source", "ai-assistant-service");
        
        kafkaTemplate.send(TOPIC_ACTION_EXECUTED, event);
        log.info("Published assistant.action.executed event: {} for user {}", actionName, userId);
    }
    
    public void publishConversationStarted(Long userId, String conversationId, String sessionId) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventId", UUID.randomUUID().toString());
        event.put("eventType", "assistant.conversation.started");
        event.put("timestamp", Instant.now().toString());
        event.put("userId", userId);
        event.put("conversationId", conversationId);
        event.put("sessionId", sessionId);
        event.put("source", "ai-assistant-service");
        
        kafkaTemplate.send(TOPIC_CONVERSATION_STARTED, event);
        log.info("Published assistant.conversation.started event for user {}", userId);
    }
    
    public void publishErrorOccurred(Long userId, String errorType, String errorMessage, String conversationId) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventId", UUID.randomUUID().toString());
        event.put("eventType", "assistant.error.occurred");
        event.put("timestamp", Instant.now().toString());
        event.put("userId", userId);
        event.put("errorType", errorType);
        event.put("errorMessage", errorMessage);
        event.put("conversationId", conversationId);
        event.put("source", "ai-assistant-service");
        
        kafkaTemplate.send(TOPIC_ERROR_OCCURRED, event);
        log.warn("Published assistant.error.occurred event: {} for user {}", errorType, userId);
    }
}

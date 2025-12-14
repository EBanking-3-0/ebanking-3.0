package com.ebanking.assistant.consumer;

import com.ebanking.shared.kafka.consumer.BaseEventConsumer;
import com.ebanking.shared.kafka.events.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Consumes relevant Kafka events that might be useful for the AI assistant context.
 */
@Slf4j
@Component
public class AssistantEventConsumer extends BaseEventConsumer {
    
    @KafkaListener(topics = "${kafka.topics.user-created:user.created}", groupId = "ai-assistant-service")
    public void handleUserCreated(UserCreatedEvent event) {
        log.info("Received user.created event: userId={}, email={}", event.getUserId(), event.getEmail());
        // Could update user context cache or trigger welcome message
    }
    
    @KafkaListener(topics = "${kafka.topics.account-created:account.created}", groupId = "ai-assistant-service")
    public void handleAccountCreated(AccountCreatedEvent event) {
        log.info("Received account.created event: accountId={}, userId={}", 
                event.getAccountId(), event.getUserId());
        // Could update account context or notify user
    }
    
    @KafkaListener(topics = "${kafka.topics.transaction-completed:transaction.completed}", groupId = "ai-assistant-service")
    public void handleTransactionCompleted(TransactionCompletedEvent event) {
        log.info("Received transaction.completed event: transactionId={}, fromAccount={}, toAccount={}", 
                event.getTransactionId(), event.getFromAccountNumber(), event.getToAccountNumber());
        // Could provide proactive updates in active conversations
    }
    
    @KafkaListener(topics = "${kafka.topics.balance-updated:balance.updated}", groupId = "ai-assistant-service")
    public void handleBalanceUpdated(BalanceUpdatedEvent event) {
        log.info("Received balance.updated event: accountId={}, newBalance={}", 
                event.getAccountId(), event.getNewBalance());
        // Could update cached balance information
    }
}

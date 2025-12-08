package com.ebanking.account.service;

import com.ebanking.shared.kafka.events.AccountCreatedEvent;
import com.ebanking.shared.kafka.events.BalanceUpdatedEvent;
import com.ebanking.shared.kafka.producer.TypedEventProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * Account service with Kafka event publishing.
 * Publishes account.created and balance.updated events.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AccountService {

    private final TypedEventProducer eventProducer;

    @Transactional
    public void createAccount(Long userId, String accountNumber, String accountType, String currency) {
        // Account creation logic would go here
        log.info("Creating account: {} for user: {}", accountNumber, userId);
        
        // After account is created, publish event
        AccountCreatedEvent event = AccountCreatedEvent.builder()
            .accountId(1L) // Would be actual account ID
            .userId(userId)
            .accountNumber(accountNumber)
            .accountType(accountType)
            .currency(currency)
            .initialBalance(BigDecimal.ZERO)
            .source("account-service")
            .build();
        
        eventProducer.publishAccountCreated(event);
        log.info("Published account.created event for account: {}", accountNumber);
    }

    @Transactional
    public void updateBalance(Long accountId, String accountNumber, BigDecimal previousBalance, 
                             BigDecimal newBalance, BigDecimal amount, String operation, String reason) {
        // Balance update logic would go here
        log.info("Updating balance for account: {} - Operation: {} - Amount: {}", accountNumber, operation, amount);
        
        // After balance is updated, publish event
        BalanceUpdatedEvent event = BalanceUpdatedEvent.builder()
            .accountId(accountId)
            .accountNumber(accountNumber)
            .previousBalance(previousBalance)
            .newBalance(newBalance)
            .amount(amount)
            .operation(operation)
            .reason(reason)
            .source("account-service")
            .build();
        
        eventProducer.publishBalanceUpdated(event);
        log.info("Published balance.updated event for account: {}", accountNumber);
    }
}


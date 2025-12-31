package com.ebanking.account.kafka.producer;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.ebanking.shared.kafka.events.AccountCreatedEvent;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@AllArgsConstructor
public class AccountProducer {

    private final KafkaTemplate<String, AccountCreatedEvent> kafkaTemplate;

    public void sendAccountCreatedEvent(AccountCreatedEvent event) {
        kafkaTemplate.send("account-created", event);
    }

}

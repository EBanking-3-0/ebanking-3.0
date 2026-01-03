package com.ebanking.account.kafka.producer;

import com.ebanking.shared.kafka.events.AccountCreatedEvent;
import com.ebanking.shared.kafka.events.BaseEvent;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@AllArgsConstructor
public class AccountProducer {

  private final KafkaTemplate<String, BaseEvent> kafkaTemplate;

  public void sendAccountCreatedEvent(AccountCreatedEvent event) {
    kafkaTemplate.send("account-created", event);
  }
}

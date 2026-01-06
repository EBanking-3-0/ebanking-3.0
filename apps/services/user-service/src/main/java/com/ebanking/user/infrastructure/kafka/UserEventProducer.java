package com.ebanking.user.infrastructure.kafka;

import com.ebanking.shared.kafka.KafkaTopics;
import com.ebanking.shared.kafka.events.BaseEvent;
import com.ebanking.shared.kafka.events.UserCreatedEvent;
import com.ebanking.shared.kafka.events.UserDeletedEvent;
import com.ebanking.user.domain.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserEventProducer {

  private final KafkaTemplate<String, BaseEvent> kafkaTemplate;

  public void publishUserCreatedEvent(User user) {
    // Convert UUID to stable long value for userId
    long userId = user.getId().getMostSignificantBits() & Long.MAX_VALUE;

    UserCreatedEvent event =
        UserCreatedEvent.builder()
            .userId(userId)
            .username(user.getFirstName() + " " + user.getLastName())
            .email(user.getEmail())
            .firstName(user.getFirstName())
            .lastName(user.getLastName())
            .status(user.getStatus().name())
            .build();

    kafkaTemplate.send(KafkaTopics.USER_CREATED, user.getId().toString(), event);
    log.info("Published UserCreatedEvent for user ID: {}", user.getId());
  }

  public void publishUserDeletedEvent(Long userId, String reason) {
    UserDeletedEvent event = new UserDeletedEvent(userId, reason);

    kafkaTemplate.send(KafkaTopics.USER_UPDATED, userId.toString(), event);
    log.warn("Published UserDeletedEvent for user ID: {}, reason: {}", userId, reason);
  }

  // Overload without reason
  public void publishUserDeletedEvent(Long userId) {
    publishUserDeletedEvent(userId, null);
  }
}

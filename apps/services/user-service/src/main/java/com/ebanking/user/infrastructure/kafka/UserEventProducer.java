package com.ebanking.user.infrastructure.kafka;

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

  private static final String USER_TOPIC =
      "user-events"; // or use KafkaTopics directly if preferred

  private final KafkaTemplate<String, BaseEvent> kafkaTemplate;

  public void publishUserCreatedEvent(User user) {
    UserCreatedEvent event =
        UserCreatedEvent.builder()
            .userId(0L)
            .email(user.getEmail())
            .firstName(user.getFirstName())
            .lastName(user.getLastName())
            .status("ACTIVE") // or whatever initial status
            .build();

    kafkaTemplate.send(USER_TOPIC, user.getId().toString(), event);
    log.info("Published UserCreatedEvent for user ID: {}", user.getId());
  }

  public void publishUserDeletedEvent(Long userId, String reason) {
    UserDeletedEvent event = new UserDeletedEvent(userId, reason);

    kafkaTemplate.send(USER_TOPIC, userId.toString(), event);
    log.warn("Published UserDeletedEvent for user ID: {}, reason: {}", userId, reason);
  }

  // Overload without reason
  public void publishUserDeletedEvent(Long userId) {
    publishUserDeletedEvent(userId, null);
  }
}

package com.ebanking.shared.kafka.events;

import com.ebanking.shared.kafka.KafkaTopics;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

/**
 * Event published when a new user is created/registered. Published by: User Service Consumed by:
 * Notification Service, Analytics Service, Audit Service
 */
@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class UserCreatedEvent extends BaseEvent {

  private String userId;
  private String email;
  private String username;
  private String firstName;
  private String lastName;
  private String status;

  public UserCreatedEvent() {
    super(KafkaTopics.USER_CREATED);
  }

  public UserCreatedEvent(
      String userId,
      String email,
      String username,
      String firstName,
      String lastName,
      String status) {
    super(KafkaTopics.USER_CREATED);
    this.userId = userId;
    this.email = email;
    this.username = username;
    this.firstName = firstName;
    this.lastName = lastName;
    this.status = status;
  }
}

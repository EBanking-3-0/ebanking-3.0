package com.ebanking.shared.kafka.events;

import com.ebanking.shared.kafka.KafkaTopics;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

/**
 * Event published when a user profile is updated. Published by: User Service Consumed by: Analytics
 * Service, Audit Service
 */
@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class UserUpdatedEvent extends BaseEvent {

  private Long userId;
  private String email;
  private String firstName;
  private String lastName;
  private String status;
  private String updatedFields;

  public UserUpdatedEvent() {
    super(KafkaTopics.USER_UPDATED);
  }

  public UserUpdatedEvent(
      Long userId,
      String email,
      String firstName,
      String lastName,
      String status,
      String updatedFields) {
    super(KafkaTopics.USER_UPDATED);
    this.userId = userId;
    this.email = email;
    this.firstName = firstName;
    this.lastName = lastName;
    this.status = status;
    this.updatedFields = updatedFields;
  }
}

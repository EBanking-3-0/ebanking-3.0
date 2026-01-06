package com.ebanking.shared.kafka.events;

import com.ebanking.shared.kafka.KafkaTopics;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

/**
 * Event published when a user's KYC status is updated. Published by: User Service Consumed by:
 * Notification Service, Fraud Service, Analytics Service, Audit Service
 */
@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class KycUpdatedEvent extends BaseEvent {

  private Long userId;
  private String previousKycStatus;
  private String newKycStatus;

  public KycUpdatedEvent() {
    super(KafkaTopics.USER_KYC_UPDATED);
  }

  public KycUpdatedEvent(Long userId, String previousKycStatus, String newKycStatus) {
    super(KafkaTopics.USER_KYC_UPDATED);
    this.userId = userId;
    this.previousKycStatus = previousKycStatus;
    this.newKycStatus = newKycStatus;
  }
}

package com.ebanking.shared.kafka.events;

import com.ebanking.shared.kafka.KafkaTopics;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

/**
 * Event published when a user successfully logs in. Published by: Auth Service Consumed by: Audit
 * Service
 */
@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class AuthLoginEvent extends BaseEvent {

  private String userId;
  private String email;
  private String ipAddress;
  private String userAgent;
  private String loginMethod; // PASSWORD, MFA, BIOMETRIC
  private boolean success;

  public AuthLoginEvent() {
    super(KafkaTopics.AUTH_LOGIN);
  }

  public AuthLoginEvent(
      String userId,
      String email,
      String ipAddress,
      String userAgent,
      String loginMethod,
      boolean success) {
    super(KafkaTopics.AUTH_LOGIN);
    this.userId = userId;
    this.email = email;
    this.ipAddress = ipAddress;
    this.userAgent = userAgent;
    this.loginMethod = loginMethod;
    this.success = success;
  }
}

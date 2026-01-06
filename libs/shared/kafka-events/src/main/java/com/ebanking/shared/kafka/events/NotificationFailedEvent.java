package com.ebanking.shared.kafka.events;

import com.ebanking.shared.kafka.KafkaTopics;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

/** Event published when a notification fails to send */
@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class NotificationFailedEvent extends BaseEvent {

  private String userId;
  private String recipient;
  private String notificationType;
  private String channel;
  private String errorMessage;
  private Integer retryCount;

  public NotificationFailedEvent() {
    super(KafkaTopics.NOTIFICATION_FAILED);
  }

  public NotificationFailedEvent(
      String userId,
      String recipient,
      String notificationType,
      String channel,
      String errorMessage,
      Integer retryCount) {
    super(KafkaTopics.NOTIFICATION_FAILED);
    this.userId = userId;
    this.recipient = recipient;
    this.notificationType = notificationType;
    this.channel = channel;
    this.errorMessage = errorMessage;
    this.retryCount = retryCount;
  }
}

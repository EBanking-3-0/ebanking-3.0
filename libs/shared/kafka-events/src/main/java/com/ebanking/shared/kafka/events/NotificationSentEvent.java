package com.ebanking.shared.kafka.events;

import com.ebanking.shared.kafka.KafkaTopics;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

/**
 * Event published when a notification is sent to a user.
 * Published by: Notification Service
 * Consumed by: Audit Service
 */
@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class NotificationSentEvent extends BaseEvent {
    
    private Long notificationId;
    private Long userId;
    private String recipient;
    private String notificationType; // EMAIL, SMS, PUSH
    private String subject;
    private String status; // SENT, FAILED, PENDING
    private String channel;
    
    public NotificationSentEvent() {
        super(KafkaTopics.NOTIFICATION_SENT);
    }
    
    public NotificationSentEvent(Long notificationId, Long userId, String recipient,
                                String notificationType, String subject, 
                                String status, String channel) {
        super(KafkaTopics.NOTIFICATION_SENT);
        this.notificationId = notificationId;
        this.userId = userId;
        this.recipient = recipient;
        this.notificationType = notificationType;
        this.subject = subject;
        this.status = status;
        this.channel = channel;
    }
}


package com.ebanking.shared.kafka.events;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Base class for all Kafka events in the E-Banking system. Provides common fields and structure for
 * event-driven communication.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "eventType")
@JsonSubTypes({
  @JsonSubTypes.Type(value = UserCreatedEvent.class, name = "user.created"),
  @JsonSubTypes.Type(value = UserUpdatedEvent.class, name = "user.updated"),
  @JsonSubTypes.Type(value = AccountCreatedEvent.class, name = "account.created"),
  @JsonSubTypes.Type(value = BalanceUpdatedEvent.class, name = "balance.updated"),
  @JsonSubTypes.Type(value = TransactionCompletedEvent.class, name = "transaction.completed"),
  @JsonSubTypes.Type(value = PaymentFailedEvent.class, name = "payment.failed"),
  @JsonSubTypes.Type(value = FraudDetectedEvent.class, name = "fraud.detected"),
  @JsonSubTypes.Type(value = AuthLoginEvent.class, name = "auth.login"),
  @JsonSubTypes.Type(value = MfaVerifiedEvent.class, name = "mfa.verified"),
  @JsonSubTypes.Type(value = CryptoTradeExecutedEvent.class, name = "crypto.trade.executed"),
  @JsonSubTypes.Type(value = NotificationSentEvent.class, name = "notification.sent"),
  @JsonSubTypes.Type(value = AlertTriggeredEvent.class, name = "alert.triggered"),
  @JsonSubTypes.Type(
      value = AssistantMessageReceivedEvent.class,
      name = "assistant.message.received"),
  @JsonSubTypes.Type(
      value = AssistantActionExecutedEvent.class,
      name = "assistant.action.executed"),
  @JsonSubTypes.Type(
      value = AssistantConversationStartedEvent.class,
      name = "assistant.conversation.started"),
  @JsonSubTypes.Type(value = AssistantErrorOccurredEvent.class, name = "assistant.error.occurred")
})
public abstract class BaseEvent {

  /** Unique identifier for this event instance */
  @lombok.Builder.Default private String eventId = UUID.randomUUID().toString();

  /** Timestamp when the event was created */
  @lombok.Builder.Default private Instant timestamp = Instant.now();

  /** Type of the event (e.g., "user.created", "transaction.completed") */
  private String eventType;

  /** ID of the user/service that triggered this event */
  private String source;

  /** Correlation ID for tracing related events */
  private String correlationId;

  /** Version of the event schema */
  @lombok.Builder.Default private String version = "1.0";

  protected BaseEvent(String eventType) {
    this.eventType = eventType;
    this.eventId = UUID.randomUUID().toString();
    this.timestamp = Instant.now();
  }
}

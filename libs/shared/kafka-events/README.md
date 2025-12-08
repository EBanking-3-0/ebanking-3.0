# Kafka Events Library

A shared library for Kafka event-driven communication across E-Banking microservices. This library provides event classes, configuration, and utilities to make it easy to publish and consume events.

## Overview

The Kafka Events library provides:
- **Event Classes**: Pre-defined event classes for all major events in the system
- **Configuration**: Pre-configured Kafka producers and consumers
- **Utilities**: Easy-to-use producer and consumer services
- **Type Safety**: Type-safe event publishing and consumption

## Quick Start

### 1. Add Dependency

Add the library to your service's `build.gradle`:

```gradle
dependencies {
    implementation project(':libs:shared:kafka-events')
    // ... other dependencies
}
```

### 2. Configure Kafka

Add Kafka configuration to your `application.yml`:

```yaml
spring:
  kafka:
    bootstrap-servers: ${SPRING_KAFKA_BOOTSTRAP_SERVERS:localhost:9092}
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer
    consumer:
      group-id: ${spring.application.name}
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.springframework.kafka.support.serializer.JsonDeserializer
      auto-offset-reset: earliest
      enable-auto-commit: false
```

### 3. Auto-Configuration

The library uses Spring Boot auto-configuration. As long as you have `spring.kafka.bootstrap-servers` configured, the producers will be automatically available.

**Note**: If you need to customize the configuration, you can exclude auto-configuration and configure manually:

```java
@SpringBootApplication(exclude = {KafkaEventsAutoConfiguration.class})
public class YourServiceApplication {
    // ...
}
```

## Publishing Events

### Using TypedEventProducer (Recommended)

The `TypedEventProducer` provides type-safe methods for each event type:

```java
@Service
@RequiredArgsConstructor
public class UserService {
    
    private final TypedEventProducer eventProducer;
    
    public void createUser(User user) {
        // ... create user logic ...
        
        // Publish event
        UserCreatedEvent event = UserCreatedEvent.builder()
            .userId(user.getId())
            .email(user.getEmail())
            .username(user.getUsername())
            .firstName(user.getFirstName())
            .lastName(user.getLastName())
            .status(user.getStatus().toString())
            .source("user-service")
            .build();
            
        eventProducer.publishUserCreated(event);
    }
}
```

### Using EventProducer (Generic)

For more control or custom topics:

```java
@Service
@RequiredArgsConstructor
public class PaymentService {
    
    private final EventProducer eventProducer;
    
    public void completeTransaction(Transaction transaction) {
        // ... transaction logic ...
        
        TransactionCompletedEvent event = TransactionCompletedEvent.builder()
            .transactionId(transaction.getId())
            .fromAccountId(transaction.getFromAccountId())
            .toAccountId(transaction.getToAccountId())
            .amount(transaction.getAmount())
            .currency(transaction.getCurrency())
            .status("COMPLETED")
            .source("payment-service")
            .build();
            
        eventProducer.publishEvent(KafkaTopics.TRANSACTION_COMPLETED, event);
    }
}
```

## Consuming Events

### Using @KafkaListener (Recommended)

```java
@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceConsumer {
    
    @KafkaListener(topics = KafkaTopics.USER_CREATED)
    public void handleUserCreated(
            @Payload UserCreatedEvent event,
            Acknowledgment acknowledgment) {
        
        try {
            log.info("New user created: {}", event.getEmail());
            
            // Send welcome email
            emailService.sendWelcomeEmail(event.getEmail(), event.getFirstName());
            
            // Acknowledge successful processing
            acknowledgment.acknowledge();
            
        } catch (Exception e) {
            log.error("Failed to process user created event", e);
            // Handle error (retry, dead letter queue, etc.)
        }
    }
    
    @KafkaListener(topics = KafkaTopics.TRANSACTION_COMPLETED)
    public void handleTransactionCompleted(
            @Payload TransactionCompletedEvent event,
            Acknowledgment acknowledgment) {
        
        try {
            log.info("Transaction completed: {}", event.getTransactionId());
            
            // Send notification
            notificationService.sendTransactionNotification(
                event.getToAccountId(), 
                event.getAmount()
            );
            
            acknowledgment.acknowledge();
            
        } catch (Exception e) {
            log.error("Failed to process transaction event", e);
        }
    }
}
```

### Using BaseEventConsumer

For common error handling patterns:

```java
@Component
@Slf4j
public class AuditServiceConsumer extends BaseEventConsumer {
    
    @KafkaListener(topics = {
        KafkaTopics.USER_CREATED,
        KafkaTopics.TRANSACTION_COMPLETED,
        KafkaTopics.ACCOUNT_CREATED
    })
    public void handleAuditEvent(
            @Payload BaseEvent event,
            Acknowledgment acknowledgment,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {
        
        processEvent(event, acknowledgment, partition, offset, () -> {
            // Your audit logic here
            auditRepository.save(AuditLog.builder()
                .eventId(event.getEventId())
                .eventType(event.getEventType())
                .timestamp(event.getTimestamp())
                .source(event.getSource())
                .build());
        });
    }
}
```

## Available Events

### User Events
- **UserCreatedEvent** - Published when a new user is registered
- **UserUpdatedEvent** - Published when a user profile is updated

### Account Events
- **AccountCreatedEvent** - Published when a new account is created
- **BalanceUpdatedEvent** - Published when an account balance changes

### Transaction Events
- **TransactionCompletedEvent** - Published when a transaction succeeds
- **PaymentFailedEvent** - Published when a payment fails

### Security Events
- **FraudDetectedEvent** - Published when fraud is detected
- **AuthLoginEvent** - Published when a user logs in
- **MfaVerifiedEvent** - Published when MFA verification completes

### Crypto Events
- **CryptoTradeExecutedEvent** - Published when a crypto trade executes

### Notification Events
- **NotificationSentEvent** - Published when a notification is sent

### Analytics Events
- **AlertTriggeredEvent** - Published when an alert is triggered

## Topic Constants

All topic names are available as constants in `KafkaTopics`:

```java
KafkaTopics.USER_CREATED
KafkaTopics.TRANSACTION_COMPLETED
KafkaTopics.ACCOUNT_CREATED
// ... etc
```

## Event Structure

All events extend `BaseEvent` which provides:

- `eventId` - Unique identifier for the event
- `timestamp` - When the event was created
- `eventType` - Type of event (automatically set)
- `source` - Service that published the event
- `correlationId` - For tracing related events
- `version` - Event schema version

## Configuration

### Local Development

For local development with Docker Compose:

```yaml
spring:
  kafka:
    bootstrap-servers: localhost:9092
```

### Docker Environment

For Docker Compose environment:

```yaml
spring:
  kafka:
    bootstrap-servers: kafka:29092
```

### Consumer Group Naming

Consumer groups are automatically set to `${spring.application.name}`. You can override:

```yaml
spring:
  kafka:
    consumer:
      group-id: my-service-group
```

## Best Practices

### 1. Always Set Source

Always set the `source` field when creating events:

```java
UserCreatedEvent event = UserCreatedEvent.builder()
    .userId(user.getId())
    .email(user.getEmail())
    .source("user-service")  // Important!
    .build();
```

### 2. Use Correlation IDs

For tracing related events:

```java
String correlationId = UUID.randomUUID().toString();

TransactionCompletedEvent event = TransactionCompletedEvent.builder()
    .transactionId(transaction.getId())
    .correlationId(correlationId)  // Use same ID for related events
    .source("payment-service")
    .build();
```

### 3. Handle Errors Gracefully

Always handle errors in consumers:

```java
@KafkaListener(topics = KafkaTopics.USER_CREATED)
public void handleUserCreated(@Payload UserCreatedEvent event, Acknowledgment ack) {
    try {
        // Process event
        processUserCreated(event);
        ack.acknowledge();
    } catch (Exception e) {
        log.error("Failed to process event", e);
        // Decide: retry, dead letter queue, or acknowledge
        // For now, acknowledge to prevent blocking
        ack.acknowledge();
    }
}
```

### 4. Use Type-Safe Producers

Prefer `TypedEventProducer` over generic `EventProducer`:

```java
// Good
typedEventProducer.publishUserCreated(event);

// Less type-safe
eventProducer.publishEvent(KafkaTopics.USER_CREATED, event);
```

### 5. Idempotent Consumers

Make your consumers idempotent:

```java
@KafkaListener(topics = KafkaTopics.TRANSACTION_COMPLETED)
public void handleTransaction(@Payload TransactionCompletedEvent event, Acknowledgment ack) {
    // Check if already processed
    if (notificationRepository.existsByEventId(event.getEventId())) {
        log.warn("Event already processed: {}", event.getEventId());
        ack.acknowledge();
        return;
    }
    
    // Process event
    sendNotification(event);
    notificationRepository.save(Notification.builder()
        .eventId(event.getEventId())
        .build());
    
    ack.acknowledge();
}
```

## Troubleshooting

### Events Not Being Published

1. Check Kafka is running:
   ```bash
   docker-compose ps kafka
   ```

2. Check bootstrap servers configuration:
   ```yaml
   spring:
     kafka:
       bootstrap-servers: localhost:9092  # or kafka:29092 for Docker
   ```

3. Check logs for errors:
   ```bash
   docker-compose logs kafka
   ```

### Events Not Being Consumed

1. Check consumer group:
   ```yaml
   spring:
     kafka:
       consumer:
         group-id: your-service-name
   ```

2. Check topic exists:
   ```bash
   docker exec -it ebanking-kafka kafka-topics --list --bootstrap-server localhost:9092
   ```

3. Check consumer is subscribed:
   - Verify `@KafkaListener` annotation is present
   - Verify component is scanned by Spring
   - Check application logs for consumer startup

### Serialization Errors

If you see serialization errors:

1. Ensure Jackson is on classpath (already included)
2. Check event classes have proper annotations
3. Verify event extends `BaseEvent`

## Examples

### Complete Producer Example

```java
@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    
    private final UserRepository userRepository;
    private final TypedEventProducer eventProducer;
    
    public User createUser(CreateUserRequest request) {
        User user = User.builder()
            .email(request.getEmail())
            .firstName(request.getFirstName())
            .lastName(request.getLastName())
            .build();
            
        user = userRepository.save(user);
        
        // Publish event
        UserCreatedEvent event = UserCreatedEvent.builder()
            .userId(user.getId())
            .email(user.getEmail())
            .username(user.getUsername())
            .firstName(user.getFirstName())
            .lastName(user.getLastName())
            .status(user.getStatus().toString())
            .source("user-service")
            .build();
            
        eventProducer.publishUserCreated(event);
        log.info("Published user created event: {}", event.getEventId());
        
        return user;
    }
}
```

### Complete Consumer Example

```java
@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationConsumer {
    
    private final EmailService emailService;
    private final NotificationRepository notificationRepository;
    
    @KafkaListener(topics = KafkaTopics.USER_CREATED)
    public void handleUserCreated(
            @Payload UserCreatedEvent event,
            Acknowledgment acknowledgment) {
        
        try {
            // Check if already processed (idempotency)
            if (notificationRepository.existsByEventId(event.getEventId())) {
                log.warn("Event already processed: {}", event.getEventId());
                acknowledgment.acknowledge();
                return;
            }
            
            // Send welcome email
            emailService.sendWelcomeEmail(
                event.getEmail(),
                event.getFirstName()
            );
            
            // Save notification record
            notificationRepository.save(Notification.builder()
                .eventId(event.getEventId())
                .userId(event.getUserId())
                .type("WELCOME_EMAIL")
                .status("SENT")
                .build());
            
            log.info("Processed user created event: {}", event.getEventId());
            acknowledgment.acknowledge();
            
        } catch (Exception e) {
            log.error("Failed to process user created event: {}", 
                event.getEventId(), e);
            // In production, implement retry or dead letter queue
            acknowledgment.acknowledge();
        }
    }
}
```

## Architecture

```
┌─────────────────┐
│  Your Service   │
└────────┬────────┘
         │
         ├─── TypedEventProducer ───┐
         │                          │
         └─── EventProducer ────────┼─── Kafka Topics
                                    │
┌─────────────────┐                 │
│  Your Service   │                 │
└────────┬────────┘                 │
         │                          │
         └─── @KafkaListener ───────┘
```

## Support

For issues or questions:
- Check this README
- Review service-specific README files
- Check Kafka logs: `docker compose logs kafka`

## Version

This library is part of E-Banking 3.0.0


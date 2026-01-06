# Kafka Events Integration Plan

## Overview

Integrate Kafka event-driven communication into all microservices in the E-Banking 3.0 system. This plan covers adding event producers (publishers) and consumers (subscribers) to each service based on their responsibilities and the event flow defined in the architecture.

## Current State Analysis

### Services with Kafka-Events Dependency Already

- ✅ account-service
- ✅ payment-service
- ✅ notification-service
- ✅ audit-service
- ✅ auth-service
- ✅ analytics-service
- ✅ crypto-service

### Services Needing Dependency

- ❌ user-service (needs kafka-events dependency)

### Services Status

- All services have `spring-kafka` dependency
- Kafka infrastructure is configured in docker-compose.yml
- No actual event publishing/consuming implementation yet

## Integration Strategy

### Phase 1: Foundation Setup

1. Add kafka-events dependency to user-service
2. Add Kafka configuration to all service application.yml files
3. Verify auto-configuration works

### Phase 2: Event Producers (Publishers)

Integrate event publishing into services that produce events:

- User Service: publish user.created, user.updated
- Account Service: publish account.created, balance.updated
- Payment Service: publish transaction.completed, payment.failed, fraud.detected
- Auth Service: publish auth.login, mfa.verified
- Crypto Service: publish crypto.trade.executed
- Notification Service: publish notification.sent
- Analytics Service: publish alert.triggered

### Phase 3: Event Consumers (Subscribers)

Integrate event consumption into services that react to events:

- Notification Service: consume user.created, transaction.completed, fraud.detected, crypto.trade.executed, alert.triggered
- Audit Service: consume user.created, account.created, transaction.completed, payment.failed, fraud.detected, auth.login, mfa.verified, crypto.trade.executed, notification.sent
- Analytics Service: consume user.created, account.created, transaction.completed, crypto.trade.executed
- Account Service: consume transaction.completed
- Crypto Service: consume account.created

## Detailed Implementation Plan

### 1. User Service Integration

**Location**: `apps/services/user-service/`

**Tasks**:

1. [x] Add kafka-events dependency to build.gradle
2. [x] Add Kafka configuration to application.yml
3. [x] Inject TypedEventProducer into UserService
4. [x] Publish UserCreatedEvent in createUser() method
5. [x] Publish UserUpdatedEvent in updateUser() method
6. [x] Add source field to events (set to "user-service")

**Files to Modify**:

- `build.gradle` - Add dependency
- `src/main/resources/application.yml` - Add Kafka config
- `src/main/java/com/ebanking/user/service/UserService.java` - Add event publishing

**Events to Publish**:

- `user.created` - When new user is created
- `user.updated` - When user profile is updated

### 2. Account Service Integration

**Location**: `apps/services/account-service/`

**Tasks**:

1. Verify kafka-events dependency exists
2. Add Kafka configuration to application.yml
3. Inject TypedEventProducer into AccountService
4. Publish AccountCreatedEvent when account is created
5. Publish BalanceUpdatedEvent when balance changes
6. Create consumer for TransactionCompletedEvent to update balances
7. Add source field to events

**Files to Modify**:

- `src/main/resources/application.yml` - Add Kafka config
- Service classes - Add event publishing and consumption

**Events to Publish**:

- `account.created` - When new account is created
- `balance.updated` - When account balance changes

**Events to Consume**:

- `transaction.completed` - To update account balances

### 3. Payment Service Integration

**Location**: `apps/services/payment-service/`

**Tasks**:

1. Verify kafka-events dependency exists
2. Add Kafka configuration to application.yml
3. Inject TypedEventProducer into PaymentService
4. Publish TransactionCompletedEvent when payment succeeds
5. Publish PaymentFailedEvent when payment fails
6. Publish FraudDetectedEvent when fraud is detected
7. Add source field to events

**Files to Modify**:

- `src/main/resources/application.yml` - Add Kafka config
- Service classes - Add event publishing

**Events to Publish**:

- `transaction.completed` - When transaction succeeds
- `payment.failed` - When payment fails
- `fraud.detected` - When fraud is detected

### 4. Auth Service Integration

**Location**: `apps/services/auth-service/`

**Tasks**:

1. Verify kafka-events dependency exists
2. Add Kafka configuration to application.yml
3. Inject TypedEventProducer into AuthService
4. Publish AuthLoginEvent when user logs in
5. Publish MfaVerifiedEvent when MFA is verified
6. Add source field to events

**Files to Modify**:

- `src/main/resources/application.yml` - Add Kafka config
- Service classes - Add event publishing

**Events to Publish**:

- `auth.login` - When user logs in
- `mfa.verified` - When MFA verification completes

### 5. Crypto Service Integration

**Location**: `apps/services/crypto-service/`

**Tasks**:

1. Verify kafka-events dependency exists
2. Add Kafka configuration to application.yml
3. Inject TypedEventProducer into CryptoService
4. Publish CryptoTradeExecutedEvent when trade completes
5. Create consumer for AccountCreatedEvent to initialize crypto wallet
6. Add source field to events

**Files to Modify**:

- `src/main/resources/application.yml` - Add Kafka config
- Service classes - Add event publishing and consumption

**Events to Publish**:

- `crypto.trade.executed` - When crypto trade completes

**Events to Consume**:

- `account.created` - To initialize crypto wallet for new account

### 6. Notification Service Integration

**Location**: `apps/services/notification-service/`

**Tasks**:

1. Verify kafka-events dependency exists
2. Add Kafka configuration to application.yml
3. Create NotificationConsumer class
4. Consume UserCreatedEvent - send welcome email
5. Consume TransactionCompletedEvent - send transaction notification
6. Consume PaymentFailedEvent - send failure notification
7. Consume FraudDetectedEvent - send fraud alert
8. Consume CryptoTradeExecutedEvent - send trade confirmation
9. Consume AlertTriggeredEvent - send alert notification
10. Publish NotificationSentEvent after sending notification
11. Inject TypedEventProducer for publishing

**Files to Modify**:

- `src/main/resources/application.yml` - Add Kafka config
- Create `src/main/java/com/ebanking/notification/consumer/NotificationConsumer.java`
- Service classes - Add event publishing

**Events to Consume**:

- `user.created` - Send welcome email
- `transaction.completed` - Send transaction notification
- `payment.failed` - Send failure notification
- `fraud.detected` - Send fraud alert
- `crypto.trade.executed` - Send trade confirmation
- `alert.triggered` - Send alert notification

**Events to Publish**:

- `notification.sent` - After notification is sent

### 7. Audit Service Integration

**Location**: `apps/services/audit-service/`

**Tasks**:

1. Verify kafka-events dependency exists
2. Add Kafka configuration to application.yml
3. Create AuditConsumer class extending BaseEventConsumer
4. Consume all events for audit logging:
   - user.created, user.updated
   - account.created, balance.updated
   - transaction.completed, payment.failed
   - fraud.detected
   - auth.login, mfa.verified
   - crypto.trade.executed
   - notification.sent
5. Save audit logs to MongoDB

**Files to Modify**:

- `src/main/resources/application.yml` - Add Kafka config
- Create `src/main/java/com/ebanking/audit/consumer/AuditConsumer.java`
- Create audit log entity/repository if needed

**Events to Consume**:

- All events for comprehensive audit trail

### 8. Analytics Service Integration

**Location**: `apps/services/analytics-service/`

**Tasks**:

1. Verify kafka-events dependency exists
2. Add Kafka configuration to application.yml
3. Create AnalyticsConsumer class
4. Consume events for analytics:
   - user.created, account.created
   - transaction.completed, crypto.trade.executed
5. Aggregate data for dashboards
6. Publish AlertTriggeredEvent when thresholds are exceeded
7. Inject TypedEventProducer for publishing

**Files to Modify**:

- `src/main/resources/application.yml` - Add Kafka config
- Create `src/main/java/com/ebanking/analytics/consumer/AnalyticsConsumer.java`
- Service classes - Add event publishing

**Events to Consume**:

- `user.created` - Track user growth
- `account.created` - Track account creation
- `transaction.completed` - Aggregate transaction data
- `crypto.trade.executed` - Track crypto activity

**Events to Publish**:

- `alert.triggered` - When budget/threshold alerts are triggered

## Configuration Template

### application.yml Kafka Configuration

Add to each service's `application.yml`:

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

For Docker environment, use:

```yaml
spring:
  kafka:
    bootstrap-servers: ${SPRING_KAFKA_BOOTSTRAP_SERVERS:kafka:29092}
```

## Implementation Patterns

### Event Producer Pattern

```java
@Service
@RequiredArgsConstructor
public class YourService {
    private final TypedEventProducer eventProducer;

    public void yourBusinessMethod() {
        // ... business logic ...

        // Publish event
        YourEvent event = YourEvent.builder()
            .field1(value1)
            .field2(value2)
            .source("your-service")
            .build();

        eventProducer.publishYourEvent(event);
    }
}
```

### Event Consumer Pattern

```java
@Component
@RequiredArgsConstructor
@Slf4j
public class YourConsumer {

    @KafkaListener(topics = KafkaTopics.YOUR_TOPIC)
    public void handleEvent(
            @Payload YourEvent event,
            Acknowledgment acknowledgment) {
        try {
            // Process event
            processEvent(event);
            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error("Failed to process event", e);
            // Handle error
            acknowledgment.acknowledge(); // or implement retry
        }
    }
}
```

## Testing Strategy

1. **Unit Tests**: Mock KafkaTemplate for producer tests
2. **Integration Tests**: Use EmbeddedKafka for consumer tests
3. **Manual Testing**:
   - Start Kafka via docker-compose
   - Trigger events through API calls
   - Verify events are published and consumed
   - Check Kafka topics using kafka-console-consumer

## Verification Checklist

For each service:

- [ ] Kafka dependency added (if needed)
- [ ] Kafka configuration added to application.yml
- [ ] Event producers integrated in service classes
- [ ] Event consumers created (if needed)
- [ ] Source field set on all published events
- [ ] Error handling implemented in consumers
- [ ] Logging added for event publishing/consumption
- [ ] Service compiles and runs successfully

## Order of Implementation

Recommended order to minimize dependencies:

1. **User Service** (foundation - other services depend on user events)
2. **Account Service** (depends on user, publishes account events)
3. **Auth Service** (publishes auth events)
4. **Payment Service** (depends on account, publishes transaction events)
5. **Crypto Service** (depends on account, publishes crypto events)
6. **Notification Service** (consumes multiple events)
7. **Analytics Service** (consumes multiple events)
8. **Audit Service** (consumes all events)

## Success Criteria

- All services can publish their designated events
- All services can consume their required events
- Events flow correctly through the system
- No compilation errors
- Services start successfully with Kafka integration
- Events are properly serialized/deserialized
- Error handling prevents service failures

## Notes

- Use `TypedEventProducer` for type-safe event publishing
- Always set `source` field to identify the publishing service
- Use correlation IDs for tracing related events
- Implement idempotency in consumers where needed
- Add proper logging for debugging
- Handle errors gracefully to prevent blocking

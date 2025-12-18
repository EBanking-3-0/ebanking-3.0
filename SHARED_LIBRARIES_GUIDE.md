# Shared Libraries Usage Guide

This guide explains how to use the shared libraries in the E-Banking microservices architecture. All shared libraries are located in `libs/shared/` and provide common functionality across all services.

## Table of Contents

1. [kafka-events](#kafka-events) - Event-driven architecture
2. [dto](#dto) - Data Transfer Objects
3. [exceptions](#exceptions) - Custom exceptions
4. [common](#common) - Common utilities
5. [security](#security) - Security utilities
6. [Integration Steps](#integration-steps)
7. [Best Practices](#best-practices)

---

## kafka-events

**Purpose**: Provides a standardized event producer and consumer framework for Kafka-based event-driven communication.

### What It Provides

- `EventProducer` - Publish typed events to Kafka
- `BaseEvent` - Base class for all events
- `KafkaTopics` - Centralized topic name constants
- Auto-configured Kafka producer/consumer beans
- Type-safe event publishing with JSON serialization

### How to Use

#### Step 1: Add Dependency

In your service's `build.gradle`:

```gradle
dependencies {
    implementation project(':libs:shared:kafka-events')
}
```

#### Step 2: Configure Kafka

Ensure your `application.yml` has Kafka configuration:

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
      properties:
        spring.json.trusted.packages: "*"
```

#### Step 3: Create Your Event Class

Create an event by extending `BaseEvent`:

```java
package com.ebanking.payment.events;

import com.ebanking.shared.kafka.events.BaseEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@lombok.EqualsAndHashCode(callSuper=true)
public class PaymentProcessedEvent extends BaseEvent {
    private String paymentId;
    private String userId;
    private Double amount;
    private String status;
}
```

**Key Points**:

- Must extend `BaseEvent`
- Use `@SuperBuilder` (not `@Builder`)
- Add `@EqualsAndHashCode(callSuper=true)` to properly handle inherited fields
- BaseEvent automatically provides: `eventId`, `timestamp`, `source`, `correlationId`, `version`

#### Step 4: Create Topic Constants

Add your event topics to `KafkaTopics.java` in the kafka-events library:

```java
public class KafkaTopics {
    // Existing topics...

    // Payment Service
    public static final String PAYMENT_PROCESSED = "payment.processed";
    public static final String PAYMENT_FAILED = "payment.failed";
}
```

#### Step 5: Publish Events

Inject `EventProducer` and publish:

```java
import com.ebanking.shared.kafka.producer.EventProducer;
import com.ebanking.shared.kafka.KafkaTopics;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final EventProducer eventProducer;

    public void processPayment(String paymentId, String userId, Double amount) {
        // Process payment...

        PaymentProcessedEvent event = PaymentProcessedEvent.builder()
            .paymentId(paymentId)
            .userId(userId)
            .amount(amount)
            .status("COMPLETED")
            .source("payment-service")
            .correlationId(userId) // Link related events
            .build();

        eventProducer.publishEvent(KafkaTopics.PAYMENT_PROCESSED, event);
    }
}
```

#### Step 6: Consume Events (Optional)

Create a consumer by extending `BaseEventConsumer`:

```java
import com.ebanking.shared.kafka.consumer.BaseEventConsumer;

@Service
@Slf4j
public class PaymentEventConsumer extends BaseEventConsumer<PaymentProcessedEvent> {

    @Override
    @KafkaListener(topics = KafkaTopics.PAYMENT_PROCESSED, groupId = "notification-service")
    public void consume(PaymentProcessedEvent event) {
        log.info("Received payment event: {}", event.getPaymentId());
        // Handle event...
    }
}
```

---

## dto

**Purpose**: Centralized Data Transfer Objects for inter-service communication and API contracts.

### What It Provides

- Common DTO classes for API requests/responses
- Validation annotations
- Standardized data structures

### How to Use

#### Step 1: Add Dependency

```gradle
dependencies {
    implementation project(':libs:shared:dto')
}
```

#### Step 2: Use Existing DTOs

Browse available DTOs in `libs/shared/dto/src/main/java/com/ebanking/shared/dto/` and use in your service:

```java
import com.ebanking.shared.dto.ChatRequestDTO;
import com.ebanking.shared.dto.ChatResponseDTO;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    @PostMapping
    public ResponseEntity<ChatResponseDTO> sendMessage(
            @Valid @RequestBody ChatRequestDTO request) {
        // Process request...
        return ResponseEntity.ok(response);
    }
}
```

#### Step 3: Create New Shared DTOs

If you need a new DTO used by multiple services, add it to the shared library:

```java
// libs/shared/dto/src/main/java/com/ebanking/shared/dto/PaymentDTO.java
package com.ebanking.shared.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentDTO {
    @NotBlank
    private String paymentId;

    @NotNull
    @Positive
    private Double amount;

    @NotBlank
    private String currency;

    @NotBlank
    private String status;
}
```

Then rebuild the library and use it across services:

```gradle
// Rebuild in any service
nx build shared-dto
```

---

## exceptions

**Purpose**: Centralized custom exception definitions for consistent error handling across services.

### What It Provides

- `BusinessException` - Business logic errors
- `ValidationException` - Validation errors
- `ResourceNotFoundException` - Resource not found errors
- Global exception handlers
- Standardized error responses

### How to Use

#### Step 1: Add Dependency

```gradle
dependencies {
    implementation project(':libs:shared:exceptions')
}
```

#### Step 2: Throw Custom Exceptions

```java
import com.ebanking.shared.exceptions.ResourceNotFoundException;
import com.ebanking.shared.exceptions.BusinessException;

@Service
public class PaymentService {

    public Payment getPayment(String paymentId) {
        return paymentRepository.findById(paymentId)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Payment not found: " + paymentId
            ));
    }

    public void cancelPayment(String paymentId) {
        Payment payment = getPayment(paymentId);

        if (!payment.canBeCancelled()) {
            throw new BusinessException(
                "Payment cannot be cancelled in status: " + payment.getStatus()
            );
        }

        // Cancel logic...
    }
}
```

#### Step 3: Exception Handling

The shared exceptions library provides automatic exception mapping. When exceptions are thrown, they're automatically converted to proper HTTP responses.

---

## common

**Purpose**: Common utilities, configurations, and base classes used across all services.

### What It Provides

- Utility functions
- Common configurations
- Base service classes
- Helper methods

### How to Use

#### Step 1: Add Dependency

```gradle
dependencies {
    implementation project(':libs:shared:common')
}
```

#### Step 2: Use Common Utilities

```java
import com.ebanking.shared.common.utils.StringUtils;
import com.ebanking.shared.common.config.CommonConfig;

@Service
public class MyService {

    public void processData(String input) {
        // Use common utilities
        if (StringUtils.isNullOrEmpty(input)) {
            throw new ValidationException("Input cannot be empty");
        }

        String processed = StringUtils.sanitize(input);
        // Continue processing...
    }
}
```

---

## security

**Purpose**: Security utilities and configurations for OAuth2/JWT authentication.

### What It Provides

- `JwtAuthConverter` - JWT token conversion to Spring Security authentication
- `SecurityConfig` - Security configuration
- Keycloak integration helpers
- User context extraction

### How to Use

#### Step 1: Add Dependency

```gradle
dependencies {
    implementation project(':libs:shared:security')
}
```

#### Step 2: Configure Security

The security library is auto-configured. Just ensure your `application.yml` has OAuth2 settings:

```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: ${SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUER_URI:http://localhost:8092/realms/ebanking-realm}
          jwk-set-uri: ${SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_JWK_SET_URI:http://localhost:8092/realms/ebanking-realm/protocol/openid-connect/certs}
```

#### Step 3: Extract User Information

The library provides `SecurityUtil` to get current user:

```java
import com.ebanking.shared.security.SecurityUtil;

@Service
public class UserService {

    public UserProfile getCurrentUserProfile() {
        String userId = SecurityUtil.getCurrentUserId(); // From JWT "sub" claim
        String username = SecurityUtil.getCurrentUsername();

        return userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException(
                "User not found: " + userId
            ));
    }
}
```

#### Step 4: Require Authentication on Endpoints

Endpoints are protected by default via Spring Security. Use annotations to fine-tune:

```java
@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<PaymentDTO> createPayment(@RequestBody PaymentDTO payment) {
        // Only users with USER role can access
        return ResponseEntity.ok(paymentService.create(payment));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<PaymentDTO> getPayment(@PathVariable String id) {
        return ResponseEntity.ok(paymentService.getPayment(id));
    }
}
```

---

## Integration Steps

### Complete Integration Checklist

Follow these steps to integrate all shared libraries into a new or existing service:

#### 1. Add Dependencies to `build.gradle`

```gradle
dependencies {
    // Shared libraries
    implementation project(':libs:shared:common')
    implementation project(':libs:shared:dto')
    implementation project(':libs:shared:exceptions')
    implementation project(':libs:shared:kafka-events')
    implementation project(':libs:shared:security')

    // Other dependencies...
}
```

#### 2. Configure `application.yml`

```yaml
server:
  port: 8085 # Unique port for your service

spring:
  application:
    name: your-service # Must match Eureka registration name

  # Database
  data:
    mongodb:
      uri: mongodb://ebanking:ebanking123@localhost:27017/ebanking?authSource=admin

  # Kafka
  kafka:
    bootstrap-servers: ${SPRING_KAFKA_BOOTSTRAP_SERVERS:localhost:9092}
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer
    consumer:
      group-id: ${spring.application.name}
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.springframework.kafka.support.serializer.JsonDeserializer
      properties:
        spring.json.trusted.packages: "*"

  # Security
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: ${SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUER_URI:http://localhost:8092/realms/ebanking-realm}
          jwk-set-uri: ${SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_JWK_SET_URI:http://localhost:8092/realms/ebanking-realm/protocol/openid-connect/certs}

# Service Discovery
eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
    register-with-eureka: true
    fetch-registry: true
  instance:
    prefer-ip-address: true
```

#### 3. Create Main Application Class

```java
package com.ebanking.yourservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class YourServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(YourServiceApplication.class, args);
    }
}
```

#### 4. Create Your Event Classes

See [kafka-events](#kafka-events) section above for details.

#### 5. Create Your Controllers

Use shared DTOs and exceptions:

```java
@RestController
@RequestMapping("/api/resources")
@RequiredArgsConstructor
@Slf4j
public class ResourceController {

    private final ResourceService resourceService;
    private final EventProducer eventProducer;

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ResourceDTO> create(@Valid @RequestBody CreateResourceDTO request) {
        try {
            ResourceDTO resource = resourceService.create(request);

            // Publish event
            ResourceCreatedEvent event = ResourceCreatedEvent.builder()
                .resourceId(resource.getId())
                .userId(SecurityUtil.getCurrentUserId())
                .source("your-service")
                .build();
            eventProducer.publishEvent(KafkaTopics.RESOURCE_CREATED, event);

            return ResponseEntity.status(HttpStatus.CREATED).body(resource);
        } catch (BusinessException e) {
            log.error("Business error creating resource", e);
            throw e;
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ResourceDTO> get(@PathVariable String id) {
        return ResponseEntity.ok(resourceService.getResource(id));
    }
}
```

---

## Best Practices

### 1. Event Design

✅ **DO**:

- Create a new event class for each significant business event
- Include correlation IDs for tracing related events
- Use immutable field types
- Document event semantics

❌ **DON'T**:

- Reuse generic event classes
- Include sensitive data in events
- Use mutable collections

### 2. Exception Handling

✅ **DO**:

- Throw specific exceptions (`BusinessException`, `ResourceNotFoundException`)
- Include meaningful error messages
- Catch and log exceptions at service boundaries
- Let Spring's exception handlers convert to HTTP responses

❌ **DON'T**:

- Catch generic `Exception`
- Return errors in successful HTTP responses
- Log sensitive data in error messages

### 3. Security

✅ **DO**:

- Use `@PreAuthorize` for role-based access control
- Extract user context using `SecurityUtil`
- Validate JWT tokens (automatic)
- Use correlation IDs for audit trails

❌ **DON'T**:

- Bypass security checks
- Store sensitive data unencrypted
- Log authentication tokens

### 4. Kafka Events

✅ **DO**:

- Use events for inter-service communication
- Include timestamps and versions in events
- Consume events asynchronously
- Implement idempotent consumers

❌ **DON'T**:

- Use events for synchronous operations (use Feign/REST instead)
- Include large binary data in events
- Create events without business meaning

### 5. DTOs

✅ **DO**:

- Use shared DTOs for cross-service communication
- Add validation annotations
- Version your DTOs if schema changes occur
- Use specific, immutable types

❌ **DON'T**:

- Use domain entities as DTOs
- Accept unvalidated input
- Return all fields (use projections)

---

## Troubleshooting

### Issue: Kafka Auto-Configuration Not Working

**Symptom**: `EventProducer` bean not found

**Solution**:

1. Verify `spring.kafka.bootstrap-servers` is configured in `application.yml`
2. Rebuild kafka-events library: `nx build kafka-events`
3. Check that `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` exists

### Issue: JWT Authentication Failing

**Symptom**: 401 Unauthorized on protected endpoints

**Solution**:

1. Verify Keycloak is running on port 8092
2. Check `SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUER_URI` environment variable
3. Ensure JWT token is included in `Authorization: Bearer <token>` header

### Issue: Events Not Being Published

**Symptom**: No messages in Kafka topics

**Solution**:

1. Verify Kafka is running on port 9092
2. Check service logs for exceptions
3. Verify topic names in `KafkaTopics` class
4. Ensure `@EnableKafka` is present (auto-configured)

### Issue: DTO Validation Not Working

**Symptom**: Invalid data accepted by API

**Solution**:

1. Add `@Valid` annotation to controller parameters
2. Add validation annotations (`@NotNull`, `@NotBlank`, etc.) to DTO fields
3. Ensure `spring-boot-starter-validation` dependency is present

---

## Quick Reference

| Library        | Purpose                   | Key Classes                                      | Setup                    |
| -------------- | ------------------------- | ------------------------------------------------ | ------------------------ |
| `kafka-events` | Event-driven architecture | `EventProducer`, `BaseEvent`                     | Configure `spring.kafka` |
| `dto`          | Data transfer             | `*DTO` classes                                   | Import and use           |
| `exceptions`   | Error handling            | `BusinessException`, `ResourceNotFoundException` | Throw in services        |
| `common`       | Utilities                 | `StringUtils`, `DateUtils`                       | Use static methods       |
| `security`     | Auth/JWT                  | `SecurityUtil`, `JwtAuthConverter`               | Configure OAuth2         |

---

## Additional Resources

- [Kafka Documentation](https://kafka.apache.org/documentation/)
- [Spring Security OAuth2](https://spring.io/projects/spring-security-oauth)
- [Spring Data MongoDB](https://spring.io/projects/spring-data-mongodb)
- [Keycloak Documentation](https://www.keycloak.org/documentation)

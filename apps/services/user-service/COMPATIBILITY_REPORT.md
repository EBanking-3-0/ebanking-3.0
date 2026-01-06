# User Service - Compatibility Report

**Date**: January 6, 2026  
**Service Version**: Current  
**Status**: ⚠️ **COMPATIBILITY ISSUES FOUND - ACTION REQUIRED**

---

## Executive Summary

The user-service has **critical compatibility gaps** with the notification-service that require immediate attention. The other services (payment-service) have minimal interaction with user-service.

**Issues Found**:

- ❌ Notification-service expects endpoints that don't exist in user-service
- ❌ Missing user contact endpoint required by notification service
- ✅ Payment-service integration is compatible (event-driven)
- ⚠️ User-service event publishing has inconsistencies with shared event standards

---

## 1. Notification-Service Integration

### 1.1 Critical Gap: Missing User Contact Endpoint ❌

**Issue**: Notification-service requires user contact information but user-service doesn't provide it.

**Expected Endpoint** (by notification-service):

```
GET /api/users/{userId}/contact
```

**Response Expected**:

```java
{
  "userId": 123,
  "email": "user@example.com",
  "phoneNumber": "+1234567890",
  "firstName": "John",
  "lastName": "Doe",
  "preferredLanguage": "en"
}
```

**Actual User-Service Endpoints**:
| Endpoint | Method | Purpose |
|----------|--------|---------|
| `/api/v1/users/me` | GET | Get current user profile (requires auth) |
| `/api/v1/users/{userId}` | GET | Get user details |
| `/api/v1/kyc/status` | GET | Get KYC verification status |
| `/api/v1/kyc` | POST | Submit KYC |

**Problem**:

- User-service provides `/api/v1/users/{userId}` but notification-service expects `/api/users/{userId}/contact`
- API path is different (`/api/v1/` vs `/api/`)
- Response structure likely doesn't match UserContactDTO

**Impact**:

- Notification-service cannot fetch user contact details
- Cannot send emails/SMS to users
- Service will fail at runtime when notification is triggered

### 1.2 Missing Additional Endpoints ❌

**Notification-service expects**:

```
GET /api/users/{userId}/exists
GET /api/users/{userId}/verify
```

**Status**: NOT FOUND in user-service

---

## 2. Payment-Service Integration

### 2.1 Kafka Event Integration ✅

**Status**: COMPATIBLE

Payment-service consumes user events indirectly:

- Publishes `payment.failed` events
- Notification-service listens and triggers notifications
- Integration is event-driven (loose coupling) ✅

**No direct REST calls to user-service** ✅

---

## 3. Account-Service Integration

### 3.1 User Association ✅

**Status**: COMPATIBLE (indirect)

- Account-service uses `userId` internally
- User-service manages user creation
- Integration via Kafka events is proper

---

## 4. Data Model Compatibility

### 4.1 User Events ⚠️

**Current Implementation Issue**:

UserEventProducer publishes event with inconsistencies:

```java
UserCreatedEvent event = UserCreatedEvent.builder()
    .userId(0L)  // ❌ WRONG: Should be user.getId()
    .email(user.getEmail())
    .firstName(user.getFirstName())
    .lastName(user.getLastName())
    .status("ACTIVE")  // ❌ WRONG: Should use UserStatus enum
    .build();
```

**Issues**:

1. `userId` is hardcoded to `0L` instead of actual user ID
2. Status should use enum, not string
3. Missing username field

**Impact**: Notification-service receives invalid user IDs and cannot match users

---

## 5. API Contract Gaps

### 5.1 Version Mismatch

User-service uses API v1 prefix:

```
/api/v1/users
/api/v1/kyc
```

Notification-service expects (likely v0 or no version):

```
/api/users
```

### 5.2 Response DTO Mismatch

User-service provides: `UserProfileResponse` (from shared DTOs)

Notification-service expects: `UserContactDTO` (internal to notification-service)

**Fields needed by notification-service**:

- userId ✓
- email ✓
- phoneNumber ✗ (not in UserProfileResponse)
- firstName ✓
- lastName ✓
- preferredLanguage ✓ (might be missing)

---

## 6. Required Fixes

### Fix 1: Add User Contact Endpoint to User-Service

**Required Endpoint**:

```java
@GetMapping("/api/users/{userId}/contact")
public ResponseEntity<UserContactDTO> getUserContact(@PathVariable Long userId) {
    User user = userService.getUserById(userId);
    if (user == null) {
        return ResponseEntity.notFound().build();
    }

    UserContactDTO contact = UserContactDTO.builder()
        .userId(user.getId())
        .email(user.getEmail())
        .phoneNumber(user.getPhoneNumber())  // Add if missing
        .firstName(user.getFirstName())
        .lastName(user.getLastName())
        .preferredLanguage(user.getPreferredLanguage())
        .build();

    return ResponseEntity.ok(contact);
}
```

**File**: `apps/services/user-service/src/main/java/com/ebanking/user/api/controller/UserController.java`

**Note**: Create `UserContactDTO` in shared DTO library or user-service

### Fix 2: Fix UserEventProducer Bug

**Current Code**:

```java
UserCreatedEvent event = UserCreatedEvent.builder()
    .userId(0L)  // ❌ BUG
    .email(user.getEmail())
    .firstName(user.getFirstName())
    .lastName(user.getLastName())
    .status("ACTIVE")  // ❌ BUG
    .build();
```

**Corrected Code**:

```java
UserCreatedEvent event = UserCreatedEvent.builder()
    .userId(user.getId())  // ✓ Use actual user ID
    .username(user.getUsername())  // ✓ Add username
    .email(user.getEmail())
    .firstName(user.getFirstName())
    .lastName(user.getLastName())
    .status("ACTIVE")  // Consider using UserStatus enum
    .build();
```

**File**: `apps/services/user-service/src/main/java/com/ebanking/user/infrastructure/kafka/UserEventProducer.java`

### Fix 3: Add Missing User Fields

Ensure User entity has:

- `phoneNumber` field (required for SMS notifications)
- `preferredLanguage` field (required for template localization)

**Check**: `apps/services/user-service/src/main/java/com/ebanking/user/domain/model/User.java`

### Fix 4: Add User Verification Endpoint (Optional)

```java
@GetMapping("/api/users/{userId}/exists")
public ResponseEntity<Boolean> userExists(@PathVariable Long userId) {
    return ResponseEntity.ok(userService.userExists(userId));
}
```

---

## 7. Configuration Issues

### 7.1 Kafka Topic Name Inconsistency ⚠️

**user-service publishes to**:

```java
private static final String USER_TOPIC = "user-events";
```

**Shared KafkaTopics defines**:

```java
public static final String USER_CREATED = "user.created";
public static final String USER_UPDATED = "user.updated";
```

**Problem**: Service uses generic topic name instead of specific event types

**Fix**: Use `KafkaTopics.USER_CREATED` instead of `"user-events"`

---

## 8. Action Items

### Priority 1 (Critical) - Required for Functionality

1. **Add UserContactDTO endpoint** to user-service
   - Endpoint: `GET /api/users/{userId}/contact`
   - File: UserController.java
   - Effort: 30 minutes
   - Blocks: Notification service functionality

2. **Fix UserEventProducer userId bug**
   - Change `userId(0L)` to `userId(user.getId())`
   - File: UserEventProducer.java
   - Effort: 5 minutes
   - Blocks: User tracking in notifications

### Priority 2 (High) - Code Quality & Consistency

3. **Add missing User fields**
   - Add `phoneNumber` and `preferredLanguage` to User entity
   - File: User.java
   - Effort: 15 minutes
   - Enables: SMS and localized notifications

4. **Fix Kafka topic naming**
   - Use specific event topics from KafkaTopics class
   - File: UserEventProducer.java
   - Effort: 10 minutes
   - Enables: Proper event routing

### Priority 3 (Medium) - API Consistency

5. **Standardize API versioning**
   - Decide on consistent API path format
   - Current: `/api/v1/users` in user-service
   - Consider: `/api/users` for consistency
   - Effort: 20 minutes

6. **Move UserContactDTO to shared library**
   - Location: `libs/shared/dto/`
   - Effort: 20 minutes
   - Benefits: Type-safe service communication

---

## 9. Testing Impact

### Current State

- User-service tests don't verify integration with notification-service
- Missing integration tests for user contact retrieval

### Required Tests

1. Test UserContactDTO endpoint returns correct data
2. Test UserCreatedEvent is published with correct userId
3. Test integration with notification-service (if implementing REST calls)
4. Test phoneNumber and preferredLanguage fields are populated

---

## 10. Recommendation Summary

| Issue                         | Severity | Action                    |
| ----------------------------- | -------- | ------------------------- |
| Missing user contact endpoint | CRITICAL | Add endpoint immediately  |
| userId hardcoded to 0L        | CRITICAL | Fix before production     |
| Kafka topic naming            | HIGH     | Align with standards      |
| Missing phoneNumber field     | HIGH     | Add to User entity        |
| API version inconsistency     | MEDIUM   | Document or standardize   |
| UserContactDTO location       | LOW      | Move to shared libs later |

---

## Verification Checklist

After implementing fixes, verify:

- [ ] User contact endpoint returns proper UserContactDTO
- [ ] UserCreatedEvent contains correct userId
- [ ] Notification-service can fetch user contacts from user-service
- [ ] User phoneNumber field is populated for SMS
- [ ] User preferredLanguage is set for notifications
- [ ] All unit tests pass
- [ ] All integration tests pass
- [ ] Kafka events use proper topic names from KafkaTopics

---

**Report Generated**: January 6, 2026  
**Status**: Requires Action - 2 Critical, 2 High Priority Issues

# Notification Service

## Overview

Notification service handling email, SMS, and push notifications with template support, user preferences, and comprehensive event-driven architecture.

## Technology Stack

- **Spring Boot 3.3.x** - Application framework
- **Spring Data JPA** - Database access
- **PostgreSQL** - Database for notification logs and preferences
- **Apache Kafka** - Event streaming
- **Thymeleaf** - Email template engine
- **Twilio SDK** - SMS integration
- **JavaMailSender** - Email sending
- **Spring Cloud Eureka** - Service discovery
- **Port:** 8088

## Features

### Core Functionality

- ✅ **Email Notifications** with HTML templates
- ✅ **SMS Notifications** via Twilio
- ✅ **Notification Templates** (Thymeleaf-based)
- ✅ **User Preferences** management
- ✅ **Notification History** tracking
- ✅ **Retry Logic** for failed notifications
- ✅ **Event-Driven** architecture via Kafka

### Notification Types

- Welcome emails
- Transaction confirmations
- Payment failure alerts
- Fraud detection alerts
- Crypto trade confirmations
- Generic alerts

## Architecture

### Database Schema

**notifications** table:

- Stores all sent notifications
- Tracks status (PENDING, SENT, FAILED, RETRYING, CANCELLED)
- Records retry attempts and error messages
- Indexed for efficient querying

**notification_preferences** table:

- User-specific notification settings
- Per-notification-type preferences
- Channel preferences (email, SMS, push, in-app)

> **Note:** Templates are now **file-based only**. The `notification_templates` table and `NotificationTemplate` entity have been removed in favor of simpler file-based Thymeleaf templates stored in `src/main/resources/templates/notifications/`.

## Kafka Integration

### Consumes Topics:

- `user.created` - Welcome emails
- `transaction.completed` - Transaction notifications
- `payment.failed` - Payment failure alerts
- `fraud.detected` - Fraud alerts
- `crypto.trade.executed` - Trade confirmations
- `alert.triggered` - Generic alerts

### Produces Topics:

- `notification.sent` - Successful notifications
- `notification.failed` - Failed notifications

## Configuration

### Email Configuration (SMTP)

```yaml
notification:
  email:
    enabled: true
    host: smtp.gmail.com
    port: 587
    username: ${EMAIL_USERNAME}
    password: ${EMAIL_PASSWORD}
    from-address: noreply@ebanking.com
    from-name: E-Banking Platform
```

**Environment Variables:**

- `EMAIL_USERNAME` - Your SMTP username
- `EMAIL_PASSWORD` - Your SMTP password (use app-specific password for Gmail)

### SMS Configuration (Twilio)

```yaml
notification:
  twilio:
    enabled: false # Enable when credentials are available
    account-sid: ${TWILIO_ACCOUNT_SID}
    auth-token: ${TWILIO_AUTH_TOKEN}
    phone-number: ${TWILIO_PHONE_NUMBER}
```

**Environment Variables:**

- `TWILIO_ACCOUNT_SID` - Your Twilio account SID
- `TWILIO_AUTH_TOKEN` - Your Twilio auth token
- `TWILIO_PHONE_NUMBER` - Your Twilio phone number

### Template Configuration

Templates are **file-based only** and stored in `src/main/resources/templates/notifications/`.

```yaml
notification:
  template:
    # Path relative to classpath:/templates/
    # Final path: classpath:/templates/notifications/{template-name}.html
    base-path: notifications/
    max-retries: 3
    retry-delay-millis: 5000
    retry-enabled: true
```

**Available Templates:**
- `welcome-email.html` - Welcome email for new users
- `transaction-email.html` - Transaction confirmation
- `fraud-alert-email.html` - Fraud detection alert
- `alert-email.html` - Generic alert notification

To add new templates, create `.html` files in `src/main/resources/templates/notifications/` using Thymeleaf syntax.

## REST API Endpoints

### Get User Notifications

```
GET /api/notifications/user/{userId}?page=0&size=20
```

### Get User Preferences

```
GET /api/notifications/preferences/{userId}
```

### Update Preference

```
PUT /api/notifications/preferences
Content-Type: application/json

{
  "userId": 1,
  "notificationType": "TRANSACTION",
  "emailEnabled": true,
  "smsEnabled": false,
  "pushEnabled": true,
  "inAppEnabled": true
}
```

### Get User Statistics

```
GET /api/notifications/stats/{userId}
```

## Email Templates

Located in `src/main/resources/templates/notifications/`:

1. **welcome-email.html** - User registration welcome
2. **transaction-email.html** - Transaction confirmations
3. **fraud-alert-email.html** - Fraud detection alerts
4. **alert-email.html** - Generic alert notifications

### Template Variables

**Welcome Email:**

- `firstName` - User's first name
- `email` - User's email
- `userId` - User ID
- `registrationDate` - Registration date

**Transaction Email:**

- `transactionId` - Transaction ID
- `fromAccount` - Source account
- `toAccount` - Destination account
- `amount` - Transaction amount
- `currency` - Currency code
- `transactionDate` - Transaction timestamp

**Fraud Alert Email:**

- `fraudType` - Type of fraud detected
- `severity` - Severity level (HIGH, MEDIUM, LOW)
- `transactionId` - Related transaction ID
- `detectedAt` - Detection timestamp
- `description` - Fraud description

## Service Layer

### NotificationService

Main orchestration service that:

- Checks user preferences before sending
- Delegates to channel-specific services
- Persists notification records
- Publishes Kafka events
- Handles retry logic

### EmailService

- Sends HTML and plain text emails
- Uses JavaMailSender
- Configurable SMTP settings

### SmsService

- Sends SMS via Twilio
- Handles Twilio API integration
- Configurable phone number

### TemplateService

- Renders Thymeleaf templates
- Supports simple placeholder templates
- Caches templates for performance

## Dependencies

- **Service Discovery** (Eureka) - For service registration
- **PostgreSQL** - For data persistence
- **Kafka** - For event streaming
- **Twilio** (optional) - For SMS functionality
- **SMTP Server** - For email functionality

## Database Setup

The service uses PostgreSQL schema `notifications`:

```sql
-- Schema is auto-created by Hibernate with ddl-auto: update
-- Tables: notifications, notification_templates, notification_preferences
```

## Running the Service

### Prerequisites

1. PostgreSQL running on `localhost:5432`
2. Kafka running on `localhost:9092`
3. Eureka Server running on `localhost:8761`
4. Email SMTP credentials configured
5. (Optional) Twilio credentials for SMS

### Build

```bash
./gradlew :apps:services:notification-service:build
```

### Run

```bash
./gradlew :apps:services:notification-service:bootRun
```

### Docker

```bash
docker build -t notification-service .
docker run -p 8088:8088 \
  -e EMAIL_USERNAME=your-email@gmail.com \
  -e EMAIL_PASSWORD=your-app-password \
  notification-service
```

## Testing

### Send Test Email (via Kafka)

Publish a `user.created` event to Kafka:

```json
{
  "eventId": "test-123",
  "userId": 1,
  "email": "test@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "timestamp": "2025-12-18T10:00:00"
}
```

## Monitoring

### Health Check

```
GET http://localhost:8088/actuator/health
```

### Metrics

```
GET http://localhost:8088/actuator/prometheus
```

## Future Enhancements

- [ ] Push notification support (Firebase/APNs)
- [ ] In-app notification support
- [ ] WhatsApp integration
- [ ] Notification scheduling
- [ ] A/B testing for templates
- [ ] Analytics dashboard
- [ ] Template editor UI
- [ ] Batch notifications
- [ ] Rich media support (attachments, images)

## Troubleshooting

### Email Not Sending

1. Check SMTP credentials in environment variables
2. For Gmail, use App Password (not account password)
3. Enable "Less secure app access" if needed
4. Check `notification.email.enabled=true`

### SMS Not Sending

1. Verify Twilio credentials
2. Check `notification.twilio.enabled=true`
3. Verify phone number format (+1234567890)

### Kafka Connection Issues

1. Verify Kafka is running on configured bootstrap servers
2. Check consumer group ID is unique
3. Review Kafka logs for errors

## License

E-Banking Platform © 2025

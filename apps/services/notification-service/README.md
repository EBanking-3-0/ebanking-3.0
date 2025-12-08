# Notification Service

## Overview

Notification service handling email, SMS, and push notifications.

## Technology

- Spring Boot
- Twilio Integration
- Port: 8088

## Responsibilities

- Email notifications
- SMS notifications (Twilio)
- Push notifications
- Notification preferences
- Notification templates

## Database

- PostgreSQL (notification logs, preferences)

## Kafka Topics

- Consumes: `transaction.completed`, `payment.completed`, `user.registered`, `alert.triggered`
- Produces: `notification.sent`, `notification.failed`

## Setup

```bash
# Build
mvn clean install

# Run
mvn spring-boot:run
```

## Dependencies

- Service Discovery
- Config Server
- Twilio (for SMS)
- PostgreSQL


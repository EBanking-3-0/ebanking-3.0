# Payment Service

## Overview

Payment service handling internal transfers (virements), instant transfers, and anti-fraud rules.

## Technology

- Spring Boot
- Port: 8085

## Responsibilities

- Internal transfers (virements)
- Instant transfers (virements instantanés)
- Anti-fraud rules (règles anti-fraude)
- Payment validation
- QR code payment processing

## Database

- PostgreSQL (payments, transfers, fraud rules)

## Kafka Topics

- Produces: `payment.initiated`, `payment.completed`, `payment.failed`, `fraud.detected`
- Consumes: `account.balance.checked`, `transaction.validated`

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
- Account Service
- Legacy Adapter Service
- PostgreSQL


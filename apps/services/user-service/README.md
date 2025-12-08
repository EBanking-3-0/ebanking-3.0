# User Service

## Overview

User management service handling user profiles, KYC (Know Your Customer) processing, and GDPR consent management.

## Technology

- Spring Boot
- Port: 8083

## Responsibilities

- User profiles management
- KYC processing
- GDPR consent management
- User CRUD operations
- Referral code management

## Database

- PostgreSQL (users, profiles, KYC data, consents)

## Kafka Topics

- Produces: `user.created`, `user.updated`, `user.deleted`, `kyc.completed`
- Consumes: `account.created`

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
- Auth Service
- PostgreSQL


# Account Service

## Overview

Account management service handling accounts, balances, and account statements.

## Technology

- Spring Boot
- Port: 8084

## Responsibilities

- Account management (create, read, update)
- Balance management
- Account statements
- Account types and configurations
- Account validation

## Database

- PostgreSQL (accounts, balances, statements)

## Kafka Topics

- Produces: `account.created`, `account.updated`, `balance.updated`
- Consumes: `transaction.completed`, `transaction.failed`

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
- User Service
- PostgreSQL

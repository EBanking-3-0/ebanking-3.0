# Auth Service

## Overview

Authentication service that interfaces with Keycloak for OAuth2/OIDC authentication. Manages MFA (Multi-Factor Authentication) and user sessions.

## Technology

- Spring Boot
- Keycloak Integration
- Port: 8082

## Responsibilities

- Interface with Keycloak
- Manage MFA (SMS + Biometric)
- Session management
- Token generation and validation
- User authentication

## Database

- PostgreSQL (sessions, MFA tokens)

## Kafka Topics

- Consumes: `user.registered`, `user.deleted`
- Produces: `auth.login`, `auth.logout`, `mfa.verified`

## Setup

```bash
# Build
mvn clean install

# Run
mvn spring-boot:run
```

## Dependencies

- Keycloak
- Service Discovery
- Config Server
- PostgreSQL


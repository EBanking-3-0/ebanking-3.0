# Audit Service

## Overview

Audit service for compliance tracking, audit logging, and GDPR compliance.

## Technology

- Spring Boot
- Port: 8091

## Responsibilities

- Audit logging (journaux d'audit)
- Compliance tracking
- Access logs (journaux d'accès)
- GDPR compliance (RGPD):
  - Consentements (consents)
  - Droit à l'effacement (right to erasure)
  - Anonymisation des logs (log anonymization)

## Database

- MongoDB (audit logs, access logs)

## Kafka Topics

- Consumes: `user.action`, `transaction.completed`, `account.accessed`, `data.deleted`
- Produces: `audit.log.created`, `compliance.alert`

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
- MongoDB


# Legacy Adapter Service

## Overview

SOAP adapter service that wraps legacy banking core systems. Provides REST endpoints internally while communicating with legacy systems via SOAP.

## Technology

- Spring Boot
- SOAP Client
- Port: 8086

## Responsibilities

- SOAP wrapper for legacy banking core
- Normalize SOAP responses to REST format
- Bridge between microservices and legacy systems
- Read/Write operations to legacy core (lecture/écriture)
- Expose REST endpoints internally

## Database

- None (proxies to legacy system)

## Kafka Topics

- Produces: `legacy.sync.completed`, `legacy.sync.failed`
- Consumes: `legacy.sync.requested`

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
- Legacy Banking Core (SOAP)


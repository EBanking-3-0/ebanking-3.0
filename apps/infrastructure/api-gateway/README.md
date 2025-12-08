# API Gateway

## Overview

Spring Cloud Gateway providing a single entry point for all client requests. Handles routing, load balancing, authentication, and rate limiting.

## Technology

- Spring Cloud Gateway
- Port: 8080

## Responsibilities

- Request routing to appropriate microservices
- JWT token validation (Keycloak integration)
- Load balancing
- Rate limiting
- CORS handling
- Request/Response transformation

## Setup

```bash
# Build
mvn clean install

# Run
mvn spring-boot:run
```

## Dependencies

- Service Discovery (Eureka)
- Keycloak (for authentication)


# Service Discovery (Eureka Server)

## Overview

Netflix Eureka Server for service registration and discovery. All microservices register themselves here and discover other services dynamically.

## Technology

- Spring Cloud Netflix Eureka Server
- Port: 8761

## Responsibilities

- Service registration
- Service discovery
- Health monitoring
- Load balancing support

## Setup

```bash
# Build
mvn clean install

# Run
mvn spring-boot:run
```

Access Eureka Dashboard: http://localhost:8761


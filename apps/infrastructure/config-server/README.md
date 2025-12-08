# Config Server

## Overview

Spring Cloud Config Server for centralized configuration management. All microservices fetch their configuration from this service.

## Technology

- Spring Cloud Config Server
- Port: 8888

## Responsibilities

- Centralized configuration storage
- Environment-specific configurations (dev, staging, prod)
- Dynamic configuration updates
- Configuration versioning

## Setup

```bash
# Build
mvn clean install

# Run
mvn spring-boot:run
```

Configuration files are stored in a Git repository or file system.


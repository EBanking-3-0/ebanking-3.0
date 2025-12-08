# GraphQL Gateway

## Overview

GraphQL API Gateway that aggregates and unifies data from multiple microservices. Provides a single GraphQL endpoint for the frontend.

## Technology

- Spring GraphQL
- Port: 8081

## Responsibilities

- Aggregate data from multiple microservices
- Provide unified GraphQL schema
- Query optimization
- Data federation
- Frontend integration (Apollo GraphQL client)

## Setup

```bash
# Build
mvn clean install

# Run
mvn spring-boot:run
```

## GraphQL Endpoint

http://localhost:8081/graphql

## Dependencies

- All business microservices (for data aggregation)
- API Gateway (for routing)


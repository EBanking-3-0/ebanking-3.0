# Analytics Service

## Overview

Analytics service for data aggregation, dashboard statistics, and budget alerts.

## Technology

- Spring Boot
- Port: 8089

## Responsibilities

- Data aggregation for dashboards
- Budget alerts and monitoring
- Dashboard statistics
- Chart data generation
- Business metrics calculation

## Database

- PostgreSQL (analytics data, alerts)
- MongoDB (aggregated metrics)

## Kafka Topics

- Consumes: `transaction.completed`, `payment.completed`, `account.updated`
- Produces: `alert.triggered`, `dashboard.updated`

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
- PostgreSQL
- MongoDB


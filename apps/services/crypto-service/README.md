# Crypto Service

## Overview

Cryptocurrency service handling crypto operations, portfolio management, and real-time market prices.

## Technology

- Spring Boot
- Binance API Integration
- Port: 8087

## Responsibilities

- Cryptocurrency operations (buy/sell via partner APIs)
- Integration with partner APIs (Binance)
- Portfolio management (portefeuille crypto)
- Real-time market prices (taux temps-réel)
- Transaction history (historique)

## Database

- PostgreSQL (crypto wallets, transactions)
- MongoDB (crypto history, market data)

## Kafka Topics

- Produces: `crypto.trade.executed`, `crypto.price.updated`, `crypto.portfolio.updated`
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
- Account Service
- PostgreSQL
- MongoDB


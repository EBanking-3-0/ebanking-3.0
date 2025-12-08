# AI Assistant Service

## Overview

AI assistant service providing chatbot functionality with ChatGPT/Dialogflow integration.

## Technology

- Spring Boot
- ChatGPT/Dialogflow Integration
- LangChain4j
- Port: 8090

## Responsibilities

- Webhook integration with ChatGPT/Dialogflow
- Conversation logging (logs des conversations)
- FAQ management
- Chatbot interactions (assistant bancaire IA)
- Natural language processing

## Database

- MongoDB (conversations, logs)

## Kafka Topics

- Produces: `chat.message.processed`, `chat.session.started`
- Consumes: `user.query.submitted`

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
- ChatGPT/Dialogflow APIs


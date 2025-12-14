# AI Assistant Service

## Overview

AI assistant service providing intelligent customer support with action-oriented capabilities. The assistant can understand natural language queries, execute banking operations, and maintain conversation context.

## Technology

- Spring Boot 3.3.5
- LangChain4j for AI integration (OpenAI, Anthropic support)
- MongoDB for conversation storage
- Apache Kafka for event-driven communication
- Spring Cloud OpenFeign for service communication
- Port: 8090

## Responsibilities

- Natural language processing and intent classification
- Banking action execution (query balances, transactions, accounts, etc.)
- Conversation management and context preservation
- Integration with all banking microservices
- Event-driven communication via Kafka
- JWT-based authentication and authorization

## Architecture

### Core Components

1. **AiService**: Core AI processing with LangChain4j integration
2. **ActionExecutorService**: Executes banking actions
3. **ConversationService**: Manages conversation state in MongoDB
4. **ActionRegistry**: Centralized registry of available actions
5. **Feign Clients**: REST clients for service communication

### Available Actions (Read-Only)

- `query_balance`: Query account balance
- `query_transactions`: Query transaction history
- `query_account_info`: Query account information
- `query_user_info`: Query user profile information
- `query_crypto_portfolio`: Query cryptocurrency portfolio

## API Endpoints

```
POST   /api/chat                    # Send message to assistant
GET    /api/chat/conversations      # Get user's conversations
GET    /api/chat/conversations/{id} # Get specific conversation
DELETE /api/chat/conversations/{id} # Delete conversation
POST   /api/actions/execute         # Direct action execution (admin)
GET    /api/chat/health             # Health check
```

## Database

- **MongoDB**: Stores conversations, messages, and context
  - Collection: `conversations`
  - Documents include: userId, sessionId, messages array, metadata

## Kafka Topics

### Produced Events

- `assistant.message.received` - When user sends a message
- `assistant.action.executed` - When an action is executed
- `assistant.conversation.started` - When a new conversation starts
- `assistant.error.occurred` - When errors occur

### Consumed Events

- `user.created` - Update user context
- `account.created` - Update account context
- `transaction.completed` - Provide transaction updates
- `balance.updated` - Update balance information

## Configuration

### application.yml

```yaml
ai:
  assistant:
    provider: openai  # or anthropic
    model: gpt-4-turbo-preview
    api-key: ${OPENAI_API_KEY}
    temperature: 0.7
    max-tokens: 1000
    enable-actions: true
    conversation:
      ttl-days: 90
      max-messages: 100
```

### Environment Variables

- `OPENAI_API_KEY`: OpenAI API key (required)
- `AI_PROVIDER`: AI provider (default: openai)
- `AI_MODEL`: Model name (default: gpt-4-turbo-preview)
- `MONGODB_HOST`: MongoDB host (default: localhost)
- `MONGODB_PORT`: MongoDB port (default: 27017)
- `MONGODB_DATABASE`: Database name (default: ebanking)

## Authentication

The service uses JWT tokens for authentication:
- Extract `userId` from JWT token in `Authorization` header
- For development/testing: Use `X-User-Id` header as fallback
- All actions verify user authorization before execution

## Setup

```bash
# Build
./gradlew :apps:services:ai-assistant-service:build

# Run
./gradlew :apps:services:ai-assistant-service:bootRun
```

## Dependencies

- Service Discovery (Eureka)
- Config Server
- MongoDB
- Kafka
- OpenAI API key (or other AI provider)

## Usage Example

```bash
# Send a chat message
curl -X POST http://localhost:8090/api/chat \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <JWT_TOKEN>" \
  -H "X-User-Id: 1" \
  -d '{
    "message": "What is my account balance?",
    "sessionId": "session-123"
  }'

# Get conversations
curl http://localhost:8090/api/chat/conversations \
  -H "Authorization: Bearer <JWT_TOKEN>" \
  -H "X-User-Id: 1"
```

## Future Enhancements

- Write actions (transfers, payments) with confirmation flows
- Multi-turn action sequences
- Proactive notifications and alerts
- Advanced NLP for better intent classification
- Support for multiple languages
- Analytics and conversation insights

# AI Assistant Service

## Overview

AI assistant service providing intelligent customer support with action-oriented capabilities. The assistant can understand natural language queries, execute banking operations, and maintain conversation context using LangChain4j and OpenAI.

## Technology Stack

- **Spring Boot** 3.3.5
- **LangChain4j** 0.29.1 for AI integration
- **OpenAI** GPT-4 Turbo Preview
- **MongoDB** for conversation persistence
- **Apache Kafka** for event-driven communication
- **Spring Cloud** (Eureka, OpenFeign)
- **Port**: 8090

## Core Responsibilities

- 🤖 Natural language processing and intent classification
- 🎯 Banking action execution (query balances, transactions, accounts, crypto portfolio)
- 💬 Conversation management and context preservation
- 🔗 Integration with all banking microservices via OpenFeign
- 📡 Event-driven communication via Kafka
- 🔐 JWT-based authentication (optional for development)

## Architecture

### Core Components

| Component | Purpose |
|-----------|---------|
| **AiService** | Core AI processing with LangChain4j integration |
| **ActionExecutorService** | Executes banking actions via Feign clients |
| **ConversationService** | Manages conversation state in MongoDB |
| **ActionRegistry** | Centralized registry of available actions |
| **Feign Clients** | REST clients for inter-service communication |
| **Kafka Producers/Consumers** | Event-driven messaging |

### Available Banking Actions (Read-Only)

The assistant can execute the following actions:

1. **`query_balance`** - Query account balance
2. **`query_transactions`** - Query transaction history
3. **`query_account_info`** - Query account information
4. **`query_user_info`** - Query user profile information
5. **`query_crypto_portfolio`** - Query cryptocurrency portfolio

## API Endpoints

### Chat Endpoints

```http
POST   /api/chat                       # Send message to AI assistant
GET    /api/chat/conversations         # Get user's conversation history
GET    /api/chat/conversations/{id}    # Get specific conversation
DELETE /api/chat/conversations/{id}    # Delete conversation
GET    /api/chat/health                # Health check endpoint
```

### Action Endpoints

```http
POST   /api/actions/execute            # Direct action execution (admin only)
GET    /api/actions                    # List available actions
```

### Actuator Endpoints

```http
GET    /actuator/health                # Service health status
GET    /actuator/prometheus            # Prometheus metrics
GET    /actuator/info                  # Service information
```

## Quick Start

### Prerequisites

1. **Java 21+** installed
2. **MongoDB** running on port 27017
3. **Kafka** running on port 9092
4. **Eureka Service Discovery** running on port 8761
5. **OpenAI API Key** (required for AI functionality)

### Option 1: Run with Nx (Recommended)

```bash
# Start infrastructure
docker compose up -d mongodb kafka

# Start service discovery
nx serve service-discovery

# Start AI assistant service
nx serve ai-assistant-service
```

### Option 2: Run with Gradle

```bash
# Build the service
./gradlew :apps:services:ai-assistant-service:build

# Run the service
./gradlew :apps:services:ai-assistant-service:bootRun
```

### Option 3: Docker Compose

```bash
# Start all services including AI assistant
docker compose up -d ai-assistant-service
```

## Configuration

### Environment Variables

Create a `.env` file in the service directory:

```bash
# AI Configuration (Required)
OPENAI_API_KEY=sk-proj-your-openai-key-here
AI_PROVIDER=openai
AI_MODEL=gpt-4-turbo-preview
AI_TEMPERATURE=0.7
AI_MAX_TOKENS=1000
AI_ENABLE_ACTIONS=true

# MongoDB
MONGODB_HOST=localhost
MONGODB_PORT=27017
MONGODB_DATABASE=ebanking
MONGODB_USERNAME=ebanking
MONGODB_PASSWORD=ebanking123

# Kafka
SPRING_KAFKA_BOOTSTRAP_SERVERS=localhost:9092

# Service Discovery
EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE=http://localhost:8761/eureka/

# Conversation Settings
CONVERSATION_TTL_DAYS=90
CONVERSATION_MAX_MESSAGES=100

# Server
SERVER_PORT=8090
LOG_LEVEL=INFO
SPRING_PROFILES_ACTIVE=dev
```

### Application Profiles

- **dev**: Development mode (default)
- **prod**: Production mode with full security
- **test**: Testing mode with mocked dependencies

## Testing the Service

### 1. Health Check

```bash
curl http://localhost:8090/actuator/health
# Expected: {"status":"UP"}

curl http://localhost:8090/api/chat/health
# Expected: "AI Assistant Service is UP!"
```

### 2. Send Chat Message (Development Mode)

When authentication is disabled for development:

```bash
curl -X POST http://localhost:8090/api/chat \
  -H "Content-Type: application/json" \
  -H "X-User-Id: 123" \
  -d '{
    "message": "What is my account balance?",
    "sessionId": "test-session-001"
  }'
```

**Response:**
```json
{
  "response": "I'll help you check your account balance...",
  "conversationId": "6941cf0f2abaec1e10949604",
  "sessionId": "test-session-001",
  "intent": "query_balance",
  "actionExecuted": "query_balance",
  "actionResult": {
    "balance": "5000.00",
    "currency": "USD"
  }
}
```

### 3. Get Conversation History

```bash
curl http://localhost:8090/api/chat/conversations \
  -H "X-User-Id: 123" | jq .
```

### 4. Get Specific Conversation

```bash
curl http://localhost:8090/api/chat/conversations/{conversationId} \
  -H "X-User-Id: 123" | jq .
```

### 5. With JWT Authentication (Production)

```bash
# Get JWT token from Keycloak
TOKEN=$(curl -s -X POST "http://localhost:8092/realms/ebanking-realm/protocol/openid-connect/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=password" \
  -d "client_id=ebanking-client" \
  -d "username=user" \
  -d "password=password" | jq -r '.access_token')

# Use token in request
curl -X POST http://localhost:8090/api/chat \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"message": "Show me my transactions"}'
```

## Database Schema

### MongoDB - Conversations Collection

```json
{
  "_id": "6941cf0f2abaec1e10949604",
  "userId": 123,
  "sessionId": "test-session-001",
  "messages": [
    {
      "role": "USER",
      "content": "What is my balance?",
      "timestamp": "2025-12-16T22:28:47.886",
      "metadata": {}
    },
    {
      "role": "ASSISTANT",
      "content": "Your current balance is $5,000.00",
      "timestamp": "2025-12-16T22:28:49.979",
      "intent": "query_balance",
      "actionExecuted": "query_balance",
      "actionResult": {"balance": "5000.00"},
      "metadata": {}
    }
  ],
  "createdAt": "2025-12-16T22:28:47.871",
  "updatedAt": "2025-12-16T22:28:49.979",
  "metadata": {}
}
```

## Kafka Integration

### Produced Events

| Topic | Event | Description |
|-------|-------|-------------|
| `assistant.message.received` | User message | When user sends a message |
| `assistant.conversation.started` | New conversation | When new chat session begins |
| `assistant.action.executed` | Action completed | When banking action is executed |
| `assistant.error.occurred` | Error event | When processing errors occur |

### Consumed Events

| Topic | Event | Purpose |
|-------|-------|---------|
| `user.created` | User event | Update user context |
| `account.created` | Account event | Update account context |
| `transaction.completed` | Transaction event | Provide real-time updates |
| `balance.updated` | Balance event | Update balance information |

## Service Integration (OpenFeign Clients)

The AI service communicates with other microservices:

- **Account Service** (port 8084) - Account queries
- **User Service** (port 8083) - User profile data
- **Payment Service** (port 8085) - Transaction history
- **Crypto Service** (port 8087) - Crypto portfolio

All clients use Eureka for service discovery and load balancing.

## Troubleshooting

### Issue: "Invalid API key" error

**Solution:** Configure valid OpenAI API key in `.env`:
```bash
OPENAI_API_KEY=sk-proj-your-valid-key-here
```

### Issue: MongoDB connection failed

**Solution:** Ensure MongoDB is running:
```bash
docker compose up -d mongodb
# Check status
docker compose ps mongodb
```

### Issue: Eureka registration failed

**Solution:** Start service discovery first:
```bash
nx serve service-discovery
# Wait for startup, then start AI service
```

### Issue: Kafka topics not found

**Solution:** Topics are auto-created on first use. Check Kafka:
```bash
docker compose logs kafka
```

### Issue: 401 Unauthorized

**Solution:** For development, disable security in `build.gradle`:
```gradle
// implementation project(':libs:shared:security')  // Commented out
```

## Running Tests

```bash
# Run unit tests
nx run ai-assistant-service:test

# Or with Gradle
./gradlew :apps:services:ai-assistant-service:test

# Run with coverage
./gradlew :apps:services:ai-assistant-service:test jacocoTestReport
```

### Test Coverage

- ✅ **ConversationServiceTest** - Conversation management (3 tests)
- ✅ **ActionRegistryTest** - Action registration (2 tests)
- ✅ **SecurityUtilTest** - JWT security (3 tests)

**Total**: 8 tests passing

## Build & Deployment

### Build JAR

```bash
./gradlew :apps:services:ai-assistant-service:bootJar
# Output: build/libs/ai-assistant-service.jar
```

### Build Docker Image

```bash
docker build -t ai-assistant-service:latest \
  -f apps/services/ai-assistant-service/Dockerfile .
```

### Run Docker Container

```bash
docker run -p 8090:8090 \
  -e OPENAI_API_KEY=your-key \
  -e MONGODB_HOST=mongodb \
  -e KAFKA_BOOTSTRAP_SERVERS=kafka:9092 \
  ai-assistant-service:latest
```

## Monitoring

### Prometheus Metrics

Access metrics at: `http://localhost:8090/actuator/prometheus`

Key metrics:
- `ai_assistant_messages_total` - Total messages processed
- `ai_assistant_actions_executed_total` - Actions executed count
- `ai_assistant_errors_total` - Error count
- `mongodb_conversations_total` - Total conversations

### Health Checks

```bash
# Basic health
curl http://localhost:8090/actuator/health

# Detailed health with components
curl http://localhost:8090/actuator/health | jq .
```

## Security Notes

### Development Mode

Security is **disabled by default** for easier testing. To enable:

1. Uncomment security in `build.gradle`:
   ```gradle
   implementation project(':libs:shared:security')
   ```

2. Restart the service

3. Use JWT tokens for authentication

### Production Mode

In production:
- ✅ JWT authentication required
- ✅ OAuth2 resource server enabled
- ✅ Keycloak integration for SSO
- ✅ Role-based access control (RBAC)

## Performance Considerations

- **Response Time**: ~2-4 seconds (depends on OpenAI API)
- **Throughput**: 50-100 req/sec (limited by AI API rate limits)
- **MongoDB Connection Pool**: 100 connections
- **Kafka Consumer**: 3 concurrent consumers
- **Conversation TTL**: 90 days (configurable)

## Future Enhancements

- [ ] Write actions (transfers, payments) with confirmation flows
- [ ] Multi-turn action sequences
- [ ] Proactive notifications and alerts
- [ ] Advanced NLP for better intent classification
- [ ] Support for multiple languages (i18n)
- [ ] Analytics and conversation insights dashboard
- [ ] Voice interface support
- [ ] Integration with more AI providers (Anthropic Claude, local models)
- [ ] Caching layer for frequently asked questions
- [ ] A/B testing for response optimization

## Dependencies

### Runtime Dependencies

- Spring Boot 3.3.5
- Spring Cloud 2023.0.3
- LangChain4j 0.29.1
- MongoDB Java Driver 5.0.1
- Kafka Clients 3.7.1
- OpenFeign
- Lombok 1.18.36

### External Services

- MongoDB 7.x
- Apache Kafka 3.6.1+
- Eureka Service Discovery
- Keycloak 26.3 (optional, for auth)
- OpenAI API

## Support & Contributing

For issues or questions:
- Check logs: `docker compose logs ai-assistant-service`
- Review Eureka dashboard: `http://localhost:8761`
- Monitor Kafka topics: `docker compose exec kafka kafka-topics.sh --list --bootstrap-server localhost:9092`

## License

Part of the E-Banking 3.0 platform.

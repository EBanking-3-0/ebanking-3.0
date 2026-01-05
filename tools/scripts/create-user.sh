#!/bin/bash

# Get Minikube IP
MINIKUBE_IP=$(minikube -p ebank ip)

# 1. Get Access Token
TOKEN_RESPONSE=$(curl -s -k -X POST \
  https://auth.ebanking.local/realms/ebanking-realm/protocol/openid-connect/token \
  --resolve auth.ebanking.local:443:$MINIKUBE_IP \
  -H 'Content-Type: application/x-www-form-urlencoded' \
  -d 'client_id=ebanking-client' \
  -d 'username=user' \
  -d 'password=password' \
  -d 'grant_type=password')

ACCESS_TOKEN=$(echo $TOKEN_RESPONSE | jq -r .access_token)

if [ "$ACCESS_TOKEN" == "null" ] || [ -z "$ACCESS_TOKEN" ]; then
    echo "❌ Failed to obtain token."
    exit 1
fi

# 2. Execute createUser Mutation
echo "👤 Creating user..."
curl -s -k -X POST \
  https://api.ebanking.local/graphql \
  --resolve api.ebanking.local:443:$MINIKUBE_IP \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -d '{
    "query": "mutation($input: CreateUserInput!) { createUser(input: $input) { id email firstName lastName status } }",
    "variables": {
      "input": {
        "email": "test.user@example.com",
        "firstName": "Test",
        "lastName": "User",
        "phone": "+1234567890"
      }
    }
  }' | jq .

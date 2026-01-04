#!/bin/bash

# Get Minikube IP automatically
MINIKUBE_IP=$(minikube -p ebank ip)

echo "🔍 Minikube IP: $MINIKUBE_IP"

# 1. Get Access Token from Keycloak
echo "🔑 Requesting access token for user 'user'..."
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
    echo "❌ Failed to obtain access token. Response:"
    echo $TOKEN_RESPONSE | jq .
    exit 1
fi

echo "✅ Token obtained successfully."

# 2. Query GraphQL Gateway
echo "📡 Querying GraphQL Gateway (Users)..."
curl -s -k -X POST \
  https://api.ebanking.local/graphql \
  --resolve api.ebanking.local:443:$MINIKUBE_IP \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -d '{"query": "{ users { id email firstName lastName } }"}' | jq .

echo -e "\n🚀 Test complete!"

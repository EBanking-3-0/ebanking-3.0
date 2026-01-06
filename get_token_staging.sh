#!/bin/bash

# Execute the curl command to get the access token for staging
response=$(curl -s -X POST https://bank-auth.h4k5.net/realms/ebanking-realm/protocol/openid-connect/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=password" \
  -d "client_id=ebanking-client" \
  -d "client_secret=8yvemkggR9lvvKa7PW685JaDqWnKemfF" \
  -d "username=testuser" \
  -d "password=password" \
  -d "scope=openid profile email")

# Extract the access_token using jq
access_token=$(echo "$response" | jq -r '.access_token')

if [ -z "$access_token" ] || [ "$access_token" == "null" ]; then
    echo "Failed to retrieve access token. Response:"
    echo "$response"
    exit 1
else
    echo "$access_token"
fi

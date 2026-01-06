#!/bin/bash

# Execute the curl command to get the access token
response=$(curl -s -X POST http://localhost:8092/realms/ebanking-realm/protocol/openid-connect/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=password" \
  -d "client_id=ebanking-client" \
  -d "client_secret=8yvemkggR9lvvKa7PW685JaDqWnKemfF" \
  -d "username=testuser" \
  -d "password=password" \
  -d "scope=openid profile email")

# Check if jq is installed
if ! command -v jq &> /dev/null
then
    echo "jq could not be found. Please install it to parse the JSON response."
    echo "Raw response: $response"
    exit 1
fi

# Extract the access_token using jq
access_token=$(echo "$response" | jq -r '.access_token')

if [ -z "$access_token" ]; then
    echo "Failed to retrieve access token. Response:"
    echo "$response"
    exit 1
else
    echo "$access_token"
fi

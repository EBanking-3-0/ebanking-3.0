#!/bin/bash
# Check if kubectl is available
if ! command -v kubectl &> /dev/null;
then
    echo '{"password": "kubectl not found"}'
    exit 0
fi

# Get the pod name
POD=$(kubectl get pod -n ebanking -l app.kubernetes.io/name=keycloak -o jsonpath="{.items[0].metadata.name}" 2>/dev/null)

if [ -z "$POD" ];
then
  echo '{"password": "Keycloak pod not found in namespace ebanking"}'
  exit 0
fi

# Get the password from env
# We use 'tr -d "\r"' to handle any Windows-style line endings if present
PASS=$(kubectl exec -n ebanking $POD -- env 2>/dev/null | grep KC_BOOTSTRAP_ADMIN_PASSWORD | cut -d '=' -f2 | tr -d '\r')

if [ -z "$PASS" ];
then
  # Try fallback var name just in case
  PASS=$(kubectl exec -n ebanking $POD -- env 2>/dev/null | grep KEYCLOAK_ADMIN_PASSWORD | cut -d '=' -f2 | tr -d '\r')
fi

if [ -z "$PASS" ];
then
   echo '{"password": "Password env var not found"}'
else
   # Safe JSON output using jq if available would be better, but simple string interpolation works for simple passwords
   echo "{\"password\": \"$PASS\"}"
fi

#!/bin/bash
set -e

# Default to "latest" tag if not provided
TAG="${1:-latest}"
NAMESPACE="ebanking"

echo "🚀 Starting Service Deployment (Tag: $TAG)..."

# Ensure we are using Minikube's Docker daemon
echo "🐳 Configuring Docker environment for Minikube..."
eval $(minikube -p ebank docker-env)

# Services to deploy
SERVICES=("user-service" "graphql-gateway" "account-service" "ai-assistant-service" "analytics-service" "audit-service" "auth-service" "crypto-service" "legacy-adapter-service" "notification-service" "payment-service")

# 1. Build and Deploy each service
for SERVICE in "${SERVICES[@]}"; do
    echo "--------------------------------------------------"
    echo "🛠️  Processing $SERVICE..."
    
    # Determine the path - gateway is in infrastructure, others in services
    if [ "$SERVICE" == "graphql-gateway" ]; then
        PROJECT_PATH="apps/infrastructure/graphql-gateway"
    else
        PROJECT_PATH="apps/services/$SERVICE"
    fi

    # Build with Nx (skip tests for speed during deploy, optional)
    echo "📦 Building JAR for $SERVICE..."
    npx nx build $SERVICE --skip-nx-cache

    # Build Docker Image
    echo "🐳 Building Docker image for $SERVICE..."
    docker build -t ebanking-$SERVICE:$TAG -f $PROJECT_PATH/Dockerfile .

    # Deploy with Helm
    echo "helm Upgrade/Install $SERVICE..."
    helm upgrade --install $SERVICE tools/helm/microservice \
        -n $NAMESPACE \
        --set image.repository=ebanking-$SERVICE \
        --set image.tag=$TAG \
        --set image.pullPolicy=Never \
        --set fullnameOverride=$SERVICE \
        --wait

    echo "✅ $SERVICE deployed successfully!"
done

echo "--------------------------------------------------"
echo "🎉 All services deployed successfully!"
echo "👉 API Gateway: https://api.ebanking.local (via Ingress)"

#!/bin/bash
set -e

DOCKER_USER="khabirhakim"
TAG="prod"

echo "🚀 Starting Build & Push process for user: $DOCKER_USER, tag: $TAG"

# Function to build and push
build_and_push() {
    SERVICE=$1
    APP_PATH=$2
    IMAGE_NAME="ebanking-$SERVICE"
    FULL_IMAGE="$DOCKER_USER/$IMAGE_NAME:$TAG"

    echo "--------------------------------------------------"
    echo "🛠️  Processing $SERVICE..."
    
    # 1. Build Artifact (Nx)
    echo "📦 Building artifact for $SERVICE..."
    if [ "$SERVICE" == "frontend" ]; then
        npx nx build frontend --configuration=kubernetes
    else
        npx nx build $SERVICE --configuration=production
    fi

    # 2. Build Docker Image
    echo "🐳 Building Docker image: $FULL_IMAGE"
    docker build -t $FULL_IMAGE -f $APP_PATH/Dockerfile .

    # 3. Push to Registry
    echo "📤 Pushing to Docker Hub..."
    docker push $FULL_IMAGE
    
    echo "✅ $SERVICE done!"
}

# --- Services to Process ---
build_and_push "user-service" "apps/services/user-service"
build_and_push "account-service" "apps/services/account-service"
build_and_push "graphql-gateway" "apps/infrastructure/graphql-gateway"
build_and_push "frontend" "apps/frontend/web-app"

# Uncomment these as you need them
# build_and_push "auth-service" "apps/services/auth-service"
# build_and_push "payment-service" "apps/services/payment-service"
# build_and_push "crypto-service" "apps/services/crypto-service"
# build_and_push "notification-service" "apps/services/notification-service"
# build_and_push "analytics-service" "apps/services/analytics-service"
# build_and_push "audit-service" "apps/services/audit-service"
# build_and_push "legacy-adapter-service" "apps/services/legacy-adapter-service"
# build_and_push "ai-assistant-service" "apps/services/ai-assistant-service"

echo "--------------------------------------------------"
echo "🎉 Build and Push cycle complete!"
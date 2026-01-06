#!/bin/bash
set -e

echo "🚀 Preparing local Minikube Infrastructure deployment..."

# 1. Update Helm dependencies for Infra
echo "📦 Updating Helm dependencies for ebanking-infra..."
cd tools/helm/ebanking-infra
helm dependency build
cd ../../..

# 2. Initialize Terraform
echo "🏗️ Initializing Terraform..."
cd tools/terraform/environments/local
terraform init

# 3. Apply Terraform
echo "🚀 Deploying Infrastructure to Minikube..."
terraform apply -auto-approve

echo "✅ Infrastructure deployment complete! Database, Kafka, etc. are running in namespace 'ebanking'."

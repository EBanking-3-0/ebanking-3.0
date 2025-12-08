# Kubernetes Deployment

This directory contains Kubernetes manifests and Helm charts for deploying E-Banking 3.0 microservices.

## Structure

```
kubernetes/
├── helm-charts/          # Helm charts for all services
│   ├── ebanking/        # Parent chart
│   ├── infrastructure/  # Infrastructure services charts
│   └── services/        # Business services charts
├── manifests/           # Raw Kubernetes manifests
└── terraform/           # Terraform for K8s cluster provisioning
```

## Quick Start

### Using Helm

```bash
# Install all services
helm install ebanking ./helm-charts/ebanking

# Upgrade
helm upgrade ebanking ./helm-charts/ebanking

# Uninstall
helm uninstall ebanking
```

### Using kubectl

```bash
# Apply manifests
kubectl apply -f manifests/

# Check status
kubectl get pods -n ebanking
```

## Prerequisites

- Kubernetes cluster (v1.27+)
- Helm 3.x
- kubectl configured

## Configuration

See `helm-charts/ebanking/values.yaml` for configuration options.

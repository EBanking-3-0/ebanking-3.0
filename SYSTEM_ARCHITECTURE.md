# E-Banking 3.0 System Architecture

This document describes the current production-ready infrastructure and configuration for the E-Banking 3.0 platform deployed on DigitalOcean Kubernetes.

## 1. Cluster Overview (DigitalOcean)

The system runs on a 2-node Kubernetes cluster providing 12GB RAM and 6 vCPUs total capacity.

| Node Name   | Specs           | Purpose                                                   |
| ----------- | --------------- | --------------------------------------------------------- |
| `beefypool` | 8GB RAM, 4 vCPU | Primary node for Infrastructure and Production workloads. |
| `big-pool`  | 4GB RAM, 2 vCPU | Secondary node for Staging and failover workloads.        |

## 2. Network & DNS (Cloudflare)

DNS is managed by Cloudflare with **SSL/TLS Mode: Full**. The Nginx Ingress Controller handles traffic routing and CORS.

### Domains

- **Main UI**: `https://bank.h4k5.net`
- **Staging UI**: `https://bank-staging.h4k5.net`
- **API Gateway (Prod)**: `https://bank-api.h4k5.net`
- **API Gateway (Staging)**: `https://bank-api-staging.h4k5.net`
- **Auth (Keycloak)**: `https://bank-auth.h4k5.net` (Shared)

### CORS Policy

CORS is offloaded to the Nginx Ingress level via annotations in `values-*.yaml`. This ensures consistent header injection across all microservices without code changes.

## 3. Environments & Isolation

The system uses Kubernetes namespaces to isolate environments.

### `ebanking` (Production)

- **Database**: `ebanking_db` (Postgres)
- **Service Naming**: Clean names (e.g., `user-service`) via `fullnameOverride`.
- **Resource Limits**: 256Mi Request / 512Mi Limit for Java services.

### `staging` (Staging)

- **Database**: `staging_db` (Postgres)
- **Isolation**: Uses FQDNs to connect to the shared Postgres instance in the `ebanking` namespace.
- **Resource Limits**: 128Mi Request / 384Mi Limit to ensure co-existence on smaller nodes.

## 4. Monitoring & Alerting

The cluster is monitored using the **Kube-Prometheus-Stack**, providing real-time visibility and proactive incident notification.

### Observability Components

- **Prometheus**: Aggregates metrics from all microservices via `/actuator/prometheus`.
- **Grafana**: Dashboarding engine available at `https://monitoring.h4k5.net`.
- **Alertmanager**: Handles alert deduplication, grouping, and routing to Slack.
- **Node Exporter**: Collects hardware and OS metrics from DigitalOcean nodes.

### Monitoring Infrastructure

- **Namespace**: `monitoring`
- **Auto-Discovery**: Uses Kubernetes `ServiceMonitor` resources to automatically find and scrape new microservices.
- **Data Source**: Integrated Prometheus instance with pre-configured JVM and Spring Boot dashboards.

### Incident Alerting (Slack)

Alerts are routed to the `#alerts` channel via a secure Webhook.

- **Deduplication**: Wait period of 30s to group related failures.
- **Resolution**: Automatic "Resolved" notifications when services recover.
- **Verification**: Alerts can be manually tested using the Alertmanager v2 API.

## 5. Database Strategy

### PostgreSQL

- **Instance**: Single instance running in the `ebanking` namespace.
- **Isolation**: Separate databases (`ebanking_db`, `staging_db`, `keycloak_db`).
- **Initialization**: Automatically provisions schemas for all 10 microservices via `init.sql`.

### MongoDB

- **Provider**: MongoDB Atlas (External).
- **Credentials**: `mongo_user` / `pass`.
- **Databases**: `ebanking_db` (Prod) and `staging_db` (Staging).

## 5. Microservices Configuration

All services are standardized on the following ports:

| Service              | Port | TargetPort |
| -------------------- | ---- | ---------- |
| GraphQL Gateway      | 80   | 8081       |
| Auth Service         | 80   | 8082       |
| User Service         | 80   | 8083       |
| Account Service      | 80   | 8084       |
| Payment Service      | 80   | 8085       |
| Legacy Adapter       | 80   | 8086       |
| Crypto Service       | 80   | 8087       |
| Notification Service | 80   | 8088       |
| Analytics Service    | 80   | 8089       |
| AI Assistant         | 80   | 8090       |
| Audit Service        | 80   | 8091       |
| Web App (Frontend)   | 80   | 80         |

## 6. Frontend Dynamic Configuration

The Angular frontend follows the "Build Once, Deploy Anywhere" pattern:

1.  **Build**: The image is built once without hardcoded API URLs.
2.  **Startup**: A Docker `ENTRYPOINT` script reads environment variables (`API_URL`, etc.).
3.  **Injection**: It generates `assets/env.js` which populates `window.env`.
4.  **Runtime**: The Angular `environment.kubernetes.ts` reads from `window.env`.

## 7. CI/CD Pipeline (GitHub Actions)

Defined in `.github/workflows/cd.yml`.

- **nx affected**: Only builds and deploys services that have changed.
- **Dynamic Tagging**: Uses `staging-{SHA}` tags for traceability.
- **Staging Auto-Deploy**: Pushes to the `staging` namespace on every commit to `main`.
- **Production Gate**: Requires manual approval and passes the same dynamic tag to the `ebanking` namespace.

---

_Last Updated: January 4, 2026_

# Monitoring Setup TODO

This guide outlines the steps to add professional monitoring (Prometheus + Grafana) to the E-Banking 3.0 cluster.

## Phase 1: Infrastructure Installation

- [ ] **Add Helm Repository**
  ```bash
  helm repo add prometheus-community https://prometheus-community.github.io/helm-charts
  helm repo update
  ```

- [ ] **Install Kube-Prometheus-Stack**
  Deploy the full stack into a dedicated namespace.
  ```bash
  helm install monitoring prometheus-community/kube-prometheus-stack \
    --namespace monitoring \
    --create-namespace \
    --set grafana.ingress.enabled=true \
    --set grafana.ingress.hosts='{monitoring.h4k5.net}' \
    --set grafana.ingress.ingressClassName=nginx \
    --set grafana.ingress.annotations."cert-manager.io/cluster-issuer"=selfsigned-cluster-issuer \
    --set prometheus.prometheusSpec.serviceMonitorSelectorNilUsesHelmValues=false
  ```

- [ ] **Configure DNS**
  Point the monitoring domain to your DO Load Balancer IP.
  ```bash
  flarectl dns create --zone h4k5.net --name monitoring --type A --content 68.183.252.138 --proxy
  ```

## Phase 2: App Integration (The "Auto-Magic" Discovery)

- [ ] **Add ServiceMonitor Template**
  Create `tools/helm/microservice/templates/servicemonitor.yaml` to tell Prometheus where to find the `/actuator/prometheus` endpoint.
  
- [ ] **Enable Metrics in Values**
  Update `values.yaml` for all services to toggle the ServiceMonitor.
  ```yaml
  metrics:
    enabled: true
    path: /actuator/prometheus
    port: http # Or the specific targetPort
  ```

## Phase 3: Visualization

- [ ] **Access Grafana**
  - URL: `https://monitoring.h4k5.net`
  - User: `admin`
  - Password: Get via `kubectl get secret -n monitoring monitoring-grafana -o jsonpath="{.data.admin-password}" | base64 --decode`

- [ ] **Import Dashboards**
  Import the following community dashboards for instant visibility:
  - **JVM Dashboard**: ID `4701` (Micrometer standard)
  - **Spring Boot Statistics**: ID `11378`
  - **K8s / Node Exporter**: Included by default in the stack.

## Phase 4: Persistence (Optional but Recommended)

- [ ] **Enable PVC for Prometheus**
  By default, data is lost on pod restart. To save history, enable block storage in the Helm command:
  ```bash
  --set prometheus.prometheusSpec.storageSpec.volumeClaimTemplate.spec.storageClassName=do-block-storage
  --set prometheus.prometheusSpec.storageSpec.volumeClaimTemplate.spec.resources.requests.storage=10Gi
  ```

---
*Created on: January 4, 2026*

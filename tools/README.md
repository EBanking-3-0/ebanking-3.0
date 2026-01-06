# Infrastructure as Code (IaC) & Deployment Guide

This directory contains the complete IaC definition for the E-Banking 3.0 platform, designed using **Terraform** and **Helm**. The architecture follows SRE best practices for monorepos, separating stable infrastructure provisioning from dynamic application deployments.

## 🏗 Architecture Overview

The system is split into two distinct layers:

1.  **Infrastructure Layer (Terraform + Helm Umbrella):**
    - **Goal:** Provision the "Hard Dependencies" that rarely change but are critical for the platform (Database, Message Queue, Identity Provider).
    - **Tool:** Terraform (using the Helm provider).
    - **Scope:** Managed by the `ebanking-infra` chart.

2.  **Application Layer (Generic Helm Chart):**
    - **Goal:** Deploy the 13+ microservices (Account, User, Frontend, etc.).
    - **Tool:** Helm (triggered by CI/CD or Nx).
    - **Scope:** Managed by the `microservice` chart using templates from `ebanking-common`.

---

## 📂 Directory Structure

```plaintext
tools/
├── helm/
│   ├── ebanking-common/      # [Library Chart] Shared templates (DRY). No resources created directly.
│   ├── microservice/         # [Generic Chart] The standard "Cookie Cutter" chart for all apps.
│   │                         # Inherits templates from ebanking-common.
│   └── ebanking-infra/       # [Umbrella Chart] Defines dependencies: Postgres, Kafka, Keycloak, etc.
│                             # Does NOT contain any custom application code.
│
├── terraform/
│   └── environments/
│       └── local/            # Terraform config for Minikube/Kind.
│           └── main.tf       # Deploys the 'ebanking-infra' chart.
│
└── scripts/
    └── deploy-infra.sh       # Helper script to bootstrap the local environment.
```

---

## 🚀 Usage Guide

### 1. Provisioning Infrastructure (The "Base")

This step spins up the Kubernetes cluster resources (PostgreSQL, Kafka, Redis, MongoDB, Keycloak). You generally run this **once** or when infrastructure requirements change.

**Prerequisites:**

- A running Kubernetes cluster (e.g., `minikube start`).
- `terraform` and `helm` installed.

**Command:**

```bash
./tools/scripts/deploy-infra.sh
```

**What happens?**

1.  Helm downloads the Bitnami chart dependencies for `ebanking-infra`.
2.  Terraform initializes in `tools/terraform/environments/local`.
3.  Terraform applies the configuration, installing the `ebanking-infra` release into the `ebanking` namespace.

---

### 2. Deploying Applications (The "Apps")

Applications are deployed independently. We do **not** use Terraform for this. Instead, we use `helm upgrade --install` directly, which allows us to update _only_ the service that changed (perfect for `nx affected`).

**Generic Command:**

```bash
helm upgrade --install <release-name> tools/helm/microservice \
  --namespace ebanking \
  --set image.repository=<your-image> \
  --set image.tag=<git-sha-or-latest> \
  --set env.SPRING_DATASOURCE_URL="jdbc:postgresql://ebanking-infra-postgresql:5432/ebanking_db" \
  --set service.port=8080
```

**Example: Deploying Account Service**

Assuming you have built the image `khabirhakim/ebanking-account-service:latest`:

```bash
helm upgrade --install account-service tools/helm/microservice \
  --namespace ebanking \
  --set image.repository=khabirhakim/ebanking-account-service \
  --set image.tag=latest \
  --set service.port=8084 \
  --set env.SPRING_PROFILES_ACTIVE=prod \
  --set env.SPRING_DATASOURCE_URL="jdbc:postgresql://ebanking-infra-postgresql:5432/ebanking_db" \
  --set env.SPRING_KAFKA_BOOTSTRAP_SERVERS="ebanking-infra-kafka:9092"
```

### 3. Key Concepts Explained

- **ebanking-common:** This is a **Library Chart**. It contains the YAML templates for `Deployment` and `Service`. It enforces a standard deployment pattern across the company. If we want to change how _all_ logs are shipped, we edit this one file.
- **microservice:** This chart has almost no files. It simply `includes` the templates from `ebanking-common`. This is the chart you actually "install".
- **Terraform State:** Terraform tracks the state of the infrastructure release. This ensures that if we change the PostgreSQL password in Terraform, it updates the Kubernetes Secret securely.

---

## 🛠 Troubleshooting

- **"CrashLoopBackOff" on Apps:** Check if the infrastructure is ready.
  - Run `kubectl get pods -n ebanking` to see if Postgres and Kafka are `Running`.
- **"Missing dependency" error:** Run `helm dependency build tools/helm/ebanking-infra`. (The script does this automatically).

---

## 🧐 Code Deep Dive (For Beginners)

If you've never touched Terraform or Helm, here is what the code actually _does_.

### 1. The Helm Charts (YAML Templating)

Helm is a package manager for Kubernetes. A "Chart" is a package.

#### `tools/helm/ebanking-common/` (The Library)

- **`templates/_deployment.yaml`**: This is a blueprint. Instead of hardcoding values, we use `{{ .Values.image.repository }}`.
  - `define "ebanking-common.deployment"`: Defines a reusable function name.
  - `include "ebanking-common.selectorLabels"`: Injects standard labels so K8s knows which pods belong to this app.
  - This file is **never deployed directly**. It is imported by other charts.

#### `tools/helm/microservice/` (The Consumer)

- **`Chart.yaml`**:
  - `type: application`: Means this chart deploys resources.
  - `dependencies`: Points to `../ebanking-common`. This imports the library.
- **`templates/deployment.yaml`**:
  - `{{- include "ebanking-common.deployment" . -}}`: This single line pastes the entire deployment blueprint from the library. This is why the file is so short!

#### `tools/helm/ebanking-infra/` (The Infrastructure)

- **`Chart.yaml`**:
  - Lists **external** dependencies like `postgresql` and `kafka` from the `bitnami` repository.
- **`values.yaml`**:
  - Configures those external charts.
  - `kafka.zookeeper.enabled: false`: Tells the Bitnami chart to use Kraft mode instead of Zookeeper.
  - `postgresql.auth.database: ebanking_db`: Tells the Postgres chart to create this DB on startup.

### 2. Terraform (Infrastructure Provisioning)

Terraform allows you to define infrastructure using HCL (HashiCorp Configuration Language).

#### `tools/terraform/environments/local/main.tf`

Here is a deep breakdown of `tools/terraform/environments/local/main.tf`, which acts as the "Controller" for deploying your infrastructure to your local Kubernetes cluster.

### 1. `terraform { ... }` Block: The Setup

This block tells Terraform _what tools it needs to download_ to do its job.

- `required_providers`: Think of these as "plugins" or "drivers". Terraform itself doesn't know how to talk to Kubernetes or Helm; it needs these providers.
  - `kubernetes`: The driver for talking to the K8s API (creating Namespaces, Pods, etc.).
  - `helm`: The driver for executing Helm commands (installing/upgrading charts).
- `source`: Where to download the driver from (HashiCorp's official registry).
- `version`: Locks the version to ensure stability. If a new version breaks something, your code won't be affected.

### 2. `provider "kubernetes" { ... }` Block: The Connection

This configures the connection to your Kubernetes cluster.

- `config_path = "~/.kube/config"`: This is the critical line. It tells Terraform: _"Look at the standard kubeconfig file on this user's computer."_
  - When you run `minikube start`, Minikube writes connection details (IP, certificate) to this file.
  - Terraform reads it to authenticate as **you**. It effectively runs commands _as if you were typing them into `kubectl`_.

### 3. `provider "helm" { ... }` Block: The Tool Config

This configures the Helm tool itself.

- `kubernetes { config_path = ... }`: Similar to the above, Helm needs to know _which_ cluster to install charts into. We pass the same kubeconfig path so Helm targets your local Minikube.

### 4. `resource "helm_release" "ebanking_infra"` Block: The Action

This is the "meat" of the file. It defines the **Desired State** of your infrastructure.

- `resource "helm_release"`: Tells Terraform, _"I want to manage a Helm Release."_
- `"ebanking_infra"`: This is just the internal Terraform ID for this resource. You could call it `"my_db_stack"`.
- `name = "ebanking-infra"`: This is the **actual release name** inside Kubernetes. When you type `helm list` later, you will see `ebanking-infra`.
- `chart = "../../../helm/ebanking-infra"`: **Crucial Path.** This points Terraform to the _folder_ where we created your Umbrella Chart. It tells Helm to install _that_ specific chart.
- `namespace = "ebanking"`: Deploys everything into the `ebanking` namespace (like a folder) in K8s, keeping it separate from system stuff.
- `create_namespace = true`: A helper flag. If the `ebanking` namespace doesn't exist yet, create it.
- `values = [ file(...) ]`:
  - `file("../../../helm/ebanking-infra/values.yaml")`: This reads your custom configuration file (where we enabled Postgres, disabled Zookeeper, etc.) and passes it to Helm.
  - This is equivalent to running: `helm install -f values.yaml ...`.

**In Plain English:**
"Hey Terraform, download the Kubernetes and Helm drivers. Log in to my local Minikube. Then, look at the Helm chart in the `../../helm/ebanking-infra` folder and install it into the `ebanking` namespace using the settings in `values.yaml`. Call this installation 'ebanking-infra'."

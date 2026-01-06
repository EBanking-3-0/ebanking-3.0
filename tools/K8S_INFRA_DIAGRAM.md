# Kubernetes Infrastructure Diagram - `ebanking` Namespace

Here is a visual representation and summary of your Kubernetes infrastructure deployed in the `ebanking` namespace.


```text
+-----------------------------------------------------------------------------------------+
|                                  NAMESPACE: ebanking                                    |
+-----------------------------------------------------------------------------------------+
|                                                                                         |
|  +---------------------------+                +---------------------------+             |
|  |        Keycloak           |                |       PostgreSQL          |             |
|  |      (Deployment)         |                |      (StatefulSet)        |             |
|  +---------------------------+                +---------------------------+             |
|  | Service:                  |                | Service:                  |             |
|  | ebanking-infra-keycloak   |                | ebanking-infra-postgresql |             |
|  | Port: 8080 (ClusterIP)    |                | Port: 5432 (ClusterIP)    |             |
|  +---------------------------+                +---------------------------+             |
|               |                                            ^  |                         |
|               v                                            |  v                         |
|  +---------------------------+     JDBC Connection         | +-----------------------+  |
|  | Pod:                      | --------------------------> | | Pod:                  |  |
|  | ebanking-infra-keycloak-* |                             | | ebanking-infra-       |  |
|  | (Replica: 1)              |                             | | postgresql-0          |  |
|  |                           |                             | | (Replica: 1)          |  |
|  +---------------------------+                             | +-----------------------+  |
|                                                            |                            |
|                                                            | +-----------------------+  |
|                                                            | | Persistence (PVC)     |  |
|                                                            | | data-ebanking-infra-  |  |
|                                                            | | postgresql-0          |  |
|                                                            | +-----------------------+  |
|                                                                                         |
|  +---------------------------+                                                          |
|  |         Kafka             |                                                          |
|  |     (StatefulSet)         |                                                          |
|  +---------------------------+                                                          |
|  | Service:                  |                                                          |
|  | ebanking-infra-kafka-     |                                                          |
|  | broker                    |                                                          |
|  | Port: 9092 (ClusterIP)    |                                                          |
|  +---------------------------+                                                          |
|               |                                                                         |
|               v                                                                         |
|  +---------------------------+        +-----------------------+                         |
|  | Pod:                      |        | Persistence (PVC)     |                         |
|  | ebanking-infra-kafka-     | -----> | data-ebanking-infra-  |                         |
|  | broker-0                  |        | kafka-broker-0        |                         |
|  | (Replica: 1)              |        +-----------------------+                         |
|  +---------------------------+                                                          |
|                                                                                         |
+-----------------------------------------------------------------------------------------+
```

## Infrastructure Details Summary

| Component | Resource Type | Name | Replicas | Service Name | Ports | Version |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **Kafka** | StatefulSet | `ebanking-infra-kafka-broker` | **1** | `ebanking-infra-kafka-broker` | 9092 (Broker), 9091 (Controller) | 3.7.1 |
| **PostgreSQL** | StatefulSet | `ebanking-infra-postgresql` | **1** | `ebanking-infra-postgresql` | 5432 | 16.2.0 (Chart 18.2.0) |
| **Keycloak** | Deployment | `ebanking-infra-keycloak` | **1** | `ebanking-infra-keycloak` | 8080 | 26.4.7 |

Your infrastructure is currently running in a minimal, single-replica development mode (1 replica for each stateful service). This is suitable for development but would need scaling (replicas > 1) for production high availability.

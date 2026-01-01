terraform {
  required_providers {
    helm = {
      source = "hashicorp/helm"
      version = "2.11.0"
    }
    kubernetes = {
      source = "hashicorp/kubernetes"
      version = "2.23.0"
    }
    external = {
      source = "hashicorp/external"
      version = "2.3.1"
    }
  }
}

provider "helm" {
  kubernetes {
    config_path = "~/.kube/config"
  }
}

provider "kubernetes" {
  config_path = "~/.kube/config"
}

variable "db_password" {
  type    = string
  default = "pass"
}

variable "keycloak_admin_password" {
  type    = string
  default = "pass"
}

variable "db_username" {
  type    = string
  default = "ebanking_user"
}


resource "kubernetes_namespace" "ebanking" {
  metadata {
    name = "ebanking"
  }
}

resource "kubernetes_secret" "postgres_secret" {
  metadata {
    name      = "postgres-secret"
    namespace = kubernetes_namespace.ebanking.metadata[0].name
  }

  data = {
    postgres-password = var.db_password
    password          = var.db_password
    username          = var.db_username
  }
}

resource "kubernetes_secret" "keycloak_admin_secret" {
  metadata {
    name      = "keycloak-admin-secret"
    namespace = kubernetes_namespace.ebanking.metadata[0].name
  }

  data = {
    admin-password = var.keycloak_admin_password
  }
}


resource "helm_release" "ebanking_infra" {
  name       = "ebanking-infra"
  chart      = "../../../helm/ebanking-infra"
  namespace  = kubernetes_namespace.ebanking.metadata[0].name
  create_namespace = false # Managed by kubernetes_namespace resource

  values = [
    file("../../../helm/ebanking-infra/values.yaml")
  ]

  depends_on = [
    kubernetes_secret.postgres_secret,
    kubernetes_secret.keycloak_admin_secret
  ]
}

# -------------------------------------------------------------------
# CERT-MANAGER AUTOMATION
# -------------------------------------------------------------------

resource "kubernetes_namespace" "cert_manager" {
  metadata {
    name = "cert-manager"
  }
}

resource "helm_release" "cert_manager" {
  name       = "cert-manager"
  repository = "https://charts.jetstack.io"
  chart      = "cert-manager"
  version    = "v1.13.3"
  namespace  = kubernetes_namespace.cert_manager.metadata[0].name

  set {
    name  = "installCRDs"
    value = "true"
  }
}

# Local Self-Signed Cluster Issuer
resource "kubernetes_manifest" "self_signed_cluster_issuer" {
  manifest = {
    apiVersion = "cert-manager.io/v1"
    kind       = "ClusterIssuer"
    metadata = {
      name = "selfsigned-cluster-issuer"
    }
    spec = {
      selfSigned = {}
    }
  }

  depends_on = [helm_release.cert_manager]
}

# Configured password (what we INTENDED)
output "keycloak_admin_password_configured" {
  value     = var.keycloak_admin_password
  sensitive = true
}

# ACTUAL running password retrieved from Pod Env
data "external" "keycloak_env_password" {
  program = ["bash", "${path.module}/get_keycloak_password.sh"]
  
  # Re-run this script on every apply to capture the current state
  depends_on = [helm_release.ebanking_infra]
}

output "keycloak_admin_password_actual" {
  value     = data.external.keycloak_env_password.result.password
  sensitive = false # Set to false so you can see it easily in CLI
}

terraform {
  required_providers {
    helm = {
      source  = "hashicorp/helm"
      version = "2.11.0"
    }
    kubernetes = {
      source  = "hashicorp/kubernetes"
      version = "2.23.0"
    }
    external = {
      source  = "hashicorp/external"
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
  name             = "ebanking-infra"
  chart            = "../../../helm/ebanking-infra"
  namespace        = kubernetes_namespace.ebanking.metadata[0].name
  create_namespace = false # Managed by kubernetes_namespace resource

  values = [
    file("../../../helm/ebanking-infra/values.yaml"),
    file("../../../helm/ebanking-infra/values-prod.yaml") # Keeping prod overrides for DO
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

# Using null_resource + kubectl to bypass Terraform CRD validation race condition
resource "null_resource" "self_signed_cluster_issuer" {
  triggers = {
    cert_manager_id = helm_release.cert_manager.id
  }

  provisioner "local-exec" {
    command = <<EOF
kubectl apply -f - <<MANIFEST
apiVersion: cert-manager.io/v1
kind: ClusterIssuer
metadata:
  name: selfsigned-cluster-issuer
spec:
  selfSigned: {}
MANIFEST
EOF
  }

  depends_on = [helm_release.cert_manager]
}
terraform {
  required_providers {
    google = {
      source  = "hashicorp/google"
      version = "~> 5.30"
    }
  }
}

variable "project" {
  type = string
}

variable "region" {
  type    = string
  default = "europe-west2"
}

# The shared lending database (tables in loan-db/schema.sql).
resource "google_sql_database_instance" "lending" {
  name             = "meridian-lending-db"
  database_version = "POSTGRES_15"
  region           = var.region
  project          = var.project

  settings {
    tier = "db-custom-4-16384"
    backup_configuration {
      enabled                        = true
      point_in_time_recovery_enabled = true
    }
  }
  deletion_protection = true
}

# Event bus for loan-core -> notify-svc.
resource "google_managed_kafka_cluster" "events" {
  cluster_id = "meridian-events"
  location   = var.region
  project    = var.project

  capacity_config {
    vcpu_count   = 3
    memory_bytes = 3221225472
  }
  gcp_config {
    access_config {
      network_configs {
        subnet = "projects/${var.project}/regions/${var.region}/subnetworks/lending"
      }
    }
  }
}

# Customer statements, kept for audit.
resource "google_storage_bucket" "statements" {
  name                        = "meridian-lending-statements"
  location                    = "EU"
  project                     = var.project
  uniform_bucket_level_access = true

  versioning {
    enabled = true
  }
}

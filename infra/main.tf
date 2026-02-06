resource "azurerm_resource_group" "rg" {
  count    = var.resource_group_name == "" ? 1 : 0
  name     = "${local.name_prefix}-rg"
  location = var.location
}

data "azurerm_resource_group" "rg" {
  count = var.resource_group_name != "" ? 1 : 0
  name  = var.resource_group_name
}

locals {
  rg_name     = var.resource_group_name != "" ? data.azurerm_resource_group.rg[0].name : azurerm_resource_group.rg[0].name
  rg_location = var.resource_group_name != "" ? data.azurerm_resource_group.rg[0].location : azurerm_resource_group.rg[0].location
}

resource "azurerm_log_analytics_workspace" "law" {
  name                = "${local.name_prefix}-law"
  location            = local.rg_location
  resource_group_name = local.rg_name
  sku                 = "PerGB2018"
  retention_in_days   = 30
  daily_quota_gb      = 0.05
}

resource "azurerm_container_app_environment" "cae" {
  name                       = "${local.name_prefix}-cae"
  location                   = local.rg_location
  resource_group_name        = local.rg_name
  log_analytics_workspace_id = azurerm_log_analytics_workspace.law.id
}

resource "azurerm_cosmosdb_account" "cosmos" {
  name                = "${replace(local.name_prefix, "-", "")}cos"
  location            = local.rg_location
  resource_group_name = local.rg_name
  offer_type          = "Standard"
  kind                = "GlobalDocumentDB"

  free_tier_enabled = var.cosmos_free_tier

  consistency_policy {
    consistency_level = "Session"
  }

  geo_location {
    location          = local.rg_location
    failover_priority = 0
  }
}

resource "azurerm_cosmosdb_sql_database" "db" {
  name                = "${local.name_prefix}-db"
  resource_group_name = local.rg_name
  account_name        = azurerm_cosmosdb_account.cosmos.name
  throughput          = 400
}

resource "azurerm_cosmosdb_sql_container" "entries" {
  name                = "entries"
  resource_group_name = local.rg_name
  account_name        = azurerm_cosmosdb_account.cosmos.name
  database_name       = azurerm_cosmosdb_sql_database.db.name
  partition_key_paths = ["/userId"]
}

resource "azurerm_cosmosdb_sql_container" "profiles" {
  name                = "profiles"
  resource_group_name = local.rg_name
  account_name        = azurerm_cosmosdb_account.cosmos.name
  database_name       = azurerm_cosmosdb_sql_database.db.name
  partition_key_paths = ["/userId"]
}

resource "azurerm_cosmosdb_sql_container" "catalog" {
  name                = "catalog"
  resource_group_name = local.rg_name
  account_name        = azurerm_cosmosdb_account.cosmos.name
  database_name       = azurerm_cosmosdb_sql_database.db.name
  partition_key_paths = ["/userId"]
}

resource "azurerm_container_app" "api" {
  name                         = "${local.name_prefix}-api"
  container_app_environment_id = azurerm_container_app_environment.cae.id
  resource_group_name          = local.rg_name
  revision_mode                = "Single"

  ingress {
    external_enabled = true
    target_port      = 80
    traffic_weight {
      percentage      = 100
      latest_revision = true
    }
  }

  template {
    min_replicas = 0
    max_replicas = 1

    container {
      name   = "api"
      image  = var.container_image
      cpu    = 0.25
      memory = "0.5Gi"
    }
  }
}

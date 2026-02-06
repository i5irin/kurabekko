output "resource_group_name" {
  value = local.rg_name
}

output "container_app_fqdn" {
  value = azurerm_container_app.api.ingress[0].fqdn
}

output "cosmos_account_name" {
  value = azurerm_cosmosdb_account.cosmos.name
}

output "cosmos_endpoint" {
  value = azurerm_cosmosdb_account.cosmos.endpoint
}

output "cosmos_database_name" {
  value = azurerm_cosmosdb_sql_database.db.name
}

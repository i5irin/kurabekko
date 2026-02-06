variable "location" {
  type    = string
  default = "japaneast"
}

variable "project" {
  type    = string
  default = "kurabekko"
}

variable "env" {
  type    = string
  default = "dev"
}

variable "resource_group_name" {
  type    = string
  default = ""
}

variable "container_image" {
  type    = string
  default = "nginx:1.27"
}

variable "cosmos_free_tier" {
  type    = bool
  default = true
}

variable "tenant_id" {
  type = string
}

variable "subscription_id" {
  type = string
}

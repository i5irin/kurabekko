# Kurabekko

For household shopping, a web app that calculates and compares unit prices from a product’s price and quantity.

## Development

### Deploy

#### Infrastructure

##### 1. Prerequisites

- Logged in with Azure CLI: `az login`
- Terraform 1.14+ (this repo’s devcontainer is recommended)
- Prepare `infra/dev.tfvars` (create it based on `terraform.tfvars.example`)
- `cd infra`

##### 2. Initialize

```bash
cd infra
terraform init
```

Note: If you do the following and manually register providers beforehand, Terraform is less likely to fail during provider registration.

```bash
export ARM_SKIP_PROVIDER_REGISTRATION=true
```

##### 3. Backup local state

```bash
cd infra
cp -a terraform.tfstate terraform.tfstate.bak.$(date +%Y%m%d-%H%M%S) 2>/dev/null || true
```

##### 4. Create a plan

```bash
terraform plan -var-file=dev.tfvars -out tfplan
```

##### 5. Apply / Deploy

```bash
terraform apply tfplan
```

Note: If it fails, you can also run with reduced parallelism by adding `-parallelism=1`.

##### Troubleshooting

- If `apply` fails with EOF / connection reset, rerun the same apply
  - `terraform apply tfplan`
- If the state is out of sync (e.g., the resource exists in Azure but not in Terraform state)
  - First, verify the resource exists using Azure CLI
  - If the resource exists, use `terraform import`; if it does not exist, clean up with `terraform state rm`

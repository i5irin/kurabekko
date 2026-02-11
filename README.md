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

#### Application

##### 1. Prerequisites

- Docker available (Docker Desktop or Docker Engine)
- Logged in to GHCR (GitHub Container Registry)
  - Create a GitHub **classic** Personal Access Token (PAT) with `write:packages`
- Prepare `infra/dev.tfvars` (create it based on `terraform.tfvars.example`)
  - Set `container_image` to the image you will push
  - Set application secrets (API key / LINE secret / LINE token) as Terraform variables

##### 2. Build and push the container image (multi-arch)

Log in to GHCR:

```bash
echo "$GHCR_TOKEN" | docker login ghcr.io -u <GITHUB_USERNAME> --password-stdin
```

Build and push a multi-arch image (recommended):

```bash
cd api

IMAGE="ghcr.io/<GITHUB_USERNAME>/kurabekko-api"
TAG="0.1.0"

docker buildx create --use --name kurabekko-builder 2>/dev/null || docker buildx use kurabekko-builder
docker buildx inspect --bootstrap

docker buildx build \
  --platform linux/amd64,linux/arm64 \
  -t "${IMAGE}:${TAG}" \
  --push .
```

(Optional) Make the package **Public** on GitHub:

- GitHub → Profile → Packages → `kurabekko-api` → Package settings → Change visibility → Public

##### 3. Configure deployment inputs (Terraform tfvars)

Edit `infra/dev.tfvars`:

```hcl
# Container image to deploy
container_image = "ghcr.io/<GITHUB_USERNAME>/kurabekko-api:0.1.0"

# Application runtime settings
app_api_key          = "<APP_API_KEY>"
line_channel_secret  = "<LINE_CHANNEL_SECRET>"
line_channel_token   = "<LINE_CHANNEL_TOKEN>"

# Cosmos (optional)
cosmos_enabled = false
# cosmos_endpoint = "<COSMOS_ENDPOINT>"
# cosmos_key      = "<COSMOS_KEY>"
# cosmos_database = "kurabekko-dev-db"
```

##### 4. Deploy the new image and secrets

Apply (from `infra/`):

```bash
cd infra
terraform apply -var-file=dev.tfvars
```

Note: If Terraform fails while reading Container Apps secrets (e.g., `listSecrets` connection reset), retry with:

```bash
terraform apply -var-file=dev.tfvars -refresh=false
```

##### 5. Smoke test

Using the Azure-provided FQDN:

```bash
FQDN="$(cd infra && terraform output -raw container_app_fqdn)"
curl -i "https://${FQDN}/actuator/health"
```

##### 6. DNS (CNAME)

Create a `CNAME` record in your DNS provider:

- Hostname: `<your-api-hostname>` (e.g., `api.example.com`)
- Target: Terraform output `container_app_fqdn`

You can obtain the target with:

```bash
cd infra
terraform output -raw container_app_fqdn
```

After DNS propagation, verify the endpoint via your hostname:

```bash
curl -i "https://<your-api-hostname>/actuator/health"
```

##### 7. LINE webhook configuration

In LINE Developers Console → Messaging API → Webhook settings:

- Webhook URL:

```text
https://<your-api-hostname>/line/webhook
```

- Enable Webhooks and run “Verify”

##### 8. If your DNS provider cannot issue SSL for your hostname (optional)

Some DNS/CDN providers’ free plans may not cover certain hostnames (e.g., deep subdomain levels) when proxying TLS.
If you cannot use your provider’s free SSL termination, you can terminate TLS at Azure instead:

- Set the DNS record to **DNS-only** (do not proxy) and point it to `container_app_fqdn`
- Configure a Custom Domain + Managed Certificate on Azure Container Apps
- Verify `https://<your-api-hostname>/...` works after the certificate is issued

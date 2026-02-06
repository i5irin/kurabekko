# Java 21
FROM mcr.microsoft.com/devcontainers/java:21

ARG TERRAFORM_VERSION=1.14.4
ARG TFLINT_VERSION=0.53.0
ARG TFSEC_VERSION=1.28.6

USER root

# Python and other tools
RUN apt-get update && apt-get install -y --no-install-recommends \
    curl ca-certificates gnupg lsb-release unzip zip git jq make \
    python3 python3-pip python3-venv \
    && rm -rf /var/lib/apt/lists/*

# Terraform
RUN curl -fsSL "https://releases.hashicorp.com/terraform/${TERRAFORM_VERSION}/terraform_${TERRAFORM_VERSION}_linux_$(dpkg --print-architecture).zip" -o /tmp/tf.zip \
    && unzip /tmp/tf.zip -d /usr/local/bin \
    && rm /tmp/tf.zip \
    && terraform -version

# tflint
RUN curl -fsSL "https://github.com/terraform-linters/tflint/releases/download/v${TFLINT_VERSION}/tflint_linux_$(dpkg --print-architecture).zip" -o /tmp/tflint.zip \
    && unzip /tmp/tflint.zip -d /usr/local/bin \
    && rm /tmp/tflint.zip \
    && tflint --version

# tfsec
RUN curl -fsSL "https://github.com/aquasecurity/tfsec/releases/download/v${TFSEC_VERSION}/tfsec-linux-$(dpkg --print-architecture)" -o /usr/local/bin/tfsec \
    && chmod +x /usr/local/bin/tfsec \
    && tfsec --version

# Azure CLI: Install via pip
RUN python3 -m venv /opt/azcli \
    && /opt/azcli/bin/pip install --upgrade pip \
    && /opt/azcli/bin/pip install azure-cli \
    && ln -s /opt/azcli/bin/az /usr/local/bin/az \
    && az version

USER vscode

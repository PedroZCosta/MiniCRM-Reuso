#!/usr/bin/env bash
# Ambiente comum dos scripts do Database First em .NET (use com: source scripts/ambiente.sh).
# O SDK e as ferramentas ficam no home do usuário (~/.dotnet e ~/.dotnet-tools), sem sudo.
export DOTNET_ROOT="${DOTNET_ROOT:-$HOME/.dotnet}"
export PATH="$DOTNET_ROOT:$HOME/.dotnet-tools:$PATH"
export DOTNET_CLI_TELEMETRY_OPTOUT=1
export DOTNET_NOLOGO=1

# Banco da demonstração (docker compose up -d --wait). Mesmos padrões do backend do Mini CRM.
DB_HOST="${DB_HOST:-localhost}"
DB_PORT="${DB_PORT:-3307}"
DB_NAME="${DB_NAME:-mini_crm_db}"
DB_USER="${DB_USER:-miniCrmUser}"
DB_PASSWORD="${DB_PASSWORD:-crm123P@}"
export CONEXAO="Server=${DB_HOST};Port=${DB_PORT};Database=${DB_NAME};User=${DB_USER};Password=${DB_PASSWORD}"

PROJETO="MiniCrm.DbFirst"

if ! command -v dotnet >/dev/null 2>&1; then
  echo "ERRO: dotnet não encontrado. Instale o SDK 9 (veja o README) ou ajuste DOTNET_ROOT." >&2
  exit 1
fi

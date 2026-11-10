#!/usr/bin/env bash
# Sobe a aplicação gerada em http://localhost:8095 (telas em /, Swagger em /swagger).
set -euo pipefail
cd "$(dirname "$0")/.."
source scripts/ambiente.sh
cd "$PROJETO"
export ConnectionStrings__MiniCrm="$CONEXAO"
# Aplicação de demonstração: em Development o dotnet run serve os arquivos estáticos (CSS/JS) do projeto.
export ASPNETCORE_ENVIRONMENT="${ASPNETCORE_ENVIRONMENT:-Development}"
exec dotnet run --no-launch-profile --urls "http://localhost:${SERVER_PORT:-8095}"

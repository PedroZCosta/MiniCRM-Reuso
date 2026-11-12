#!/usr/bin/env bash
# Etapa 1 do Database First em .NET: lê o esquema do MySQL e gera as entidades e o DbContext.
# Pré-requisitos: SDK .NET 9, dotnet-ef e o MySQL da demonstração no ar (docker compose up -d --wait).
set -euo pipefail
cd "$(dirname "$0")/.."
source scripts/ambiente.sh

# Remove o que foi gerado antes (o --force do scaffold só sobrescreve, não apaga classes que sumiram do banco).
find "$PROJETO/Models" -name '*.cs' ! -name 'ErrorViewModel.cs' -delete 2>/dev/null || true
rm -f "$PROJETO/Data/MiniCrmContext.cs"

# O CRUD gerado (controllers e views) depende das entidades: se ficasse, o build que o scaffold dispara
# quebraria sem elas. Apaga tudo o que o gerar-crud.sh criou (HomeController e Views/Home|Shared são do template).
find "$PROJETO/Controllers" -maxdepth 1 -name '*Controller.cs' ! -name 'HomeController.cs' -delete 2>/dev/null || true
rm -rf "$PROJETO/Controllers/Api"
for d in "$PROJETO"/Views/*/; do
  case "$(basename "$d")" in Home|Shared) ;; *) rm -rf "$d" ;; esac
done

# O scaffold compila o projeto antes de gerar, e o Program.cs usa o MiniCrmContext, que ainda não existe.
# Este DbContext vazio é provisório, só para o build passar; o scaffold o sobrescreve (--force).
mkdir -p "$PROJETO/Data"
cat > "$PROJETO/Data/MiniCrmContext.cs" <<'CS'
using Microsoft.EntityFrameworkCore;

namespace MiniCrm.DbFirst.Data;

// Provisório: existe só para o projeto compilar antes da geração. O scaffold o sobrescreve.
public partial class MiniCrmContext : DbContext
{
    public MiniCrmContext(DbContextOptions<MiniCrmContext> options) : base(options) { }
}
CS

echo "==> EF Core Scaffolding: lendo o esquema de ${DB_NAME} em ${DB_HOST}:${DB_PORT} e gerando as classes"
dotnet ef dbcontext scaffold "$CONEXAO" Pomelo.EntityFrameworkCore.MySql \
  --project "$PROJETO" \
  --output-dir Models --context MiniCrmContext --context-dir Data \
  --data-annotations --no-onconfiguring --no-pluralize --force 2>&1 | grep -vE "^Build started|^Build succeeded"

# Peculiaridade do Pomelo + EF: com --data-annotations, colunas datetime(6) saem com [MaxLength(6)] e o
# ASP.NET quebra na validação (InvalidCastException -> HTTP 500). Remove só esse atributo nos campos de data.
# O perl edita os arquivos no lugar e preserva os fins de linha (CRLF) que a ferramenta gerou.
perl -0pi -e 's/[ \t]*\[MaxLength\(\d+\)\]\r?\n(?=(?:[ \t]*\[[^\n]*\]\r?\n)*[ \t]*public (?:DateTime|DateOnly|TimeOnly)\??\s)//g' "$PROJETO"/Models/*.cs

echo
echo "Entidades geradas em $PROJETO/Models:"
find "$PROJETO/Models" -name '*.cs' ! -name 'ErrorViewModel.cs' -printf '  %f\n' | sort
echo "DbContext gerado em $PROJETO/Data/MiniCrmContext.cs"

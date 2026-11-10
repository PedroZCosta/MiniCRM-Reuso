#!/usr/bin/env bash
# Etapa 2 do Database First em .NET: para cada entidade gerada, cria o CRUD completo.
#   - controller MVC + 5 views Razor (Index, Details, Create, Edit, Delete)  -> as telas
#   - controller de API REST (GET, POST, PUT, DELETE) em /api/<Entidades>   -> documentado no Swagger
# Pré-requisito: ./scripts/gerar.sh já executado (Models/ e Data/MiniCrmContext.cs existem).
set -euo pipefail
cd "$(dirname "$0")/.."
source scripts/ambiente.sh
cd "$PROJETO"

CONTEXTO=Data/MiniCrmContext.cs
[ -f "$CONTEXTO" ] || { echo "ERRO: $CONTEXTO não existe. Rode ./scripts/gerar.sh primeiro." >&2; exit 1; }

# Pares "Entidade Colecao" lidos do DbContext gerado (ex.: "Cliente Clientes").
mapfile -t PARES < <(sed -nE 's/^ *public virtual DbSet<([A-Za-z]+)> ([A-Za-z]+) \{ get; set; \}.*/\1 \2/p' "$CONTEXTO")
[ "${#PARES[@]}" -gt 0 ] || { echo "ERRO: nenhum DbSet encontrado em $CONTEXTO." >&2; exit 1; }

# O aspnet-codegenerator só conhece SQL Server, SQLite, Cosmos e Postgres e exige o pacote do SQL Server
# no projeto, mesmo com o provider MySQL. Ele entra só durante a geração e sai no final.
PACOTE_TEMP=Microsoft.EntityFrameworkCore.SqlServer
if grep -q "$PACOTE_TEMP" "$PROJETO.csproj" 2>/dev/null; then ja_tinha=1; else ja_tinha=0; fi
if [ "$ja_tinha" = 0 ]; then dotnet add package "$PACOTE_TEMP" --version 9.0.23 >/dev/null; fi
limpar() { if [ "$ja_tinha" = 0 ]; then dotnet remove package "$PACOTE_TEMP" >/dev/null 2>&1 || true; fi; }
trap limpar EXIT

# Apaga o CRUD gerado antes (o --force só sobrescreve, não remove o que saiu do banco).
rm -rf Controllers/Api
for par in "${PARES[@]}"; do
  colecao="${par#* }"
  rm -f "Controllers/${colecao}Controller.cs"
  rm -rf "Views/${colecao}"
done

dotnet build -nologo -v q >/dev/null

echo "==> Gerando CRUD (controller MVC + views + controller de API) para ${#PARES[@]} entidades"
for par in "${PARES[@]}"; do
  entidade="${par%% *}"; colecao="${par#* }"
  dotnet-aspnet-codegenerator controller -name "${colecao}Controller" -m "$entidade" -dc MiniCrmContext \
    --relativeFolderPath Controllers --useDefaultLayout --referenceScriptLibraries -f >/dev/null
  dotnet-aspnet-codegenerator controller -name "${colecao}Controller" -m "$entidade" -dc MiniCrmContext \
    -api --relativeFolderPath Controllers/Api -f >/dev/null
  printf '  %-17s -> tela /%-18s  API /api/%s\n' "$entidade" "$colecao (5 views)" "$colecao"
done

echo "Pronto. Compile e rode com:  ./scripts/rodar.sh"

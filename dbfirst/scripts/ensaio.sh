#!/usr/bin/env bash
# Ensaio completo do Database First em .NET, do zero:
#   banco novo -> entidades -> CRUD -> aplicação no ar -> CRUD pela API -> CRUD pelas telas -> para a aplicação.
# Uso: ./scripts/ensaio.sh [pasta-para-salvar-as-saidas]
set -euo pipefail
cd "$(dirname "$0")/.."
SAIDAS="${1:-}"
[ -n "$SAIDAS" ] && mkdir -p "$SAIDAS"

etapa() { # nome-do-arquivo, comando exibido, comando...
  local arq="$1" exibido="$2"; shift 2
  echo "=== $exibido"
  if [ -n "$SAIDAS" ]; then { echo "\$ $exibido"; "$@" 2>&1; } | tee "$SAIDAS/$arq" | sed 's/^/    /'
  else "$@" 2>&1 | sed 's/^/    /'; fi
}

etapa 01-compose.txt "docker compose down -v && docker compose up -d --wait" \
  bash -c 'docker compose down -v 2>&1 | tail -2; docker compose up -d --wait 2>&1 | tail -4'
etapa 02-gerar.txt "./scripts/gerar.sh" ./scripts/gerar.sh
etapa 03-gerar-crud.txt "./scripts/gerar-crud.sh" ./scripts/gerar-crud.sh

echo "=== ./scripts/rodar.sh (em segundo plano)"
LOG=$(mktemp); setsid ./scripts/rodar.sh > "$LOG" 2>&1 & APP=$!
trap 'kill -- -$APP 2>/dev/null || true; rm -f "$LOG"' EXIT
for _ in $(seq 1 60); do
  grep -q "Now listening" "$LOG" 2>/dev/null && break
  grep -qE "Unhandled exception|Application startup exception" "$LOG" 2>/dev/null && { cat "$LOG"; exit 1; }
  sleep 2
done
grep -q "Now listening" "$LOG" || { echo "ERRO: a aplicação não subiu"; cat "$LOG"; exit 1; }
{ echo '$ ./scripts/rodar.sh'; grep -E "Now listening|Application started" "$LOG" | sed -E 's/^ +//'
  echo; echo '$ curl http://localhost:8095/swagger/v1/swagger.json   (recursos da API)'
  curl -s http://localhost:8095/swagger/v1/swagger.json | python3 -I -c "
import json,sys
for p in sorted(json.load(sys.stdin)['paths']):
    if '{' not in p: print('  ', p)"; } | { [ -n "$SAIDAS" ] && tee "$SAIDAS/04-app.txt" || cat; } | sed 's/^/    /'

etapa 05-demo-crud.txt  "./scripts/demo-crud.sh"  ./scripts/demo-crud.sh
etapa 06-demo-telas.txt "./scripts/demo-telas.sh" ./scripts/demo-telas.sh

echo "=== ensaio concluído: ${SAIDAS:-sem arquivos salvos}"

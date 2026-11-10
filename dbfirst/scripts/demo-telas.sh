#!/usr/bin/env bash
# Demonstra o CRUD pelas TELAS Razor geradas (Views/Clientes), enviando os formulários como o navegador:
# abre a página, lê o token antiforgery, envia o POST. Confere cada passo pela API.
# Pré-requisito: aplicação no ar em http://localhost:8095 (./scripts/rodar.sh).
set -euo pipefail
BASE="${BASE_URL:-http://localhost:8095}"
JAR=$(mktemp); trap 'rm -f "$JAR"' EXIT

token() { curl -s -c "$JAR" -b "$JAR" "$BASE$1" | python3 -I -c "
import re,sys
print(re.search(r'name=\"__RequestVerificationToken\"[^>]*value=\"([^\"]+)\"', sys.stdin.read()).group(1))"; }
post() { # caminho, dados...
  local caminho="$1"; shift
  curl -s -o /dev/null -w '%{http_code} -> %{redirect_url}' -c "$JAR" -b "$JAR" -X POST "$BASE$caminho" "$@"
}
total() { curl -s "$BASE/api/Clientes" | python3 -I -c "import json,sys; print(len(json.load(sys.stdin)))"; }

echo "Clientes no banco antes: $(total)"

echo; echo "1) CREATE pela tela  (GET /Clientes/Create -> POST do formulário)"
T=$(token /Clientes/Create)
echo "   POST /Clientes/Create: $(post /Clientes/Create --data-urlencode "__RequestVerificationToken=$T" \
  --data-urlencode "Nome=Cliente Criado pela Tela Razor" --data-urlencode "Email=razor@dbfirst.com" \
  --data-urlencode "Empresa=DB First Ltda" --data-urlencode "Telefone=(41) 3000-1111" --data-urlencode "Status=PROSPECT" \
  --data-urlencode "Excluido=0" --data-urlencode "CriadoEm=2026-11-09T19:20" --data-urlencode "AtualizadoEm=2026-11-09T19:20" \
  --data-urlencode "IdVendedor=3")"
ID=$(curl -s "$BASE/api/Clientes" | python3 -I -c "
import json,sys
print([c['idCliente'] for c in json.load(sys.stdin) if c['email']=='razor@dbfirst.com'][0])")
echo "   cliente criado com id $ID; total agora: $(total)"

echo; echo "2) UPDATE pela tela  (GET /Clientes/Edit/$ID -> POST do formulário)"
T=$(token "/Clientes/Edit/$ID")
echo "   POST /Clientes/Edit/$ID: $(post "/Clientes/Edit/$ID" --data-urlencode "__RequestVerificationToken=$T" \
  --data-urlencode "IdCliente=$ID" --data-urlencode "Nome=Cliente Criado pela Tela Razor" --data-urlencode "Email=razor@dbfirst.com" \
  --data-urlencode "Empresa=DB First Ltda" --data-urlencode "Telefone=(41) 3000-1111" --data-urlencode "Status=ATIVO" \
  --data-urlencode "Excluido=0" --data-urlencode "CriadoEm=2026-11-09T19:20" --data-urlencode "AtualizadoEm=2026-11-09T19:25" \
  --data-urlencode "IdVendedor=3")"
curl -s "$BASE/api/Clientes/$ID" | python3 -I -c "import json,sys; d=json.load(sys.stdin); print('   agora:', d['nome'], '|', d['status'])"

echo; echo "3) DELETE pela tela  (GET /Clientes/Delete/$ID -> POST de confirmação)"
T=$(token "/Clientes/Delete/$ID")
echo "   POST /Clientes/Delete/$ID: $(post "/Clientes/Delete/$ID" --data-urlencode "__RequestVerificationToken=$T")"
echo "   GET depois de excluir: HTTP $(curl -s -o /dev/null -w '%{http_code}' "$BASE/api/Clientes/$ID"); total agora: $(total)"

echo; echo "CRUD pelas telas executado nas tabelas do MySQL."

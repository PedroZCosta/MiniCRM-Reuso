#!/usr/bin/env bash
# Demonstra o CRUD da API gerada (controllers de Controllers/Api) contra o MySQL real.
# Pré-requisito: aplicação no ar em http://localhost:8095 (./scripts/rodar.sh).
set -euo pipefail
B="${BASE_URL:-http://localhost:8095/api}"
H='Content-Type: application/json'
py() { python3 -I -c "$1"; }

echo "1) READ   - listando clientes que já estão no banco"
curl -s "$B/Clientes" | py "
import json,sys
for c in json.load(sys.stdin): print('   -', c['nome'], '|', c['email'], '|', c['status'])"

echo; echo "2) CREATE - criando um cliente pela API gerada"
NOVO=$(curl -s -X POST "$B/Clientes" -H "$H" -d '{"nome":"Cliente Criado pela API .NET","email":"dotnet@dbfirst.com","empresa":"DB First Ltda","telefone":"(41) 3000-0000","status":"PROSPECT","excluido":0,"criadoEm":"2026-11-09T19:15:00","atualizadoEm":"2026-11-09T19:15:00","idVendedor":3}')
ID=$(echo "$NOVO" | py "import json,sys; print(json.load(sys.stdin)['idCliente'])")
echo "   criado com id $ID"

echo; echo "3) UPDATE - mudando o status de PROSPECT para ATIVO (PUT com o objeto)"
CORPO=$(echo "$NOVO" | py "
import json,sys
d=json.load(sys.stdin); d['status']='ATIVO'
for k in ('idVendedorNavigation','interacoes','oportunidades','tarefas'): d.pop(k,None)
print(json.dumps(d))")
echo "   PUT respondeu HTTP $(curl -s -o /dev/null -w '%{http_code}' -X PUT "$B/Clientes/$ID" -H "$H" -d "$CORPO")"
curl -s "$B/Clientes/$ID" | py "import json,sys; d=json.load(sys.stdin); print('   agora:', d['nome'], '|', d['status'])"

echo; echo "4) DELETE - removendo o cliente"
echo "   DELETE respondeu HTTP $(curl -s -o /dev/null -w '%{http_code}' -X DELETE "$B/Clientes/$ID")"
echo "   GET depois de excluir: HTTP $(curl -s -o /dev/null -w '%{http_code}' "$B/Clientes/$ID")"

echo; echo "CRUD completo executado nas tabelas do MySQL."

# SPEC-04 · Tarefas, Notificações, Dashboard e Relatórios

**Responsável:** ______
**Requisitos:** RF10, RF11, RF12, RF13, RF15, RF21, RF22, RF23, RF24, RF28, RF31
**Patterns desta spec:** Template Method nº 2 (`GeradorNotificacaoBase`), Template Method nº 3 (`ExportadorBase`), Strategy (exportadores) e Facade (dashboard)

É a spec mais larga, mas quase tudo é leitura/agregação. Depende dos métodos públicos de
`ClienteService` (SPEC-02), `OportunidadeService` (SPEC-03) e do `EscopoCarteira` (SPEC-01).

## 0. Ajuste inicial (SPEC-00 §3)

Primeiro PR: `TipoNotificacao` → `D15, D1, HOJE, VENCIDA, REUNIAO`.

## 1. Tarefas (RF10, RF28)

| Endpoint | Permissão | Regra |
|----------|-----------|-------|
| `POST /api/v1/tarefas` | TAREFA_CRIAR | RN-01 |
| `GET /api/v1/tarefas?situacao=&clienteId=&page=` | TAREFA_VER | RN-03; escopo de carteira |
| `PUT /api/v1/tarefas/{id}` | TAREFA_EDITAR | título, descrição, vencimento |
| `PATCH /api/v1/tarefas/{id}/concluir` | TAREFA_CONCLUIR | RN-02 |
| `PATCH /api/v1/tarefas/{id}/reabrir` | TAREFA_CONCLUIR | desfaz conclusão |

Criação: `{ "titulo", "descricao", "dataVencimento": "2026-09-20", "idCliente": 7, "idOportunidade": 12 }`.
Cliente/oportunidade opcionais e independentes (RF10). Responsável = usuário logado
(GERENTE/ADMIN podem atribuir com `"idUsuario"`).

- **RN-01** `titulo` e `dataVencimento` obrigatórios; cliente/oportunidade, se vierem, validados pelos services das outras specs.
- **RN-02** Concluir: `concluida=true`, `concluidaEm=agora`. **"Vencida" nunca vai para o banco**: é calculada (`dataVencimento < hoje && !concluida`) (RF28).
- **RN-03** `situacao` = `PENDENTE | VENCIDA | CONCLUIDA`, resolvida na consulta.
- Método público para o histórico da SPEC-02: `List<Tarefa> listarDoCliente(Integer idCliente)`.

## 2. Notificações (RF13, RF23, RF24)

Internas, sem e-mail (RF23). Cada usuário só vê as suas.

| Endpoint | Permissão | Regra |
|----------|-----------|-------|
| `GET /api/v1/notificacoes?lida=&page=` | NOTIFICACAO_VER | `geradaEm` desc |
| `GET /api/v1/notificacoes/contador` | NOTIFICACAO_VER | `{ "naoLidas": 4 }` |
| `PATCH /api/v1/notificacoes/{id}/lida` | NOTIFICACAO_VER | de outro usuário → 404 |
| `PATCH /api/v1/notificacoes/marcar-todas` | NOTIFICACAO_VER | RF23 |

Geração: um job `@Scheduled(cron = "0 0 7 * * *")` (+ uma rodada no start) varre as
tarefas não concluídas e cria notificações por faixa: vence em 15 dias → D15, amanhã → D1,
hoje → HOJE, atrasada → VENCIDA. Interação REUNIAO com data futura → REUNIAO.

- **RN-04** Sem duplicata: no máximo 1 notificação por (tarefa, faixa). O job verifica antes de inserir; rodar 2x não duplica.
- **RN-05** Mensagens do RF24 (ex.: D1 → "Sua atividade vai vencer amanhã"). O pop-up "uma vez por sessão" é responsabilidade do front.

### Pattern · Template Method nº 2: `GeradorNotificacaoBase`

```java
public abstract class GeradorNotificacaoBase {
    public final Notificacao gerar(Tarefa tarefa) {   // esqueleto fixo
        Notificacao n = new Notificacao();
        n.setUsuario(tarefa.getUsuario());
        n.setTarefa(tarefa);
        n.setTipo(tipo());                            // passos das filhas
        n.setMensagem(mensagem(tarefa));
        n.setLida(false);
        return n;
    }
    protected abstract TipoNotificacao tipo();
    protected abstract String mensagem(Tarefa tarefa);
}
// filhas: GeradorD15, GeradorD1, GeradorHoje, GeradorVencida
```

## 3. Dashboard (RF11, RF15) · Pattern: Facade

`GET /api/v1/dashboard` · `DASHBOARD_VER`. Números já recortados pelo escopo: VENDEDOR
recebe os dele, GERENTE os da equipe, ADMIN tudo (RF15).

```json
{ "totalClientes": 28,
  "clientesPorStatus": { "PROSPECT": 10, "ATIVO": 15, "INATIVO": 3 },
  "oportunidadesAbertas": 24,
  "oportunidadesPorEtapa": { "PROSPECCAO": 9, "CONTATO": 7, "PROPOSTA": 8 },
  "valorEmNegociacao": 1485000.00,
  "alertasTarefas": { "vencidas": 3, "hoje": 2, "amanha": 1, "em15dias": 4 } }
```

`DashboardFacade` chama `ClienteService` + `OportunidadeService` + `TarefaService` e monta
o DTO. O controller fica com ~3 linhas. Para a apresentação: sem a fachada, o controller
conheceria 3 services e a ordem certa de chamá-los; com ela, conhece um único ponto.

## 4. Ranking (RF31)

`GET /api/v1/ranking?inicio=&fim=` · `RANKING_VER` (VENDEDOR não tem → 403).
ADMIN vê geral; GERENTE só os próprios vendedores.

```json
{ "posicoes": [ { "posicao": 1, "vendedor": "Juliana Torres",
                  "valorGanho": 178000.00, "oportunidadesFechadas": 5 } ] }
```

Ordena por `valorGanho` desc, empate por `oportunidadesFechadas` desc. Fonte:
`OportunidadeService.fechadasNoPeriodo(...)`.

## 5. Relatórios (RF12, RF21, RF22) · Patterns: Strategy + Template Method nº 3

| Endpoint | Permissão |
|----------|-----------|
| `GET /api/v1/relatorios/clientes?formato=csv&status=&inicio=&fim=` | RELATORIO_EXPORTAR |
| `GET /api/v1/relatorios/oportunidades?formato=csv&etapa=&inicio=&fim=` | RELATORIO_EXPORTAR |

- **RN-06** Exporta o resultado COMPLETO do filtro, não uma página (RF12). `inicio`/`fim` filtram por `criadoEm` (RF21).
- **RN-07** Colunas do RF12. Clientes: nome, e-mail, telefone, empresa, status, vendedor. Oportunidades: cliente, valor estimado, etapa, data prevista, vendedor.
- **RN-08** VENDEDOR sem a permissão → 403; GERENTE exporta só a equipe (RF22).
- CSV obrigatório (`text/csv`, UTF-8, separador `;`, `Content-Disposition: attachment`). PDF é desejável: mesma interface, entra se der tempo.

```java
public interface RelatorioExportStrategy {                 // Strategy
    String formato();                                      // "csv", "pdf"
    byte[] exportar(List<String> cabecalho, List<List<String>> linhas);
}

public abstract class ExportadorBase implements RelatorioExportStrategy {  // Template Method
    public final byte[] exportar(List<String> cabecalho, List<List<String>> linhas) {
        abrir();
        escreverCabecalho(cabecalho);
        linhas.forEach(this::escreverLinha);
        return fechar();
    }
    protected abstract void abrir();
    protected abstract void escreverCabecalho(List<String> c);
    protected abstract void escreverLinha(List<String> l);
    protected abstract byte[] fechar();
}
```

O controller resolve a estratégia num `Map<String, RelatorioExportStrategy>` (o Spring
injeta a lista de implementações); formato desconhecido → 400. Ponto de apresentação:
Strategy é a escolha da família de algoritmo em runtime; Template Method é o esqueleto
comum dentro de cada família. Dois padrões, mesmo código, papéis diferentes.

## 6. Checklist de auditoria (colar no PR)

- [ ] "Vencida" não existe no banco: mudar o relógio do teste muda a situação (RF28)
- [ ] Job rodado 2x no mesmo dia não duplica notificações (RN-04)
- [ ] Cada faixa gera a mensagem certa via seu gerador (RF24)
- [ ] Notificação alheia: não lista e PATCH → 404 (RF23)
- [ ] Dashboard do VENDEDOR ≠ do GERENTE com os mesmos dados (RF15)
- [ ] Ranking: VENDEDOR → 403; GERENTE só a equipe; ordenação correta (RF31)
- [ ] CSV traz TODAS as linhas com 45 registros e página de 20 (RF12)
- [ ] `?formato=xml` → 400; CSV abre no Excel com acentos corretos
- [ ] Período `inicio`/`fim` respeitado (RF21)

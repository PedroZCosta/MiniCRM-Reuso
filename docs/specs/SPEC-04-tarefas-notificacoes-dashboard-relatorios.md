# SPEC-04 · Tarefas, Notificações, Dashboard e Relatórios

**Responsável:** ______
**Requisitos:** RF10, RF11, RF12, RF13, RF15, RF21, RF22, RF23, RF24, RF28, RF31 · UC04, UC07, UC10, UC12, UC13, UC17
**Design patterns:** Factory Method (notificações), Strategy + Template Method (exportadores), Facade (dashboard)

É a spec mais larga, porém quase toda de leitura/agregação: as escritas são tarefas e
notificações. Depende das interfaces `OportunidadeConsultaService` (SPEC-03),
`ClienteConsultaService` (SPEC-02) e `EscopoCarteira` (SPEC-01).

## 0. Ajustes iniciais (SPEC-00 §3)

Primeiro PR: `TipoNotificacao` → `D15, D1, HOJE, VENCIDA, REUNIAO` (diagrama de classes do PDF).

## 1. Entidades sob responsabilidade deste módulo

`Tarefa`, `Notificacao` (já criadas).

## 2. Tarefas (RF10, RF28)

| Endpoint | Permissão | Observação |
|----------|-----------|------------|
| `POST /api/v1/tarefas` | `TAREFA_CRIAR` | RN-01 |
| `GET /api/v1/tarefas?situacao=&faixa=&clienteId=&page=` | `TAREFA_VER` | RN-03 |
| `PUT /api/v1/tarefas/{id}` | `TAREFA_EDITAR` | título, descrição, vencimento |
| `PATCH /api/v1/tarefas/{id}/concluir` | `TAREFA_CONCLUIR` | RN-02 |
| `PATCH /api/v1/tarefas/{id}/reabrir` | `TAREFA_CONCLUIR` | desfaz conclusão |

Criação: `{ "titulo", "descricao", "dataVencimento": "2026-09-20", "idCliente": 7, "idOportunidade": 12 }`.
`idCliente`/`idOportunidade` opcionais e independentes (RF10); responsável = autenticado
(GERENTE/ADMIN podem atribuir a outro com `"idUsuario"`).

- **RN-01** `titulo` e `dataVencimento` obrigatórios; cliente/oportunidade, quando informados, devem existir e estar no escopo (validar pelas interfaces públicas, nunca por repository alheio).
- **RN-02** Concluir seta `concluida=true` + `concluidaEm=agora`. Só "concluída" é persistida; **"vencida" é sempre calculada** (`dataVencimento < hoje && !concluida`), nunca gravada (RF28).
- **RN-03** `situacao` = `PENDENTE | VENCIDA | CONCLUIDA` (calculado); `faixa` = `D15 | D1 | HOJE | VENCIDA` para a tela inicial (RF13). Listagem respeita `EscopoCarteira`: vendedor vê as dele, gerente as da equipe.

## 3. Notificações (RF13, RF23, RF24, UC07, UC12)

Internas ao sistema, sem e-mail (RF23).

| Endpoint | Permissão | Observação |
|----------|-----------|------------|
| `GET /api/v1/notificacoes?lida=&page=` | `NOTIFICACAO_VER` | só as do usuário autenticado, `geradaEm` desc |
| `GET /api/v1/notificacoes/contador` | `NOTIFICACAO_VER` | `{ "naoLidas": 4 }` (badge) |
| `PATCH /api/v1/notificacoes/{id}/lida` | `NOTIFICACAO_VER` | 404 se de outro usuário |
| `PATCH /api/v1/notificacoes/marcar-todas` | `NOTIFICACAO_VER` | RF23 "Marcar todas como lidas" |

### 3.1 Geração agendada (ator Schedule do diagrama de casos de uso)

Job diário `@Scheduled(cron = "0 0 7 * * *")` + execução no start: varre tarefas não
concluídas do dia e publica `TarefaVencendoEvent` por faixa: vence em 15 dias → `D15`,
amanhã → `D1`, hoje → `HOJE`, atrasada → `VENCIDA`. Reuniões agendadas (interação REUNIAO
com data futura) → `REUNIAO`.

- **RN-04** Idempotência: no máximo 1 notificação por (tarefa, faixa, usuário). Reexecutar o job não duplica (constraint de unicidade lógica verificada antes do insert).
- **RN-05** Notificação nasce `lida=false`; a mensagem segue o RF24: `D1` → "Sua atividade vai vencer amanhã", etc. O pop-up "uma vez por sessão" é controle do front; o backend entrega tudo via listagem.

### 3.2 Pattern A · Factory Method: `NotificacaoFactory`

Cada faixa produz mensagem e conteúdo próprios; o listener não conhece as concretas:

```java
public abstract class NotificacaoFactory {
    public final Notificacao criar(TarefaVencendoEvent evento) {   // template do produto
        Notificacao n = instanciar(evento);                        // factory method
        n.setLida(false);
        return n;
    }
    protected abstract Notificacao instanciar(TarefaVencendoEvent evento);
    public abstract TipoNotificacao faixa();
}
// concretas: NotificacaoD15Factory, NotificacaoD1Factory, NotificacaoHojeFactory,
// NotificacaoVencidaFactory, NotificacaoReuniaoFactory — registradas num Map<TipoNotificacao, NotificacaoFactory>
```

## 4. Dashboard e Ranking (RF11, RF15, RF31, UC04, UC13)

### 4.1 `GET /api/v1/dashboard` · permissão `DASHBOARD_VER` · Pattern B: Facade

Uma chamada, todos os indicadores, já recortados pelo `EscopoCarteira` (RF15: VENDEDOR
recebe só os próprios números; GERENTE, os da equipe; ADMIN, tudo):

```json
{
  "totalClientes": 28,
  "clientesPorStatus": { "PROSPECT": 10, "ATIVO": 15, "INATIVO": 3 },
  "oportunidadesAbertas": 24,
  "oportunidadesPorEtapa": { "PROSPECCAO": 9, "CONTATO": 7, "PROPOSTA": 8 },
  "valorEmNegociacao": 1485000.00,
  "alertasTarefas": { "vencidas": 3, "hoje": 2, "amanha": 1, "em15dias": 4 }
}
```

`DashboardFacade` orquestra `ClienteConsultaService` + `OportunidadeConsultaService` +
`TarefaService` e monta o DTO. Controller com 3 linhas: é a demonstração do padrão
(subsistemas complexos atrás de uma interface única).

### 4.2 `GET /api/v1/ranking?inicio=&fim=` · permissão `RANKING_VER` (RF31)

VENDEDOR → 403 (sem a permissão). ADMIN vê geral; GERENTE, só os próprios vendedores:

```json
{ "periodo": { "inicio": "2026-01-01", "fim": "2026-09-10" }, "posicoes": [
  { "posicao": 1, "vendedor": "Juliana Torres", "valorGanho": 178000.00, "oportunidadesFechadas": 5 }
] }
```

Ordenação: `valorGanho` desc, empate por `oportunidadesFechadas` desc.

## 5. Relatórios (RF12, RF21, RF22, UC17) · Pattern C: Strategy + Template Method

| Endpoint | Permissão | Observação |
|----------|-----------|------------|
| `GET /api/v1/relatorios/clientes?formato=csv&status=&inicio=&fim=` | `RELATORIO_EXPORTAR` | colunas RF12 |
| `GET /api/v1/relatorios/oportunidades?formato=csv&etapa=&inicio=&fim=` | `RELATORIO_EXPORTAR` | colunas RF12 |

- **RN-06** Exporta o RESULTADO COMPLETO do filtro, não a página (RF12). Período filtra `criadoEm` (clientes) / `fechadaEm` ou `criadoEm` (oportunidades, documentar a escolha no código) (RF21).
- **RN-07** Colunas fixas do RF12. Clientes: nome, e-mail, telefone, empresa, status, vendedor responsável. Oportunidades: cliente, valor estimado, etapa, data prevista, vendedor responsável.
- **RN-08** VENDEDOR não tem `RELATORIO_EXPORTAR` → 403; GERENTE exporta só a equipe (escopo aplicado ANTES da exportação) (RF22).
- CSV obrigatório (`text/csv`, UTF-8 com BOM, separador `;`, header `Content-Disposition: attachment`); PDF desejável, entra se sobrar tempo com a mesma interface.

```java
public interface RelatorioExportStrategy {              // Strategy
    String formato();                                   // "csv", "pdf"
    byte[] exportar(RelatorioDados dados);
    MediaType mediaType();
}
public abstract class ExportadorBase implements RelatorioExportStrategy {  // Template Method
    public final byte[] exportar(RelatorioDados dados) {
        var saida = abrirDocumento();
        escreverCabecalho(saida, dados.colunas());
        dados.linhas().forEach(l -> escreverLinha(saida, l));
        return fechar(saida);
    }
    protected abstract ...;
}
// CsvExportador e (futuro) PdfExportador; resolução por Map<String, RelatorioExportStrategy>;
// formato desconhecido -> 400
```

## 6. Checklist de auditoria (colar no PR)

- [ ] Tarefa vencida NÃO tem flag no banco: mudar o relógio do teste muda a situação (RF28)
- [ ] Job rodado 2x no mesmo dia não duplica notificações (RN-04)
- [ ] Cada faixa (D15/D1/HOJE/VENCIDA) gera a mensagem certa via factory (RF24)
- [ ] Notificação de outro usuário: GET não lista, PATCH lida → 404 (RF23)
- [ ] Dashboard de VENDEDOR ≠ dashboard do GERENTE com os mesmos dados de teste (RF15)
- [ ] Ranking: VENDEDOR → 403; GERENTE só vê a equipe; ordenação por valor ganho (RF31)
- [ ] CSV traz TODAS as linhas do filtro com 45 registros e page size 20 (RF12)
- [ ] CSV abre no Excel com acentuação correta (BOM) e colunas exatas do RF12
- [ ] Relatório com `inicio`/`fim` respeita o período (RF21)

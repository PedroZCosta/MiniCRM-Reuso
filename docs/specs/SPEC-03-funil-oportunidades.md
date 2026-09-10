# SPEC-03 · Funil de Oportunidades

**Responsável:** ______
**Requisitos:** RF07, RF08, RF09, RF19, RF29 · UC11
**Design patterns:** State (transições de etapa), Observer (lado publicador dos eventos de domínio)

## 0. Ajustes iniciais (SPEC-00 §3)

Primeiro PR: `EtapaOportunidade` → `PROSPECCAO, CONTATO, PROPOSTA, GANHA, PERDIDA` (RF08:
o funil tem as colunas Prospecção, Contato, Proposta e Fechado; "Fechado" se materializa
como GANHA ou PERDIDA). Ajustar também `historico_etapa` nos testes, que usa o mesmo enum.

## 1. Entidades sob responsabilidade deste módulo

`Oportunidade`, `HistoricoEtapa`, `MotivoPerda` (já criadas) + seed dos motivos (RF29):
`preço`, `concorrente`, `sem verba`, `sem resposta`.

## 2. Endpoints

Visibilidade sempre via `EscopoCarteira`. Permissões: `OPORTUNIDADE_*` (SPEC-01 §6).

| Endpoint | Permissão | Observação |
|----------|-----------|------------|
| `POST /api/v1/oportunidades` | `OPORTUNIDADE_CRIAR` | RN-01 |
| `GET /api/v1/oportunidades?etapa=&clienteId=&vendedorId=&page=` | `OPORTUNIDADE_VER` | paginado 20 |
| `GET /api/v1/oportunidades/{id}` | `OPORTUNIDADE_VER` | inclui histórico de etapas |
| `PUT /api/v1/oportunidades/{id}` | `OPORTUNIDADE_EDITAR` | título, valor, data prevista (RN-06) |
| `PATCH /api/v1/oportunidades/{id}/etapa` | `OPORTUNIDADE_MOVER_ETAPA` | RN-02..05 |
| `PATCH /api/v1/oportunidades/{id}/reabrir` | `OPORTUNIDADE_REABRIR` | RN-05 |
| `GET /api/v1/funil` | `OPORTUNIDADE_VER` | RF19, seção 2.3 |
| `GET /api/v1/motivos-perda` | `OPORTUNIDADE_VER` | lista fixa do seed |

### 2.1 Criação (RF07)

```json
{ "idCliente": 7, "titulo": "Plano mensal", "valorEstimado": 47000.00,
  "dataPrevista": "2026-10-15", "idVendedor": 3 }
```

`idVendedor` opcional (default: autenticado; GERENTE/ADMIN podem indicar). Valida cliente
via `ClienteConsultaService.buscarAtivo()` (SPEC-02). Nasce em `PROSPECCAO`.

### 2.2 Mover etapa (RF09) — coração do módulo

```json
{ "novaEtapa": "PERDIDA", "idMotivoPerda": 1 }
```

Respostas: 200 com a oportunidade atualizada · 409 `CONFLITO` transição inválida (mensagem
diz qual regra barrou) · 422 fechar como PERDIDA sem motivo (RF29).

### 2.3 `GET /api/v1/funil` (RF19)

Uma coluna por etapa aberta + agregado de fechadas, sempre com contagem e soma:

```json
{ "colunas": [
    { "etapa": "PROSPECCAO", "quantidade": 5, "valorTotal": 215000.00, "oportunidades": [ ... ] },
    { "etapa": "CONTATO",    "quantidade": 3, "valorTotal": 83000.00,  "oportunidades": [ ... ] },
    { "etapa": "PROPOSTA",   "quantidade": 2, "valorTotal": 103000.00, "oportunidades": [ ... ] },
    { "etapa": "FECHADO",    "quantidade": 4, "valorTotal": 178000.00, "oportunidades": [ ... ] }
  ],
  "valorEmNegociacao": 401000.00 }
```

`valorEmNegociacao` = soma das etapas abertas (alimenta o dashboard da SPEC-04 via
`OportunidadeConsultaService`). Cada card: id, título, cliente (nome), valor, vendedor,
`dataPrevista`, e para fechadas `resultado` + `motivoPerda`.

## 3. Regras de negócio

- **RN-01** `idCliente`, `titulo`, `valorEstimado` obrigatórios; valor > 0; cliente não excluído (RF07).
- **RN-02** Transições permitidas: entre PROSPECCAO ↔ CONTATO ↔ PROPOSTA em qualquer direção (RF09 permite retroceder), e de qualquer etapa aberta para GANHA ou PERDIDA.
- **RN-03** Fechamento: mover para GANHA/PERDIDA grava `fechadaEm=agora`; PERDIDA exige `idMotivoPerda` da lista fixa (RF29); GANHA zera motivo.
- **RN-04** Oportunidade fechada é imutável: não move etapa, não edita campos → 409 (RF09).
- **RN-05** Reabertura explícita (RF09): `PATCH /reabrir` → volta para `PROPOSTA`, limpa `fechadaEm` e `motivoPerda`, registra no histórico. Reabrir uma aberta → 409.
- **RN-06** Toda mudança de etapa (incluindo fechamento e reabertura) gera exatamente 1 linha em `historico_etapa` com `etapaAnterior`, `etapaNova`, usuário e momento.
- **RN-07** Mover para a etapa em que já está → 409 (evita histórico poluído).

## 4. Pattern A · State: transições de etapa

A regra de "o que pode a partir daqui" fica no estado, não num if-else gigante no service:

```java
public enum EtapaOportunidade {
    PROSPECCAO { Set<EtapaOportunidade> proximas() { return Set.of(CONTATO, PROPOSTA, GANHA, PERDIDA); } },
    CONTATO    { Set<EtapaOportunidade> proximas() { return Set.of(PROSPECCAO, PROPOSTA, GANHA, PERDIDA); } },
    PROPOSTA   { Set<EtapaOportunidade> proximas() { return Set.of(PROSPECCAO, CONTATO, GANHA, PERDIDA); } },
    GANHA      { Set<EtapaOportunidade> proximas() { return Set.of(); }   // só via reabrir()
               boolean fechada() { return true; } },
    PERDIDA    { Set<EtapaOportunidade> proximas() { return Set.of(); }
               boolean fechada() { return true; } };

    abstract Set<EtapaOportunidade> proximas();
    boolean fechada() { return false; }
    public boolean podeIrPara(EtapaOportunidade alvo) { return proximas().contains(alvo); }
}
```

(É o padrão State na forma idiomática Java: cada constante é uma subclasse anônima com o
próprio comportamento. Documentar no trabalho o mapeamento para o GoF: Context =
`Oportunidade`, State = `EtapaOportunidade`, ConcreteStates = as constantes.)

## 5. Pattern B · Observer (publicador)

O service publica; quem ouve é problema de quem ouve (SPEC-02 promove cliente, o próprio
módulo grava histórico):

```java
// dentro de OportunidadeService.moverEtapa(...), na mesma transação:
publisher.publishEvent(new OportunidadeEtapaAlteradaEvent(id, idUsuario, anterior, nova, agora));
if (nova.fechada()) {
    publisher.publishEvent(new OportunidadeFechadaEvent(id, idCliente, idVendedor, nova,
        idMotivoPerda, valorEstimado, agora));
}
```

O listener de `historico_etapa` deste módulo usa fase `BEFORE_COMMIT` (o histórico deve
entrar na MESMA transação da mudança, RN-06). Payloads: SPEC-00 §6, não alterar.

Interface pública exposta para SPEC-04 (dashboard/relatórios/ranking):

```java
public interface OportunidadeConsultaService {
    Map<EtapaOportunidade, TotalEtapa> totaisPorEtapa(Optional<List<Integer>> vendedores);
    BigDecimal valorEmNegociacao(Optional<List<Integer>> vendedores);
    List<OportunidadeResumo> fechadasNoPeriodo(LocalDate inicio, LocalDate fim, Optional<List<Integer>> vendedores);
}
```

## 6. Checklist de auditoria (colar no PR)

- [ ] Retroceder PROPOSTA → CONTATO funciona e gera histórico (RF09)
- [ ] Fechar PERDIDA sem motivo → 422; com motivo fora da lista → 422 (RF29)
- [ ] Mover oportunidade GANHA → 409; editar fechada → 409 (RN-04)
- [ ] Reabrir fechada volta a PROPOSTA e permite mover de novo (RF09)
- [ ] Cada mudança = exatamente 1 linha no histórico, com anterior/nova/usuário (RN-06)
- [ ] `/funil` bate contagem e soma com os dados inseridos no teste (RF19)
- [ ] VENDEDOR só vê as próprias oportunidades no funil; GERENTE vê as da equipe (RF15)
- [ ] Evento `OportunidadeFechadaEvent` publicado com payload exato da SPEC-00 (teste com `@RecordApplicationEvents`)
- [ ] Criação para cliente excluído → 422 (usa `ClienteConsultaService`, não repository)

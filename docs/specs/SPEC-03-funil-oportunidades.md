# SPEC-03 · Funil de Oportunidades

**Responsável:** ______
**Requisitos:** RF07, RF08, RF09, RF19, RF29
**Patterns desta spec:** o lado publicador do Observer (`OportunidadeFechadaEvent`) e a filha `SeedMotivosPerda` do Template Method `SeedBase` (SPEC-01 §7)

## 0. Ajuste inicial (SPEC-00 §3)

Primeiro PR: `EtapaOportunidade` → `PROSPECCAO, CONTATO, PROPOSTA, GANHA, PERDIDA` (RF08:
as colunas do funil são Prospecção, Contato, Proposta e Fechado; "Fechado" vira GANHA ou PERDIDA).

## 1. Entidades

`Oportunidade`, `HistoricoEtapa`, `MotivoPerda` (já criadas) + seed dos motivos (RF29):
`preço`, `concorrente`, `sem verba`, `sem resposta`.

## 2. Endpoints

| Endpoint | Permissão | Regra |
|----------|-----------|-------|
| `POST /api/v1/oportunidades` | OPORTUNIDADE_CRIAR | RN-01 |
| `GET /api/v1/oportunidades?etapa=&clienteId=&page=` | OPORTUNIDADE_VER | escopo de carteira |
| `GET /api/v1/oportunidades/{id}` | OPORTUNIDADE_VER | inclui o histórico de etapas |
| `PUT /api/v1/oportunidades/{id}` | OPORTUNIDADE_EDITAR | título, valor, data prevista; fechada → 409 |
| `PATCH /api/v1/oportunidades/{id}/etapa` | OPORTUNIDADE_MOVER | §2.2 |
| `PATCH /api/v1/oportunidades/{id}/reabrir` | OPORTUNIDADE_REABRIR | RN-05 |
| `GET /api/v1/funil` | OPORTUNIDADE_VER | §2.3 (RF19) |
| `GET /api/v1/motivos-perda` | OPORTUNIDADE_VER | lista fixa do seed |

### 2.1 Criação (RF07)

```json
{ "idCliente": 7, "titulo": "Plano mensal", "valorEstimado": 47000.00, "dataPrevista": "2026-10-15" }
```

Vendedor = usuário logado (GERENTE/ADMIN podem indicar `"idVendedor"`). Valida o cliente
chamando `ClienteService.buscarAtivo(id)` (SPEC-02). Nasce em `PROSPECCAO`.

### 2.2 Mover etapa (RF09)

`{ "novaEtapa": "PERDIDA", "idMotivoPerda": 1 }` → 200 com a oportunidade atualizada.
409 transição inválida · 422 PERDIDA sem motivo (RF29).

### 2.3 `GET /api/v1/funil` (RF19)

Cada etapa com contagem e soma, mais o total em negociação:

```json
{ "colunas": [
    { "etapa": "PROSPECCAO", "quantidade": 5, "valorTotal": 215000.00, "oportunidades": [ ... ] },
    { "etapa": "CONTATO",    "quantidade": 3, "valorTotal": 83000.00,  "oportunidades": [ ... ] },
    { "etapa": "PROPOSTA",   "quantidade": 2, "valorTotal": 103000.00, "oportunidades": [ ... ] },
    { "etapa": "FECHADO",    "quantidade": 4, "valorTotal": 178000.00, "oportunidades": [ ... ] }
  ], "valorEmNegociacao": 401000.00 }
```

Card: id, título, nome do cliente, valor, vendedor, `dataPrevista`; nas fechadas, também
`resultado` e `motivoPerda`.

## 3. Regras de negócio

- **RN-01** `idCliente`, `titulo`, `valorEstimado` obrigatórios; valor > 0; cliente ativo (RF07).
- **RN-02** Transições: entre PROSPECCAO ↔ CONTATO ↔ PROPOSTA vale qualquer direção (RF09 permite retroceder); de qualquer aberta pode ir para GANHA ou PERDIDA. Validação num método simples do enum:

```java
public boolean podeIrPara(EtapaOportunidade alvo) {
    if (this.fechada()) return false;              // GANHA/PERDIDA não movem (RN-04)
    return alvo != this;                           // abertas transitam livremente
}
public boolean fechada() { return this == GANHA || this == PERDIDA; }
```

- **RN-03** Fechar grava `fechadaEm = agora`; PERDIDA exige motivo da lista (RF29); GANHA limpa motivo.
- **RN-04** Fechada é imutável: mover ou editar → 409 (RF09).
- **RN-05** Reabrir (RF09): volta para `PROPOSTA`, limpa `fechadaEm` e motivo, registra no histórico. Reabrir uma aberta → 409.
- **RN-06** Toda mudança de etapa (mover, fechar, reabrir) grava 1 linha em `historico_etapa` (anterior, nova, usuário, momento), direto no service, na mesma transação.
- **RN-07** Mover para a etapa atual → 409.

## 4. Pattern · Observer (lado publicador)

No `OportunidadeService.moverEtapa(...)`, depois de salvar:

```java
if (novaEtapa.fechada()) {
    publisher.publishEvent(new OportunidadeFechadaEvent(
        opp.getIdOportunidade(), opp.getCliente().getIdCliente(),
        opp.getVendedor().getIdUsuario(), novaEtapa,
        opp.getValorEstimado(), opp.getFechadaEm()));
}
```

Para a apresentação: este service não importa nada do módulo de clientes. Quem reage ao
evento (SPEC-02 §5) é problema de quem escuta. Payload fixo na SPEC-00 §5.

## 5. Seed (Template Method)

`SeedMotivosPerda extends SeedBase` (SPEC-01 §7): `jaExecutou()` = tabela não vazia;
`criarDados()` insere os 4 motivos do RF29. É a segunda filha que completa a demonstração
do Template Method nº 1.

## 6. Métodos públicos para a SPEC-04

No `OportunidadeService` (sem interface extra, métodos públicos bastam):

```java
Map<EtapaOportunidade, TotalEtapa> totaisPorEtapa(List<Integer> vendedoresOuNull);
BigDecimal valorEmNegociacao(List<Integer> vendedoresOuNull);
List<Oportunidade> fechadasNoPeriodo(LocalDate inicio, LocalDate fim, List<Integer> vendedoresOuNull);
List<Oportunidade> listarDoCliente(Integer idCliente);   // usado no histórico da SPEC-02
```

## 7. Checklist de auditoria (colar no PR)

- [ ] Retroceder PROPOSTA → CONTATO funciona e gera histórico (RF09)
- [ ] PERDIDA sem motivo → 422; motivo fora da lista → 422 (RF29)
- [ ] Mover ou editar GANHA → 409 (RN-04)
- [ ] Reabrir volta a PROPOSTA e permite mover de novo (RF09)
- [ ] Cada mudança = exatamente 1 linha no histórico (RN-06)
- [ ] `/funil` bate contagem e soma com os dados do teste (RF19)
- [ ] VENDEDOR vê só o próprio funil; GERENTE vê a equipe (RF15)
- [ ] Evento publicado só no fechamento, com o payload da SPEC-00 (`@RecordApplicationEvents`)
- [ ] Criar para cliente excluído → 422 (via `ClienteService.buscarAtivo`)
- [ ] Seed dos motivos rodando 2x não duplica

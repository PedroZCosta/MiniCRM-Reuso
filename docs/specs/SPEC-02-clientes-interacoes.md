# SPEC-02 · Clientes e Interações

**Responsável:** Lincoln Brunkow Neto 
**Requisitos:** RF03, RF04, RF05, RF06, RF16, RF26, RF30 · UC03, UC06, UC08, UC09
**Design patterns:** Specification/Composite (busca dinâmica), Observer (lado consumidor: status automático do cliente)

## 0. Ajustes iniciais (SPEC-00 §3)

Primeiro PR deste módulo: `StatusCliente` → `PROSPECT, ATIVO, INATIVO` (remover LEAD) e
`TipoInteracao` → `LIGACAO, EMAIL, REUNIAO` (remover WHATSAPP, VISITA).

## 1. Entidades sob responsabilidade deste módulo

`Cliente`, `Interacao` (já criadas). Não alterar `Oportunidade`/`Tarefa` (donos: SPEC-03/04).

## 2. Endpoints

Todos autenticados; visibilidade via `EscopoCarteira` (SPEC-01 §7).

### 2.1 `GET /api/v1/clientes` (RF04, RF18, UC06)

Query params: `busca` (nome OU empresa, contains, case-insensitive), `status`
(PROSPECT|ATIVO|INATIVO), `vendedorId`, `page`. Permissão `CLIENTE_VER`.

```json
{ "conteudo": [ {
    "idCliente": 7, "nome": "Construtora Horizonte", "email": "contato@horizonte.com",
    "telefone": "41999990000", "empresa": "Horizonte Ltda", "status": "ATIVO",
    "vendedor": { "idUsuario": 3, "nome": "Juliana Torres", "ativo": true },
    "criadoEm": "2026-09-01T10:00:00"
} ], "pagina": 0, "tamanho": 20, "totalPaginas": 1, "totalRegistros": 4 }
```

Regra de exibição: clientes `excluido=true` **não** aparecem em nenhuma listagem padrão
(RF03), mas GERENTE/ADMIN podem consultá-los com `?incluirExcluidos=true` (RF33: dados
preservados e visíveis para gestão). Quando o vendedor responsável está desativado, o
objeto `vendedor` vem com `"ativo": false` para o front exibir "sem responsável ativo".

### 2.2 Demais endpoints

| Endpoint | Permissão | Observação |
|----------|-----------|------------|
| `POST /api/v1/clientes` | `CLIENTE_CRIAR` | RN-01/02; qualquer perfil cadastra (RF03) |
| `GET /api/v1/clientes/{id}` | `CLIENTE_VER` | 404 se fora do escopo de carteira |
| `PUT /api/v1/clientes/{id}` | `CLIENTE_EDITAR` | nome, e-mail, telefone, empresa |
| `DELETE /api/v1/clientes/{id}` | `CLIENTE_EXCLUIR` | exclusão LÓGICA (RN-03), 204 |
| `PATCH /api/v1/clientes/{id}/transferir` | `CLIENTE_TRANSFERIR` | `{ "novoVendedorId": 5 }` (RF26); só GERENTE/ADMIN |
| `POST /api/v1/clientes/{id}/interacoes` | `INTERACAO_CRIAR` | RN-05 |
| `GET /api/v1/clientes/{id}/interacoes?page=` | `INTERACAO_VER` | ordenado por `dataInteracao` desc |
| `GET /api/v1/clientes/{id}/historico` | `CLIENTE_VER` | RF06 + RF16, seção 2.3 |

Request de criação:

```json
{ "nome": "...", "email": "...", "telefone": "...", "empresa": "...", "idVendedor": 3 }
```

`idVendedor` opcional para VENDEDOR (assume ele mesmo); GERENTE/ADMIN podem indicar outro.
Request de interação: `{ "tipo": "LIGACAO", "dataInteracao": "2026-09-10T15:00:00", "observacoes": "..." }`
(usuário da interação = autenticado; `dataInteracao` default = agora).

### 2.3 `GET /api/v1/clientes/{id}/historico` (RF06, RF16, UC08)

Linha do tempo unificada, mais recente primeiro, paginada:

```json
{ "conteudo": [
  { "tipo": "INTERACAO",    "data": "2026-09-10T15:00:00", "titulo": "LIGACAO",
    "descricao": "Follow-up da proposta", "autor": "Juliana Torres" },
  { "tipo": "OPORTUNIDADE", "data": "2026-09-08T09:00:00", "titulo": "Plano mensal",
    "descricao": "PROPOSTA · R$ 47.000,00", "autor": "Juliana Torres" },
  { "tipo": "TAREFA",       "data": "2026-09-05T08:00:00", "titulo": "Enviar contrato",
    "descricao": "vence 2026-09-12 · pendente", "autor": "Juliana Torres" }
], "pagina": 0, "tamanho": 20, "totalPaginas": 1, "totalRegistros": 3 }
```

Oportunidades vêm de `OportunidadeConsultaService` (SPEC-03) e tarefas de
`TarefaConsultaService` (SPEC-04). **Proibido** injetar `OportunidadeRepository`/
`TarefaRepository` aqui: é o contrato do README.

Interface pública que ESTE módulo expõe (para SPEC-03/04):

```java
public interface ClienteConsultaService {
    /** 404 se não existir; 422 se excluido=true (não se cria nada para cliente excluído) */
    ClienteResumo buscarAtivo(Integer idCliente);
}
```

## 3. Regras de negócio

- **RN-01** `nome` e `email` obrigatórios (RF03); demais campos opcionais. E-mail com formato válido.
- **RN-02** Todo cliente nasce `status=PROSPECT` (RF30) com exatamente um vendedor responsável (RF26). Vendedor responsável precisa estar ativo na criação/transferência.
- **RN-03** Exclusão lógica (RF03): `excluido=true` E `status=INATIVO`. Some das listagens, histórico preservado. Qualquer perfil com acesso ao cliente pode excluir. Não há endpoint de "desfazer" nesta fase.
- **RN-04** Transferência (RF26): troca `vendedor`, registra no histórico (gera `Interacao` sintética? NÃO: apenas `atualizadoEm`; o funil não muda). Vendedor não transfere carteira.
- **RN-05** Interação exige cliente não excluído; `tipo` dentro do enum; `observacoes` ≤ 65k (TEXT).
- **RN-06** Status automático (RF30): na PRIMEIRA `OportunidadeFechadaEvent` com `resultado=GANHA`, cliente PROSPECT → ATIVO. Cliente não volta a PROSPECT nunca; INATIVO só via exclusão lógica (ou futura regra de inatividade, fora de escopo).

## 4. Pattern A · Specification (com Composite): busca dinâmica do RF04

Filtros combináveis sem if-else de concatenação de JPQL. Usar a
`org.springframework.data.jpa.domain.Specification` (implementação canônica do padrão,
composta com `and`/`or`, que é a parte Composite):

```java
public final class ClienteSpecs {
    public static Specification<Cliente> naoExcluido() { /* excluido = false */ }
    public static Specification<Cliente> buscaLivre(String termo) { /* nome LIKE ou empresa LIKE */ }
    public static Specification<Cliente> comStatus(StatusCliente s) { ... }
    public static Specification<Cliente> doVendedor(Integer id) { ... }
    public static Specification<Cliente> dentroDoEscopo(List<Integer> vendedores) { ... }
}
// service:
var spec = ClienteSpecs.naoExcluido()
        .and(busca != null ? ClienteSpecs.buscaLivre(busca) : null)
        .and(status != null ? ClienteSpecs.comStatus(status) : null)
        .and(escopo.vendedoresVisiveis().map(ClienteSpecs::dentroDoEscopo).orElse(null));
clienteRepository.findAll(spec, PageRequest.of(page, 20, Sort.by("nome")));
```

`ClienteRepository` passa a estender `JpaSpecificationExecutor<Cliente>`.

## 5. Pattern B · Observer (consumidor): RF30

```java
@Component
public class ClienteStatusListener {
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void aoFecharOportunidade(OportunidadeFechadaEvent evento) {
        if (evento.resultado() == EtapaOportunidade.GANHA) {
            clienteService.promoverParaAtivoSeProspect(evento.idCliente());
        }
    }
}
```

O módulo do funil (SPEC-03) não sabe que este listener existe: acoplamento zero.
Idem para `UsuarioDesativadoEvent` (nada a fazer além de logging nesta fase, pois a
sinalização "sem responsável ativo" é derivada de `vendedor.ativo` na leitura).

## 6. Checklist de auditoria (colar no PR)

- [ ] Busca "hori" encontra por nome E por empresa, ignorando caixa (RF04)
- [ ] Filtros combinados busca+status+vendedor funcionam juntos e paginados em 20 (RF18)
- [ ] VENDEDOR não enxerga cliente de outro vendedor: listagem filtra, GET direto → 404
- [ ] DELETE não apaga linha: `excluido=true`, some da listagem, histórico continua acessível via `?incluirExcluidos=true` (RF03/RF33)
- [ ] Interação em cliente excluído → 422
- [ ] Evento GANHA em cliente PROSPECT → vira ATIVO; segunda GANHA não muda nada; PERDIDA não muda nada (RF30)
- [ ] Transferência por VENDEDOR → 403; para vendedor desativado → 422 (RF26)
- [ ] Histórico unificado ordena interações, oportunidades e tarefas por data desc (RF06/RF16)

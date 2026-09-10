# SPEC-02 · Clientes e Interações

**Responsável:** ______
**Requisitos:** RF03, RF04, RF05, RF06, RF16, RF26, RF30
**Patterns desta spec:** Singleton nº 2 (`ClienteService`) e o lado ouvinte do Observer (RF30)

## 0. Ajuste inicial (SPEC-00 §3)

Primeiro PR: `StatusCliente` → `PROSPECT, ATIVO, INATIVO` e `TipoInteracao` → `LIGACAO, EMAIL, REUNIAO`.

## 1. Entidades

`Cliente` e `Interacao` (já criadas).

## 2. Endpoints

Todos autenticados; listagens filtradas pelo `EscopoCarteira` (SPEC-01 §6).

| Endpoint | Permissão | Regra |
|----------|-----------|-------|
| `GET /api/v1/clientes?busca=&status=&vendedorId=&page=` | CLIENTE_VER | §2.1 |
| `POST /api/v1/clientes` | CLIENTE_CRIAR | RN-01, RN-02 |
| `GET /api/v1/clientes/{id}` | CLIENTE_VER | 404 se fora do escopo |
| `PUT /api/v1/clientes/{id}` | CLIENTE_EDITAR | nome, e-mail, telefone, empresa |
| `DELETE /api/v1/clientes/{id}` | CLIENTE_EXCLUIR | exclusão LÓGICA (RN-03) → 204 |
| `PATCH /api/v1/clientes/{id}/transferir` `{novoVendedorId}` | CLIENTE_TRANSFERIR | RF26; só GERENTE/ADMIN |
| `POST /api/v1/clientes/{id}/interacoes` | INTERACAO_CRIAR | RN-05 |
| `GET /api/v1/clientes/{id}/interacoes?page=` | INTERACAO_VER | `dataInteracao` desc |
| `GET /api/v1/clientes/{id}/historico?page=` | CLIENTE_VER | §2.2 (RF06 + RF16) |

Criação: `{ "nome", "email", "telefone", "empresa", "idVendedor" }`. `idVendedor` opcional
para VENDEDOR (assume ele mesmo); GERENTE/ADMIN podem indicar outro.
Interação: `{ "tipo": "LIGACAO", "dataInteracao": "2026-09-10T15:00:00", "observacoes": "..." }`
(autor = usuário logado; data default = agora).

### 2.1 Busca e filtros (RF04) — sem firula

Um único método `@Query` com parâmetros opcionais resolve tudo, e um júnior lê sem susto:

```java
@Query("""
   SELECT c FROM Cliente c
   WHERE c.excluido = false
     AND (:busca IS NULL OR LOWER(c.nome) LIKE LOWER(CONCAT('%', :busca, '%'))
                         OR LOWER(c.empresa) LIKE LOWER(CONCAT('%', :busca, '%')))
     AND (:status IS NULL OR c.status = :status)
     AND (:vendedorId IS NULL OR c.vendedor.idUsuario = :vendedorId)
     AND (:vendedoresEscopo IS NULL OR c.vendedor.idUsuario IN :vendedoresEscopo)
   """)
Page<Cliente> buscar(String busca, StatusCliente status, Integer vendedorId,
                     List<Integer> vendedoresEscopo, Pageable pageable);
```

Clientes excluídos não aparecem em listagem nenhuma (RF03); GERENTE/ADMIN acessam o
detalhe de um excluído por id (RF33: histórico preservado). Se o vendedor responsável
estiver desativado, o response traz `"vendedor": { ..., "ativo": false }` para o front
mostrar "sem responsável ativo".

### 2.2 `GET /clientes/{id}/historico` (RF06, RF16)

Linha do tempo com interações, oportunidades e tarefas do cliente, mais recente primeiro:

```json
{ "conteudo": [
  { "tipo": "INTERACAO",    "data": "2026-09-10T15:00:00", "titulo": "LIGACAO",  "descricao": "Follow-up", "autor": "Juliana" },
  { "tipo": "OPORTUNIDADE", "data": "2026-09-08T09:00:00", "titulo": "Plano mensal", "descricao": "PROPOSTA · R$ 47.000,00", "autor": "Juliana" },
  { "tipo": "TAREFA",       "data": "2026-09-05T08:00:00", "titulo": "Enviar contrato", "descricao": "vence 2026-09-12 · pendente", "autor": "Juliana" }
], "pagina": 0, "tamanho": 20, "totalPaginas": 1, "totalRegistros": 3 }
```

Oportunidades e tarefas vêm dos services públicos das SPEC-03/04
(`OportunidadeService.listarDoCliente(id)`, `TarefaService.listarDoCliente(id)`).
Repository alheio aqui = PR reprovado.

Método público que ESTE módulo oferece aos outros:

```java
/** lança RecursoNaoEncontradoException se não existe; RegraNegocioException se excluído */
public Cliente buscarAtivo(Integer idCliente) { ... }
```

## 3. Regras de negócio

- **RN-01** `nome` e `email` obrigatórios, e-mail com formato válido (RF03).
- **RN-02** Todo cliente nasce `PROSPECT` (RF30), com exatamente um vendedor responsável ativo (RF26).
- **RN-03** Excluir = `excluido=true` + `status=INATIVO`. Some das listas, dados ficam (RF03, RF33).
- **RN-04** Transferir (RF26): só muda o `vendedor`; destino precisa estar ativo → senão 422.
- **RN-05** Interação só em cliente não excluído → senão 422.
- **RN-06** RF30: na primeira oportunidade GANHA, cliente PROSPECT vira ATIVO (via listener, §5). ATIVO nunca volta a PROSPECT.

## 4. Pattern · Singleton nº 2: `ClienteService`

Mesma explicação do `EscopoCarteira` (SPEC-01 §6): `@Service` = uma instância única
gerenciada pelo container, compartilhada por `ClienteController`, `OportunidadeService` e
`DashboardFacade`. Segundo exemplo exigido pelo professor; provar de novo com `assertSame`.

## 5. Pattern · Observer (lado ouvinte): RF30

```java
@Component
public class ClienteStatusListener {
    @EventListener
    public void aoFecharOportunidade(OportunidadeFechadaEvent evento) {
        if (evento.resultado() == EtapaOportunidade.GANHA) {
            clienteService.promoverParaAtivoSeProspect(evento.idCliente());
        }
    }
}
```

Ponto para a apresentação: o funil (SPEC-03) publica o evento sem saber que este listener
existe. Amanhã um módulo de e-mail pode ouvir o mesmo evento sem tocar no funil.

## 6. Checklist de auditoria (colar no PR)

- [ ] Busca "hori" encontra por nome E por empresa, ignorando maiúsculas (RF04)
- [ ] Filtros combinados funcionam juntos, paginados em 20 (RF18)
- [ ] VENDEDOR não vê cliente alheio: some da lista e GET direto → 404
- [ ] DELETE não apaga a linha: `excluido=true` e fora das listagens (RF03/RF33)
- [ ] Interação em cliente excluído → 422
- [ ] Evento GANHA: PROSPECT vira ATIVO; segunda GANHA não muda nada; PERDIDA não muda nada (RF30)
- [ ] Transferência por VENDEDOR → 403; para vendedor desativado → 422 (RF26)
- [ ] Histórico mistura os 3 tipos ordenados por data desc (RF06/RF16)
- [ ] `assertSame` provando o singleton do `ClienteService`

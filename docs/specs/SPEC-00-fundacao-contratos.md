# SPEC-00 · Fundação e Contratos Compartilhados

**Responsável:** todos (leitura obrigatória antes de qualquer linha de código)
**Requisitos:** RF18 (paginação), RNF03 (segurança), RNF05 (versionamento)
**Status das entidades:** já implementadas em `model/` (commit `c7faa81`), com ajustes de enum pendentes (seção 3).

Esta spec não é um módulo: é o conjunto de decisões que valem para os 4 módulos.
Qualquer mudança aqui exige concordância dos 4 integrantes via PR.

---

## 1. Estrutura de pacotes

```
com.miniCRM.miniCRM
├── config/          SecurityConfig, JacksonConfig, SchedulingConfig
├── security/        JWT, filtro, @RequiresPermission, PermissionAspect, EscopoCarteira (SPEC-01)
├── model/           entidades JPA (compartilhadas, já criadas)
│   └── enums/
├── repository/      interfaces Spring Data (cada spec adiciona SEUS métodos de consulta)
├── service/         regras de negócio (1+ classes por módulo)
├── controller/      endpoints REST (1+ classes por módulo)
├── dto/             requests/responses por módulo: dto/auth, dto/cliente, dto/funil, dto/tarefa...
├── event/           eventos de domínio (payloads desta spec, seção 6)
└── exception/       exceções de negócio + handler global (seção 5)
```

Regra de ouro: **controller → service → repository, sempre nessa direção.** Controller não
toca repository. Service de um módulo não injeta repository de outro módulo (usa as
interfaces públicas ou eventos do mapa de integração no README).

## 2. Convenções REST

- Base path: `/api/v1`. Recursos no plural, em português, kebab-case: `/api/v1/motivos-perda`.
- Datas: ISO-8601 (`2026-09-10` para DATE, `2026-09-10T14:30:00` para DATETIME). Jackson já serializa `LocalDate`/`LocalDateTime` assim; ninguém formata data na mão.
- Verbos: POST cria (201 + body criado), GET lê (200), PUT substitui (200), PATCH altera estado pontual (200 ou 204), DELETE lógico (204).
- Autenticação: header `Authorization: Bearer <jwt>` em tudo, exceto os endpoints públicos listados na SPEC-01.

### 2.1 Paginação (RF18, obrigatória em TODA listagem)

Tamanho fixo de página: **20**. Query params: `?page=0` (base zero). O cliente não escolhe o tamanho.
Resposta sempre neste envelope (classe `PageResponse<T>` em `dto/comum`):

```json
{
  "conteudo": [],
  "pagina": 0,
  "tamanho": 20,
  "totalPaginas": 3,
  "totalRegistros": 47
}
```

Busca e filtros preservados ao trocar de página = responsabilidade do front; o backend
garante que os mesmos query params retornam o mesmo resultado.

## 3. Enums canônicos (ajuste obrigatório antes de qualquer feature)

Os enums criados no commit inicial divergem do PDF. Corrigir assim, cada dono no primeiro PR:

| Enum | Valor canônico (PDF) | Dono do ajuste |
|------|----------------------|----------------|
| `PerfilUsuario` | `ADMIN, GERENTE, VENDEDOR` (já correto; seed RBAC do Ativo 6 usa ADMIN) | SPEC-01 |
| `StatusCliente` | `PROSPECT, ATIVO, INATIVO` (remover LEAD; RF04/RF30) | SPEC-02 |
| `TipoInteracao` | `LIGACAO, EMAIL, REUNIAO` (remover WHATSAPP e VISITA; RF05) | SPEC-02 |
| `EtapaOportunidade` | `PROSPECCAO, CONTATO, PROPOSTA, GANHA, PERDIDA` (RF08: "Fechado" = GANHA ou PERDIDA) | SPEC-03 |
| `TipoNotificacao` | `D15, D1, HOJE, VENCIDA, REUNIAO` (diagrama de classes do PDF) | SPEC-04 |

Todos os enums persistem como `EnumType.STRING` (já configurado nas entidades).

## 4. Segurança transversal (RNF03)

- Senha: BCrypt via `PasswordEncoder`, nunca texto puro, nunca logada (os `@ToString` das entidades já excluem `senhaHash` e `codigoHash`).
- Toda rota fora da lista pública exige JWT válido; autorização fina via `@RequiresPermission` (SPEC-01).
- Visibilidade de dados: **nenhuma query de listagem sai sem passar pelo `EscopoCarteira`** (seção 7 da SPEC-01). VENDEDOR enxerga só a própria carteira; GERENTE, a dos seus vendedores; ADMIN, tudo. Vale para clientes, oportunidades, tarefas, dashboard e relatórios (RF15, RF22).

## 5. Erros padronizados

Handler global (`@RestControllerAdvice`, classe `exception/ApiExceptionHandler`) traduz
exceções para este envelope, sem stack trace vazando:

```json
{
  "timestamp": "2026-09-10T14:30:00",
  "status": 422,
  "erro": "REGRA_NEGOCIO",
  "mensagem": "Oportunidade fechada exige reabertura antes de mover de etapa",
  "detalhes": []
}
```

| HTTP | `erro` | Quando |
|------|--------|--------|
| 400 | `REQUISICAO_INVALIDA` | Bean Validation falhou (`detalhes` lista campo+motivo) |
| 401 | `NAO_AUTENTICADO` | JWT ausente/expirado/inválido |
| 403 | `SEM_PERMISSAO` | `@RequiresPermission` negou, ou registro fora do escopo de carteira |
| 404 | `NAO_ENCONTRADO` | id inexistente (ou fora do escopo, quando não se deve revelar existência) |
| 409 | `CONFLITO` | e-mail duplicado, transição de etapa inválida |
| 422 | `REGRA_NEGOCIO` | violação de regra (RN-xx das specs) |
| 423 | `BLOQUEADO` | login bloqueado por tentativas (RF01) |

Exceções de negócio: `RegraNegocioException(mensagem)`, `RecursoNaoEncontradoException`,
`SemPermissaoException`. Ninguém cria formato de erro próprio.

## 6. Eventos de domínio (contratos imutáveis)

Mecanismo: `ApplicationEventPublisher` do Spring (implementação do padrão **Observer**:
o publicador não conhece os ouvintes). Listeners que gravam banco usam
`@TransactionalEventListener(phase = AFTER_COMMIT)` quando o efeito deve ocorrer apenas
se a transação de origem confirmou. Payloads são `record`s no pacote `event/`:

```java
public record OportunidadeEtapaAlteradaEvent(
    Integer idOportunidade, Integer idUsuario,
    EtapaOportunidade etapaAnterior, EtapaOportunidade etapaNova,
    LocalDateTime ocorridoEm) {}

public record OportunidadeFechadaEvent(
    Integer idOportunidade, Integer idCliente, Integer idVendedor,
    EtapaOportunidade resultado,          // GANHA ou PERDIDA
    Short idMotivoPerda,                  // null quando GANHA
    BigDecimal valorEstimado, LocalDateTime fechadaEm) {}

public record TarefaVencendoEvent(
    Integer idTarefa, Integer idUsuario, String tituloTarefa,
    TipoNotificacao faixa, LocalDate dataVencimento) {}

public record UsuarioDesativadoEvent(Integer idUsuario, LocalDateTime desativadoEm) {}
```

## 7. Seeds (executam no start, idempotentes, pacote `config/seed`)

| Seed | Conteúdo | Dono |
|------|----------|------|
| Admin inicial (RF32) | usuário ADMIN com `trocarSenha=true`, credenciais via variável de ambiente | SPEC-01 |
| Permissões RBAC | tabela `permissao` + `perfil_permissao` (catálogo na SPEC-01 §6) | SPEC-01 |
| Motivos de perda (RF29) | `preço, concorrente, sem verba, sem resposta` | SPEC-03 |

## 8. Testes e definição de pronto (auditoria)

Uma spec só é dada como pronta quando:

1. `./mvnw verify` verde no CI local (sem pular testes).
2. Teste unitário para cada regra RN-xx da spec (JUnit 5 + Mockito, service isolado).
3. Teste de integração dos endpoints felizes + erros 403/404/422 (`@SpringBootTest` + MockMvc, banco H2 em memória no profile `test`).
4. Checklist de auditoria da spec preenchido no PR.
5. Nenhum acesso cruzado a repository de outro módulo (verificável por `grep` nos imports).

## 9. Configuração

- Profiles: `dev` (MySQL local, `ddl-auto=update`), `test` (H2, `create-drop`). Produção fica para a fase de deploy.
- Variáveis: `DB_HOST/DB_PORT/DB_NAME/DB_USER/DB_PASSWORD` (já no `application.properties`), `JWT_SECRET`, `JWT_EXPIRACAO_HORAS=8`, `SEED_ADMIN_EMAIL`, `SEED_ADMIN_SENHA`.
- Dependências novas permitidas nesta fase: `spring-boot-starter-validation`, `jjwt` (ou `spring-security-oauth2-jose`), `opencsv` (SPEC-04). Qualquer outra: discutir no grupo.

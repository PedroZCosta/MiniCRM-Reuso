# SPEC-00 · Fundação e Contratos Compartilhados

**Responsável:** todos (ler antes de qualquer código)
**Requisitos:** RF18 (paginação), RNF03 (segurança), RNF05 (versionamento)

Regras que valem para os 4 módulos. Mudança aqui só com os 4 de acordo, via PR.

## 1. Estrutura de pacotes

```
com.miniCRM.miniCRM
├── config/          SecurityConfig, seeds
├── security/        JWT (gerar/validar), filtro, EscopoCarteira
├── model/ (enums/)  entidades JPA (já criadas)
├── repository/      interfaces Spring Data
├── service/         regras de negócio
├── controller/      endpoints REST
├── dto/             requests e responses (nunca expor entidade direto no controller)
├── event/           OportunidadeFechadaEvent
└── exception/       exceções de negócio + handler global
```

Sentido único: **controller → service → repository.** Controller não usa repository.
Service usa service público de outro módulo quando precisar; repository alheio, nunca.

## 2. Convenções REST

- Base path `/api/v1`, recursos no plural em português: `/api/v1/clientes`, `/api/v1/motivos-perda`.
- Datas ISO-8601: `2026-09-10` (DATE) e `2026-09-10T14:30:00` (DATETIME). É o default do Jackson com `LocalDate`/`LocalDateTime`, ninguém formata na mão.
- POST cria (201), GET lê (200), PUT edita (200), PATCH muda estado pontual (200/204), DELETE lógico (204).
- Tudo autenticado com `Authorization: Bearer <token>`, exceto os 3 endpoints públicos da SPEC-01.

### 2.1 Paginação (RF18)

Página fixa de **20**. Param `?page=0` (base zero). Toda listagem responde no envelope
(`PageResponse<T>` em `dto/comum`, preenchido a partir do `Page` do Spring):

```json
{ "conteudo": [], "pagina": 0, "tamanho": 20, "totalPaginas": 3, "totalRegistros": 47 }
```

## 3. Enums canônicos (corrigir antes das features)

Os enums do commit inicial divergem do PDF. Cada dono corrige no seu primeiro PR:

| Enum | Valores corretos (PDF) | Dono |
|------|------------------------|------|
| `PerfilUsuario` | `ADMIN, GERENTE, VENDEDOR` (já está certo) | SPEC-01 |
| `StatusCliente` | `PROSPECT, ATIVO, INATIVO` (tirar LEAD) | SPEC-02 |
| `TipoInteracao` | `LIGACAO, EMAIL, REUNIAO` (tirar WHATSAPP, VISITA) | SPEC-02 |
| `EtapaOportunidade` | `PROSPECCAO, CONTATO, PROPOSTA, GANHA, PERDIDA` | SPEC-03 |
| `TipoNotificacao` | `D15, D1, HOJE, VENCIDA, REUNIAO` | SPEC-04 |

## 4. Erros padronizados

Um `@RestControllerAdvice` (`exception/ApiExceptionHandler`) traduz tudo para:

```json
{ "timestamp": "2026-09-10T14:30:00", "status": 422, "erro": "REGRA_NEGOCIO",
  "mensagem": "Oportunidade fechada precisa ser reaberta antes de mover", "detalhes": [] }
```

| HTTP | `erro` | Quando |
|------|--------|--------|
| 400 | `REQUISICAO_INVALIDA` | Bean Validation falhou (campos em `detalhes`) |
| 401 | `NAO_AUTENTICADO` | token ausente, inválido ou expirado |
| 403 | `SEM_PERMISSAO` | perfil sem a permissão |
| 404 | `NAO_ENCONTRADO` | id não existe OU está fora do escopo do usuário |
| 409 | `CONFLITO` | e-mail duplicado, transição de etapa inválida |
| 422 | `REGRA_NEGOCIO` | regra RN-xx violada |
| 423 | `BLOQUEADO` | login bloqueado por tentativas |

Três exceções bastam: `RegraNegocioException`, `RecursoNaoEncontradoException`,
`ConflitoException`. Não criar formato de erro próprio.

## 5. O único evento do sistema

Padrão **Observer** via `ApplicationEventPublisher` do Spring, listener com
`@EventListener` simples (síncrono, mesma transação). Payload é um record em `event/`:

```java
public record OportunidadeFechadaEvent(
    Integer idOportunidade, Integer idCliente, Integer idVendedor,
    EtapaOportunidade resultado,   // GANHA ou PERDIDA
    BigDecimal valorEstimado, LocalDateTime fechadaEm) {}
```

SPEC-03 publica; SPEC-02 escuta (RF30). Só isso. Precisou de outro evento? Discutir no grupo primeiro.

## 6. Segurança transversal (RNF03)

- Senha sempre BCrypt (`PasswordEncoder`), nunca em texto puro, nunca em log ou response.
- Permissões: enum `Permissao` + mapa fixo por perfil (SPEC-01 §5). Controllers usam `@PreAuthorize("hasAuthority('CLIENTE_CRIAR')")`, anotação pronta do Spring Security.
- Visibilidade: toda listagem passa pelo `EscopoCarteira` (SPEC-01 §6). VENDEDOR vê só o que é dele; GERENTE, o da equipe; ADMIN, tudo (RF15, RF22).

## 7. Seeds

Classes que rodam no start (via `CommandLineRunner`), idempotentes, usando o
**Template Method** `SeedBase` (SPEC-01 §7):

| Seed | Conteúdo | Dono |
|------|----------|------|
| `SeedAdmin` (RF32) | admin inicial com `trocarSenha=true`, credenciais em variável de ambiente | SPEC-01 |
| `SeedMotivosPerda` (RF29) | `preço`, `concorrente`, `sem verba`, `sem resposta` | SPEC-03 |

## 8. Testes e definição de pronto

1. `./mvnw verify` verde, sem pular teste.
2. Teste unitário por regra RN-xx (JUnit 5 + Mockito).
3. Teste de integração dos endpoints: caminho feliz + 403 + 404 + 422 (MockMvc, H2 no profile `test`).
4. Checklist de auditoria da spec preenchido no PR.

## 9. Configuração

- Profiles: `dev` (MySQL, `ddl-auto=update`) e `test` (H2, `create-drop`).
- Variáveis: `DB_*` (já existem), `JWT_SECRET`, `SEED_ADMIN_EMAIL`, `SEED_ADMIN_SENHA`.
- Dependências novas permitidas: `spring-boot-starter-validation`, `jjwt`, `opencsv`, H2 (test). Fora isso, combinar no grupo.

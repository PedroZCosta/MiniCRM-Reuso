# SPEC-01 · Autenticação, Usuários e RBAC

**Responsável:** ______
**Requisitos:** RF01, RF02, RF14, RF17, RF20, RF25, RF27, RF32, RF33 · RNF03 · UC01, UC02, UC05, UC14, UC15
**Base:** Ativo 6 do PDF (Módulo de Segurança RBAC) adaptado ao nosso modelo (DEC-01 abaixo)
**Design patterns:** Proxy (autorização via AOP), Adapter (envio de e-mail). De brinde, documentar: Chain of Responsibility (filter chain do Spring Security) e Singleton (beans).

## DEC-01 · Conciliação entre o MER e o módulo RBAC

O PDF traz duas fontes: o MER (tabela `usuario` com coluna `perfil` ENUM, já implementado) e o
Ativo 6 (users N:N roles N:N permissions). Decisão: **perfil único por usuário** (coluna
`usuario.perfil`, coerente com o MER e RF02) + **permissões como dados** no espírito do Ativo 6:

```
permissao        (id_permissao TINYINT PK, nome VARCHAR(60) UNIQUE)   -- ex.: CLIENTE_CRIAR
perfil_permissao (perfil VARCHAR(20), id_permissao TINYINT)           -- PK composta
```

Isso preserva o mecanismo de reuso do Ativo 6 (mudar quem pode o quê = mexer em dados, sem
recompilar; VP-1..VP-3) sem contrariar o modelo de dados entregue. Criar as entidades
`Permissao` e `PerfilPermissao` neste módulo.

## 1. Entidades sob responsabilidade deste módulo

`Usuario`, `TokenRecuperacao` (já criadas) + `Permissao`, `PerfilPermissao` (novas).

## 2. Endpoints

Públicos (sem JWT): `POST /api/v1/auth/login`, `POST /api/v1/auth/recuperar-senha`, `POST /api/v1/auth/redefinir-senha`.

### 2.1 `POST /api/v1/auth/login` (RF01, UC01)

Request `{ "email": "...", "senha": "..." }` → 200:

```json
{
  "token": "eyJ...",
  "expiraEm": "2026-09-10T22:30:00",
  "usuario": { "idUsuario": 1, "nome": "...", "perfil": "GERENTE", "trocarSenha": false }
}
```

Erros: 401 credencial inválida (mensagem genérica, não revelar qual campo errou) · 423 bloqueado (RN-03) · 422 usuário desativado.
Claims do JWT: `sub` = idUsuario, `perfil`, `permissoes` (array), `exp` = agora + 8h (RF01: sessão expira em 8h).

### 2.2 Recuperação de senha (RF17, UC02, UC05)

- `POST /auth/recuperar-senha` `{ "email" }` → **sempre 204** (não vaza se o e-mail existe). Se existir: gera código de 6 dígitos, grava `token_recuperacao` com `codigoHash` = BCrypt(código), `expiraEm` = agora + 1h, `usado=false`, e envia por `EmailService`.
- `POST /auth/redefinir-senha` `{ "email", "codigo", "novaSenha" }` → 204. Erros: 422 código inválido/expirado/já usado. Marca `usado=true`, zera `tentativasLogin`, seta `trocarSenha=false`.

### 2.3 `PUT /api/v1/auth/trocar-senha` (autenticado)

`{ "senhaAtual", "novaSenha" }` → 204. Obrigatória quando `trocarSenha=true` (RF32): o filtro barra qualquer outra rota com 403 `SENHA_PROVISORIA` até a troca.

### 2.4 Gestão de usuários (RF14, RF20, RF25, RF27, UC14/15/16)

| Endpoint | Permissão | Regra |
|----------|-----------|-------|
| `GET /api/v1/usuarios?page=&perfil=&ativo=` | `USUARIO_VER` | ADMIN vê todos; GERENTE vê só seus vendedores |
| `POST /api/v1/usuarios` | `USUARIO_CRIAR` | RN-05/RN-06 abaixo |
| `PUT /api/v1/usuarios/{id}` | `USUARIO_EDITAR` | nome/e-mail |
| `PATCH /api/v1/usuarios/{id}/desativar` | `USUARIO_DESATIVAR` | RN-07; publica `UsuarioDesativadoEvent` |
| `PATCH /api/v1/usuarios/{id}/reativar` | `USUARIO_DESATIVAR` | RF20: restaura acesso e vínculos |
| `PATCH /api/v1/usuarios/{id}/perfil` | `USUARIO_ALTERAR_PERFIL` | RF25, só ADMIN |

Response de usuário nunca inclui `senhaHash`. Criação gera senha provisória (retornada uma única vez na resposta do POST) com `trocarSenha=true`.

## 3. Regras de negócio

- **RN-01** Senha: mínimo 8 caracteres, ao menos 1 letra e 1 número (RF01). Validar em criação, redefinição e troca.
- **RN-02** E-mail único em `usuario` (409 se duplicado).
- **RN-03** Bloqueio: 5 tentativas erradas seguidas → `bloqueadoAte` = agora + 15 min; login durante bloqueio retorna 423 sem revalidar senha; sucesso zera `tentativasLogin` e grava `ultimoAcesso` (RF01).
- **RN-04** Código de recuperação: uso único, 1h de validade; um novo código invalida os anteriores não usados do mesmo usuário (RF17).
- **RN-05** ADMIN cria contas ADMIN e GERENTE. GERENTE cria somente VENDEDOR (RF14, RF27). VENDEDOR não cria ninguém.
- **RN-06** Vendedor criado por gerente fica permanentemente vinculado a ele: `usuario.gerente` = criador, imutável (RF27).
- **RN-07** Desativação não apaga nada: `ativo=false`, dados preservados, carteira sinalizada "sem responsável ativo", sem redistribuição automática (RF33). ADMIN não pode desativar a própria conta.
- **RN-08** Seed do admin inicial (RF32): se não existe nenhum ADMIN ativo, cria com `SEED_ADMIN_EMAIL`/`SEED_ADMIN_SENHA` e `trocarSenha=true`.

## 4. Pattern A · Proxy via AOP: `@RequiresPermission`

Autorização fora do código de negócio, como no Ativo 6. O aspecto age como **Proxy de
proteção**: intercepta a chamada e decide se o método real executa.

```java
@Target(ElementType.METHOD) @Retention(RetentionPolicy.RUNTIME)
public @interface RequiresPermission { String value(); }

@Aspect @Component
public class PermissionAspect {
    // before: lê o JWT do contexto, compara value() com as claims 'permissoes'
    // nega -> SemPermissaoException (403); permite -> prossegue via joinPoint
}
```

Uso nos outros módulos: `@RequiresPermission("CLIENTE_CRIAR")` sobre o método do controller.
Documentar no PR: o Spring entrega isso com proxies dinâmicos (JDK/CGLIB), e a filter chain
do Security é um Chain of Responsibility real para citar no trabalho.

## 5. Pattern B · Adapter: `EmailService`

O domínio conhece só a porta; o detalhe de infraestrutura fica no adapter (trocável sem tocar regra):

```java
public interface EmailService { void enviar(String para, String assunto, String corpo); }

@Component @Profile({"dev","test"})
class EmailConsoleAdapter implements EmailService { /* loga no console */ }

@Component @Profile("prod")
class EmailSmtpAdapter implements EmailService { /* JavaMailSender */ }
```

## 6. Catálogo de permissões (seed `perfil_permissao`)

| Permissão | ADMIN | GERENTE | VENDEDOR |
|-----------|:-----:|:-------:|:--------:|
| CLIENTE_VER / CLIENTE_CRIAR / CLIENTE_EDITAR / CLIENTE_EXCLUIR / CLIENTE_TRANSFERIR | ✓ | ✓ | ✓ (transferir: não) |
| INTERACAO_VER / INTERACAO_CRIAR | ✓ | ✓ | ✓ |
| OPORTUNIDADE_VER / CRIAR / EDITAR / MOVER_ETAPA / REABRIR | ✓ | ✓ | ✓ |
| TAREFA_VER / CRIAR / EDITAR / CONCLUIR | ✓ | ✓ | ✓ |
| NOTIFICACAO_VER | ✓ | ✓ | ✓ |
| DASHBOARD_VER | ✓ | ✓ | ✓ |
| RANKING_VER (RF31: vendedor NÃO acessa) | ✓ | ✓ | — |
| RELATORIO_EXPORTAR (RF22: vendedor não exporta) | ✓ | ✓ | — |
| USUARIO_VER / USUARIO_CRIAR | ✓ | ✓ | — |
| USUARIO_EDITAR / DESATIVAR / ALTERAR_PERFIL | ✓ | — | — |

O que cada um enxerga DENTRO da permissão é papel do `EscopoCarteira` (seção 7), não da permissão.

## 7. Componente compartilhado: `EscopoCarteira`

Consumido por SPEC-02/03/04 em toda listagem (RF15, RF22, RF26):

```java
public interface EscopoCarteira {
    /** ids de vendedor que o usuário autenticado pode enxergar:
     *  VENDEDOR -> [eu]; GERENTE -> [meus vendedores + eu]; ADMIN -> Optional.empty() = sem filtro */
    Optional<List<Integer>> vendedoresVisiveis();
}
```

## 8. Checklist de auditoria (colar no PR)

- [ ] Login feliz, senha errada 5x → 423, desbloqueio após 15 min (teste com clock mockado)
- [ ] JWT expira em 8h; requisição com token vencido → 401
- [ ] Recuperar senha de e-mail inexistente → 204 igualzinho ao existente
- [ ] Código de recuperação reutilizado → 422; expirado (1h+) → 422
- [ ] GERENTE tentando criar ADMIN → 403; VENDEDOR criando qualquer um → 403
- [ ] Desativar usuário mantém clientes/oportunidades no banco (RF33) e publica evento
- [ ] Seed idempotente: dois starts seguidos não duplicam admin nem permissões
- [ ] Nenhum endpoint retorna `senhaHash`/`codigoHash` (asserção nos testes de integração)

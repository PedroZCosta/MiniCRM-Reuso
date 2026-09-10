# SPEC-01 · Autenticação e Usuários

**Responsável:** ______
**Requisitos:** RF01, RF02, RF14, RF17, RF20, RF25, RF27, RF32, RF33 · RNF03
**Patterns desta spec:** Singleton nº 1 (`EscopoCarteira`) e Template Method nº 1 (`SeedBase` + `SeedAdmin`)

## 1. Entidades

`Usuario` e `TokenRecuperacao` (já criadas). Nenhuma tabela nova: permissões ficam em
código (§5), como decisão de simplicidade desta fase.

## 2. Endpoints públicos (sem token)

### `POST /api/v1/auth/login` (RF01)

Request `{ "email", "senha" }` → 200:

```json
{ "token": "eyJ...", "expiraEm": "2026-09-10T22:30:00",
  "usuario": { "idUsuario": 1, "nome": "...", "perfil": "GERENTE", "trocarSenha": false } }
```

Erros: 401 credencial errada (mensagem genérica, sem dizer qual campo) · 423 bloqueado ·
422 usuário desativado. Token JWT com claims `sub` (id), `perfil` e validade de **8 horas** (RF01).

### `POST /api/v1/auth/recuperar-senha` (RF17)

`{ "email" }` → **sempre 204** (não revela se o e-mail existe). Se existir: gera código de
6 dígitos, grava em `token_recuperacao` (`codigoHash` = BCrypt do código, `expiraEm` =
agora + 1h, `usado = false`) e envia por e-mail. Nesta fase o "envio" é uma interface
`EmailService` com implementação que loga no console (a SMTP real entra depois, sem mudar
quem chama).

### `POST /api/v1/auth/redefinir-senha` (RF17)

`{ "email", "codigo", "novaSenha" }` → 204. Erros 422: código errado, expirado ou já
usado. Sucesso: marca `usado = true`, zera tentativas, `trocarSenha = false`.

## 3. Endpoints autenticados

| Endpoint | Quem pode | Regra |
|----------|-----------|-------|
| `PUT /api/v1/auth/trocar-senha` `{senhaAtual, novaSenha}` | todos | obrigatório enquanto `trocarSenha=true`: filtro barra o resto com 403 (RF32) |
| `GET /api/v1/usuarios?page=&perfil=&ativo=` | ADMIN, GERENTE | GERENTE só vê os próprios vendedores |
| `POST /api/v1/usuarios` | ADMIN, GERENTE | RN-05, RN-06 |
| `PUT /api/v1/usuarios/{id}` | ADMIN | nome e e-mail |
| `PATCH /api/v1/usuarios/{id}/desativar` | ADMIN | RN-07 (RF33) |
| `PATCH /api/v1/usuarios/{id}/reativar` | ADMIN | RF20 |
| `PATCH /api/v1/usuarios/{id}/perfil` | ADMIN | RF25 |

Criação retorna senha provisória (uma única vez) e a conta nasce com `trocarSenha=true`.
Nenhum response inclui `senhaHash`.

## 4. Regras de negócio

- **RN-01** Senha: mínimo 8 caracteres com letras e números (RF01). Vale para criar, trocar e redefinir.
- **RN-02** E-mail único → 409.
- **RN-03** 5 erros de login seguidos → `bloqueadoAte = agora + 15 min` → 423 durante o bloqueio. Acerto zera `tentativasLogin` e grava `ultimoAcesso`.
- **RN-04** Código de recuperação: uso único, 1h; código novo invalida os anteriores não usados.
- **RN-05** ADMIN cria ADMIN e GERENTE; GERENTE cria só VENDEDOR (RF14, RF27); VENDEDOR não cria ninguém.
- **RN-06** Vendedor criado pelo gerente fica vinculado a ele para sempre: `usuario.gerente` = criador (RF27).
- **RN-07** Desativar não apaga nada: `ativo=false`, clientes e oportunidades ficam no banco e aparecem para GERENTE/ADMIN com o vendedor sinalizado inativo, sem redistribuição automática (RF33). ADMIN não desativa a si mesmo.
- **RN-08** Seed (RF32): se não houver ADMIN ativo, cria um com `SEED_ADMIN_EMAIL`/`SEED_ADMIN_SENHA` e `trocarSenha=true`.

## 5. Permissões: simples, em código

Um enum e um mapa. Sem tabela, sem framework extra:

```java
public enum Permissao {
    CLIENTE_VER, CLIENTE_CRIAR, CLIENTE_EDITAR, CLIENTE_EXCLUIR, CLIENTE_TRANSFERIR,
    INTERACAO_VER, INTERACAO_CRIAR,
    OPORTUNIDADE_VER, OPORTUNIDADE_CRIAR, OPORTUNIDADE_EDITAR, OPORTUNIDADE_MOVER, OPORTUNIDADE_REABRIR,
    TAREFA_VER, TAREFA_CRIAR, TAREFA_EDITAR, TAREFA_CONCLUIR,
    NOTIFICACAO_VER, DASHBOARD_VER, RANKING_VER, RELATORIO_EXPORTAR,
    USUARIO_VER, USUARIO_CRIAR, USUARIO_EDITAR, USUARIO_DESATIVAR, USUARIO_ALTERAR_PERFIL
}

public final class Permissoes {
    public static Set<Permissao> doPerfil(PerfilUsuario perfil) {
        return switch (perfil) {
            case ADMIN    -> EnumSet.allOf(Permissao.class);
            case GERENTE  -> /* tudo, menos USUARIO_EDITAR/DESATIVAR/ALTERAR_PERFIL */;
            case VENDEDOR -> /* operacionais; SEM ranking, relatório, usuários e transferir (RF22, RF26, RF31) */;
        };
    }
}
```

O filtro JWT transforma isso em authorities; nos controllers, só a anotação pronta do
Spring: `@PreAuthorize("hasAuthority('CLIENTE_CRIAR')")`.

## 6. Pattern · Singleton nº 1: `EscopoCarteira`

```java
@Service
public class EscopoCarteira {
    /** VENDEDOR -> [eu] · GERENTE -> [meus vendedores + eu] · ADMIN -> Optional.empty() = sem filtro */
    public Optional<List<Integer>> vendedoresVisiveis() { ... }
}
```

Para o trabalho: `@Service` tem escopo **singleton** por padrão. O container do Spring
cria UMA instância e injeta essa mesma instância em clientes, funil, tarefas e relatórios.
Ganhamos o padrão sem escrever `private static instance` nem `getInstance()`: o papel de
controlar a instância única passou para o container. Provar com teste: injetar em dois
pontos e `assertSame(a, b)`.

## 7. Pattern · Template Method nº 1: `SeedBase`

```java
public abstract class SeedBase implements CommandLineRunner {
    @Override public final void run(String... args) {   // o template: fixo, final
        if (jaExecutou()) return;
        criarDados();
        log.info("Seed {} aplicado", nome());
    }
    protected abstract boolean jaExecutou();            // passos que variam
    protected abstract void criarDados();
    protected abstract String nome();
}
```

Filhas: `SeedAdmin` (aqui, RN-08) e `SeedMotivosPerda` (SPEC-03). O esqueleto do algoritmo
mora na classe base; as filhas só preenchem os passos.

## 8. Checklist de auditoria (colar no PR)

- [ ] 5 senhas erradas → 423; após 15 min volta a aceitar (teste com clock mockado)
- [ ] Token com 8h; requisição com token vencido → 401
- [ ] Recuperar senha de e-mail inexistente responde 204 idêntico ao existente
- [ ] Código usado 2x → 422; expirado → 422
- [ ] GERENTE criando ADMIN → 403; VENDEDOR criando qualquer conta → 403
- [ ] Desativar preserva clientes e oportunidades no banco (RF33)
- [ ] Seed rodando 2x não duplica admin
- [ ] Nenhum response contém senhaHash/codigoHash
- [ ] `assertSame` provando o singleton do `EscopoCarteira`

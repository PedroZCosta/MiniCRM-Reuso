# Especificações Técnicas · Mini CRM PJBL (Backend)

Divisão do backend em 4 módulos independentes + 1 spec de fundação com os contratos
compartilhados. Cada integrante assume uma spec, e a SPEC-00 é leitura obrigatória de
todos antes de começar: ela define os contratos que impedem divergência entre os módulos.

Fonte dos requisitos: `Project Planning for Software Product Lines.pdf` (RF01-RF33,
RNF01-RNF10, UC01-UC18, Ativo 6 - Módulo RBAC). O modelo de dados já está implementado
em `src/main/java/com/miniCRM/miniCRM/model`.

## Escopo desta fase

- **Somente backend** (API REST + jobs agendados). O front-end virá em fase futura.
- A Parte III do PDF (alta variabilidade, PV1-PV12) fica fora desta fase, com uma exceção
  que sai de graça: as permissões RBAC como dados no banco (VP-1, VP-2, VP-3 do Ativo 6).
- Requisitos de interface (RNF01, RNF04, RNF08, RNF09 e a parte visual de RF13/RF24)
  ficam para o front. O backend entrega os dados prontos para eles.

## As specs

| Spec | Módulo | Responsável | Requisitos | Design Patterns |
|------|--------|-------------|------------|-----------------|
| [SPEC-00](SPEC-00-fundacao-contratos.md) | Fundação e contratos compartilhados | todos | RNF03, RNF05, RF18 | Singleton (beans Spring), convenções |
| [SPEC-01](SPEC-01-autenticacao-usuarios-rbac.md) | Autenticação, Usuários e RBAC | ______ | RF01, RF02, RF14, RF17, RF20, RF25, RF27, RF32, RF33 | **Proxy** (AOP de permissão), **Adapter** (e-mail), Chain of Responsibility (filter chain) |
| [SPEC-02](SPEC-02-clientes-interacoes.md) | Clientes e Interações | ______ | RF03, RF04, RF05, RF06, RF16, RF26, RF30 | **Specification/Composite** (busca dinâmica), **Observer** (status do cliente) |
| [SPEC-03](SPEC-03-funil-oportunidades.md) | Funil de Oportunidades | ______ | RF07, RF08, RF09, RF19, RF29 | **State** (etapas do funil), **Observer** (eventos de domínio, lado publicador) |
| [SPEC-04](SPEC-04-tarefas-notificacoes-dashboard-relatorios.md) | Tarefas, Notificações, Dashboard e Relatórios | ______ | RF10, RF11, RF12, RF13, RF15, RF21, RF22, RF23, RF24, RF28, RF31 | **Factory Method** (notificações), **Strategy + Template Method** (exportadores), **Facade** (dashboard) |

Integrantes do grupo: Lincoln Neto, Pedro Costa, Leandro Canha, Nicolas Hara.
Preencham a coluna Responsável no primeiro commit de cada um.

## Mapa de integração entre módulos

Toda comunicação entre módulos acontece por UM destes dois canais. Nenhum service de um
módulo chama repository de outro módulo. Isso é o que a auditoria vai verificar primeiro.

### Canal 1: eventos de domínio (assíncrono, desacoplado)

| Evento | Publicado por | Consumido por | Efeito |
|--------|---------------|---------------|--------|
| `OportunidadeEtapaAlteradaEvent` | SPEC-03 | SPEC-03 (grava `historico_etapa`) | trilha do funil |
| `OportunidadeFechadaEvent` | SPEC-03 | SPEC-02 (RF30: cliente vira ATIVO na 1ª ganha) | status automático |
| `TarefaVencendoEvent` | SPEC-04 (job) | SPEC-04 (NotificacaoFactory) | notificações D15/D1/HOJE/VENCIDA |
| `UsuarioDesativadoEvent` | SPEC-01 | SPEC-02 (marca carteira "sem responsável ativo", RF33) | preservação de dados |

Payloads definidos na SPEC-00, seção 6. Ninguém altera um payload sem PR aprovado pelos 4.

### Canal 2: interfaces de serviço público

| Interface | Dono | Consumidores | Uso |
|-----------|------|--------------|-----|
| `EscopoCarteira` | SPEC-01 | SPEC-02, 03, 04 | resolve quais `idVendedor` o usuário logado enxerga (RF15/RF22/RF26) |
| `@RequiresPermission` + aspecto | SPEC-01 | todos | autorização declarativa nos endpoints |
| `ClienteConsultaService` | SPEC-02 | SPEC-03, 04 | validar existência/estado do cliente sem acoplar repository |
| `OportunidadeConsultaService` | SPEC-03 | SPEC-04 | dados de oportunidade para dashboard/relatórios |

## Matriz de rastreabilidade RF → Spec

RF01,02,14,17,20,25,27,32,33 → SPEC-01 · RF03,04,05,06,16,26,30 → SPEC-02 ·
RF07,08,09,19,29 → SPEC-03 · RF10-13,15,21-24,28,31 → SPEC-04 · RF18 (paginação) → SPEC-00 (todos).

## Fluxo de trabalho no Git

1. Uma branch por spec: `spec-01-auth`, `spec-02-clientes`, `spec-03-funil`, `spec-04-tarefas`.
2. Commits em Conventional Commits, em português, sempre citando o requisito: `feat(funil): mover etapa com validação de transição (RF09)`.
3. PR para a `main` com checklist de auditoria da própria spec preenchido (seção final de cada spec).
4. Quem revisa o PR é o dev da spec vizinha (01↔04, 02↔03), verificando os contratos da SPEC-00.

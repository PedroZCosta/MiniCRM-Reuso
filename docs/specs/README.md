# Especificações Técnicas · Mini CRM PJBL (Backend)

Backend dividido em 4 módulos, um por integrante, + a SPEC-00 com as regras que valem para
todos. Leiam a SPEC-00 antes de codar: é ela que garante que as partes se encaixam.

Fonte dos requisitos: `Project Planning for Software Product Lines.pdf` (RF01-RF33,
RNF01-RNF10). O modelo de dados já está pronto em `src/main/java/com/miniCRM/miniCRM/model`.

**Princípio geral: simples resolve.** Nada de camada extra, abstração "para o futuro" ou
configuração esperta. Código que um júnior lê e entende na primeira passada.

## Escopo desta fase

- Somente backend (API REST + 1 job agendado). Front-end fica para a próxima fase.
- Requisitos de interface (RNF01, RNF04, RNF08, RNF09, parte visual de RF13/RF24) ficam
  para o front; o backend só entrega os dados.

## As specs

| Spec | Módulo | Responsável | Requisitos |
|------|--------|-------------|------------|
| [SPEC-00](SPEC-00-fundacao-contratos.md) | Fundação e contratos | todos | RF18, RNF03, RNF05 |
| [SPEC-01](SPEC-01-autenticacao-usuarios-rbac.md) | Autenticação e Usuários | ______ | RF01, RF02, RF14, RF17, RF20, RF25, RF27, RF32, RF33 |
| [SPEC-02](SPEC-02-clientes-interacoes.md) | Clientes e Interações | ______ | RF03, RF04, RF05, RF06, RF16, RF26, RF30 |
| [SPEC-03](SPEC-03-funil-oportunidades.md) | Funil de Oportunidades | ______ | RF07, RF08, RF09, RF19, RF29 |
| [SPEC-04](SPEC-04-tarefas-notificacoes-dashboard-relatorios.md) | Tarefas, Notificações, Dashboard e Relatórios | ______ | RF10-13, RF15, RF21-24, RF28, RF31 |

Integrantes: Lincoln Neto, Pedro Costa, Leandro Canha, Nicolas Hara. Preencham a coluna
Responsável no primeiro commit.

## Design Patterns do trabalho (combinado com o professor)

Obrigatório: **2 Singletons + 3 Template Methods + 3 outros padrões**, todos surgindo
naturalmente do projeto. Onde cada um vive:

| # | Padrão | Onde | Spec | O que apresentar |
|---|--------|------|------|------------------|
| 1 | **Singleton** | `EscopoCarteira` (@Service) | 01 | O Spring cria UMA instância por container (escopo padrão singleton) e injeta a mesma em todos os módulos. Mostrar que não precisamos de `getInstance()`: o container faz o papel do padrão. |
| 2 | **Singleton** | `ClienteService` (@Service) | 02 | Mesmo mecanismo, segundo exemplo exigido. Demonstrar com teste: dois `@Autowired` recebem o mesmo objeto (`assertSame`). |
| 3 | **Template Method** | `SeedBase` → `SeedAdmin`, `SeedMotivosPerda` | 01 e 03 | Esqueleto fixo (`jaExecutou()? senão criarDados() e logar`), passos variáveis nas filhas. |
| 4 | **Template Method** | `GeradorNotificacaoBase` → D15, D1, Hoje, Vencida | 04 | Base monta a `Notificacao` comum; cada filha define tipo e mensagem. |
| 5 | **Template Method** | `ExportadorBase` → `CsvExportador`, `PdfExportador` | 04 | `exportar()` final: abre → cabeçalho → linhas → fecha; filhas escrevem cada passo. |
| 6 | **Strategy** | `RelatorioExportStrategy` (csv/pdf) | 04 | O controller escolhe a estratégia pelo query param `formato`, sem if espalhado. |
| 7 | **Observer** | `OportunidadeFechadaEvent` | 03 publica, 02 escuta | Funil não conhece o módulo de clientes; o listener promove PROSPECT → ATIVO (RF30). |
| 8 | **Facade** | `DashboardFacade` | 04 | Uma fachada esconde 3 services; o controller do dashboard tem ~3 linhas (RF11). |

Strategy e Template Method dos exportadores convivem na mesma classe base, e isso é bom
para o trabalho: dá para mostrar os dois papéis no mesmo código.

## Como os módulos conversam (sem divergência)

Duas regras, verificáveis no code review:

1. **Service pode chamar service público de outro módulo. Repository de outro módulo, nunca.**
   Ex.: `OportunidadeService` valida cliente chamando `ClienteService.buscarAtivo(id)`.
2. **O único evento do sistema é `OportunidadeFechadaEvent`** (payload na SPEC-00 §5).
   Ninguém cria evento novo nem muda o payload sem combinar com o grupo.

## Fluxo de trabalho no Git

1. Uma branch por spec: `spec-01-auth`, `spec-02-clientes`, `spec-03-funil`, `spec-04-tarefas`.
2. Conventional Commits em português citando o requisito: `feat(funil): mover etapa (RF09)`.
3. PR para a `main` com o checklist de auditoria da spec preenchido.
4. Revisão cruzada: 01↔04 e 02↔03 revisam um ao outro.

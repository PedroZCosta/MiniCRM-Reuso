# Database First do Mini CRM (.NET)

O backend principal do Mini CRM é **code first**: as entidades Java existem primeiro e o Hibernate cria as tabelas. Aqui o fluxo é invertido: partimos do **banco que já existe** (`mini_crm_db`, as 9 tabelas do Mini CRM) e geramos o código a partir dele, até o CRUD e as telas.

```
MySQL (9 tabelas)
   │  dotnet ef dbcontext scaffold          (EF Core Scaffolding, scripts/gerar.sh)
   ▼
9 entidades + MiniCrmContext
   │  dotnet aspnet-codegenerator controller   (scripts/gerar-crud.sh)
   ▼
9 controllers MVC + 45 views Razor (as telas)  +  9 controllers de API (Swagger)
   │  dotnet run --project MiniCrm.DbFirst
   ▼
http://localhost:8095  (telas)      http://localhost:8095/swagger  (API)
```

| Peça | Ferramenta | O que gera |
|---|---|---|
| Entidades e `DbContext` | `dotnet ef dbcontext scaffold` (EF Core 9, provider Pomelo 9.0.0) | `Models/*.cs` (9 classes) e `Data/MiniCrmContext.cs` |
| Telas | `dotnet aspnet-codegenerator controller` | `Controllers/*Controller.cs` e `Views/<Entidades>/` (Index, Details, Create, Edit, Delete) |
| API REST | `dotnet aspnet-codegenerator controller -api` | `Controllers/Api/*Controller.cs` (GET, POST, PUT, DELETE em `/api/<Entidades>`) |

**Gerado (não editar à mão, rode os scripts de novo):** `Models/`, `Data/`, `Controllers/` (menos o `HomeController`) e `Views/` (menos `Home` e `Shared`).
**Escrito à mão:** `Program.cs`, `Design/DesignTimeServices.cs` (pluralizador em português), `appsettings.json`, o `.csproj` e a pasta `scripts/`.

## Pré-requisitos

- Docker
- SDK do .NET 9 e as duas ferramentas. Sem sudo, no seu usuário:

```bash
curl -sSL https://dot.net/v1/dotnet-install.sh -o dotnet-install.sh
bash dotnet-install.sh --channel 9.0 --install-dir ~/.dotnet
~/.dotnet/dotnet tool install dotnet-ef --tool-path ~/.dotnet-tools --version "9.*"
~/.dotnet/dotnet tool install dotnet-aspnet-codegenerator --tool-path ~/.dotnet-tools --version "9.*"
```

Os scripts usam `~/.dotnet` e `~/.dotnet-tools` sozinhos (veja `scripts/ambiente.sh`); se o SDK estiver em outro lugar, ajuste `DOTNET_ROOT`.

## Roteiro de demonstração

```bash
cd dbfirst

docker compose up -d --wait   # 1. banco com as 9 tabelas e dados de exemplo (MySQL 8.4, porta 3307)
./scripts/gerar.sh            # 2. entidades e DbContext gerados a partir do banco
./scripts/gerar-crud.sh       # 3. controllers, telas e API gerados a partir das entidades
dotnet run --project MiniCrm.DbFirst   # 4. aplicação em http://localhost:8095
```

Se o terminal responder `dotnet: command not found`, o SDK instalado em `~/.dotnet` não está no `PATH` desta sessão. Rode isto uma vez antes do passo 4 (os scripts `gerar.sh` e `gerar-crud.sh` já fazem isso sozinhos):

```bash
export DOTNET_ROOT=$HOME/.dotnet PATH=$HOME/.dotnet:$PATH
```

Com a aplicação no ar, teste o CRUD no navegador:

- **http://localhost:8095** abre a lista de clientes (telas geradas). Use `Create New`, `Edit`, `Details` e `Delete`.
- **http://localhost:8095/swagger** lista os 9 recursos da API com GET, POST, PUT e DELETE. Expanda um endpoint e use `Try it out`.
- As demais telas: `/Oportunidades`, `/Tarefas`, `/Interacoes`, `/Usuarios`, `/HistoricoEtapas`, `/MotivoPerdas`, `/Notificacoes` e `/TokenRecuperacoes`.

### O que mostrar: o banco manda

Crie uma tabela direto no MySQL, regenere e veja a entidade, o CRUD e a tela nascerem sozinhos:

```bash
docker exec minicrm-mysql-dbfirst mysql -uroot -prootpw mini_crm_db -e \
  "CREATE TABLE produto (id_produto INT AUTO_INCREMENT PRIMARY KEY, nome VARCHAR(100) NOT NULL, preco DECIMAL(10,2) NOT NULL);
   INSERT INTO produto (nome, preco) VALUES ('Plano Premium', 199.90);"
./scripts/gerar.sh && ./scripts/gerar-crud.sh   # surgem Models/Produto.cs, ProdutosController e Views/Produtos
dotnet run --project MiniCrm.DbFirst            # http://localhost:8095/Produtos
```

Para voltar ao estado original: `DROP TABLE produto;` e rode `gerar.sh` e `gerar-crud.sh` de novo.

## Banco de dados

`banco/schema.sql` e `banco/dados-demo.sql` são o `mysqldump` do banco criado pelo backend do Mini CRM e populado pela API real (usuários, clientes, interações, oportunidades e tarefas fictícios). O `docker-compose.yml` carrega os dois na primeira subida. O projeto nunca roda migrations nem `EnsureCreated`: o EF só lê e grava dados.

## Limitações e peculiaridades conhecidas

- **Sem segurança.** A API e as telas geradas não têm login nem permissões. Servem para demonstrar a geração; não exponha fora da máquina local. O sistema real é o backend em `../backend`.
- **Sem camada de service nem regras de negócio.** Os controllers gerados usam o `MiniCrmContext` direto. Escopo de carteira, transições do funil e status automático continuam no backend principal.
- **Arquivos estáticos do template.** O `wwwroot/lib` (Bootstrap e jQuery do template MVC) vem sem os `.map` (6 MB de sourcemaps, só úteis para depurar no navegador).
- **Telas genéricas**, em inglês (`Index`, `Create New`) e com os campos na ordem alfabética das colunas.
- **Nomes mecânicos.** A chave `id_vendedor` vira a navegação `IdVendedorNavigation`. O pluralizador padrão do EF só conhece inglês e gerava `MotivoPerdum` e `Interacaos`, então o projeto traz um `IPluralizer` em português (`Design/DesignTimeServices.cs`).
- **Tipos.** `bit(1)` (`excluido`) vira `ulong` e `datetime` vira `DateTime`.
- **`[MaxLength(6)]` em campos de data.** Com `--data-annotations`, o Pomelo marca colunas `datetime(6)` com `[MaxLength(6)]`, e o ASP.NET quebra na validação (HTTP 500). O `gerar.sh` remove só esse atributo.
- **O gerador de controllers não conhece MySQL.** O `aspnet-codegenerator` só oferece SQL Server, SQLite, Cosmos e Postgres e exige o pacote do SQL Server no projeto. O `gerar-crud.sh` o adiciona só durante a geração e o remove depois. Ele também exige `Microsoft.EntityFrameworkCore.Tools`.
- **Compilação antes do scaffold.** O scaffold compila o projeto antes de gerar, e o `Program.cs` usa o `MiniCrmContext`. Por isso o `gerar.sh` cria um `DbContext` vazio provisório, que o scaffold sobrescreve.
- **Versões.** O provider Pomelo 9.0.0 exige EF Core 9.0.x, então o projeto usa o SDK 9. O Swashbuckle está na 10.3.1 porque a 11.0.0 só suporta .NET 10 ou superior.

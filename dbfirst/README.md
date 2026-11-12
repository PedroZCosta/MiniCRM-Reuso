# Database First do Mini CRM (.NET)

O backend principal do Mini CRM é **code first**: as entidades Java existem primeiro e o Hibernate cria as tabelas. Aqui o fluxo é invertido: partimos do **banco que já existe** (`mini_crm_db`, as 9 tabelas do Mini CRM) e geramos o código a partir dele, até o CRUD e as telas.

```
MySQL (9 tabelas)
   │  dotnet ef dbcontext scaffold          (EF Core Scaffolding, passo 6)
   ▼
9 entidades + MiniCrmContext
   │  dotnet-aspnet-codegenerator controller   (passo 9)
   ▼
9 controllers MVC + 45 views Razor (as telas)  +  9 controllers de API (Swagger)
   │  dotnet run --project MiniCrm.DbFirst --urls ...  (passo 10)
   ▼
http://localhost:8095  (telas)      http://localhost:8095/swagger  (API)
```

| Peça | Ferramenta | O que gera |
|---|---|---|
| Entidades e `DbContext` | `dotnet ef dbcontext scaffold` (EF Core 9, provider Pomelo 9.0.0) | `Models/*.cs` (9 classes) e `Data/MiniCrmContext.cs` |
| Telas | `dotnet-aspnet-codegenerator controller` | `Controllers/*Controller.cs` e `Views/<Entidades>/` (Index, Details, Create, Edit, Delete) |
| API REST | `dotnet-aspnet-codegenerator controller -api` | `Controllers/Api/*Controller.cs` (GET, POST, PUT, DELETE em `/api/<Entidades>`) |

**Gerado pelas ferramentas (não editar à mão, rode a geração de novo):** `Models/` (as 9 entidades), `Data/`, `Controllers/` (menos o `HomeController`) e `Views/` (menos `Home` e `Shared`).
**Criado por `dotnet new mvc` (passo 3):** o esqueleto do site: `HomeController`, `Views/Home`, `Views/Shared` (o layout), `Views/_ViewStart.cshtml`, `Views/_ViewImports.cshtml`, `Models/ErrorViewModel.cs` e `wwwroot/`.
**Escrito à mão (passos 5 e 8):** `Program.cs` e `appsettings.json`; mais o `.csproj`, ajustado nos passos 4 e 5. Todos estão no passo a passo, então o projeto inteiro **pode ser apagado e recriado só com os comandos do README**. A pasta `scripts/` é a opção secundária.

## Dois jeitos de rodar

| | Como | Quando usar |
|---|---|---|
| **Principal: comando por comando** | Você digita cada comando, na ordem, vendo o que ele faz e o que aparece (seção abaixo) | Para **aprender e demonstrar**: cada passo fica visível |
| **Secundário: scripts** | Os mesmos comandos agrupados em `scripts/gerar.sh` e `scripts/gerar-crud.sh` (veja [Opção secundária: scripts](#opção-secundária-scripts)) | Para **repetir rápido** |

Os dois jeitos foram testados lado a lado e geram **exatamente os mesmos arquivos** (mesmo hash dos arquivos gerados).

## Pré-requisitos

- Docker
- SDK do .NET 9 e as duas ferramentas. Sem sudo, no seu usuário:

```bash
curl -sSL https://dot.net/v1/dotnet-install.sh -o dotnet-install.sh
bash dotnet-install.sh --channel 9.0 --install-dir ~/.dotnet
~/.dotnet/dotnet tool install dotnet-ef --tool-path ~/.dotnet-tools --version "9.*"
~/.dotnet/dotnet tool install dotnet-aspnet-codegenerator --tool-path ~/.dotnet-tools --version "9.*"
```


## Passo a passo: do primeiro comando até o CRUD funcionando

Todos os comandos rodam **dentro da pasta `dbfirst/`**, na ordem, no mesmo terminal. Em cada passo: o **comando**, **o que ele faz** e **o que você deve ver**. Os passos 2 a 5 criam o projeto do zero; os passos 6 a 9 são o Database First em si.

| Passo | Comando principal | Para quê |
|---|---|---|
| 0 | `export ... PATH=...` | O terminal achar o `dotnet` e as ferramentas |
| 1 | `docker compose up -d --wait` | Subir o MySQL com as 9 tabelas |
| 2 | `rm -rf MiniCrm.DbFirst` | **Limpar** o projeto, se ele já existir (opcional) |
| 3 | `dotnet new mvc -n MiniCrm.DbFirst ...` | **Criar a base do projeto** (o esqueleto do site) |
| 4 | `dotnet add package ...` | Instalar os 6 pacotes |
| 5 | `sed`, `cat > ...` | Escrever a configuração: o banco |
| 6 | `dotnet ef dbcontext scaffold ...` | **Ler o banco** e gerar entidades e `DbContext` |
| 7 | `perl -0pi -e '...' Models/*.cs` | Consertar um defeito do gerador (datas) |
| 8 | `cat > Program.cs` | Ligar o `DbContext` gerado à aplicação |
| 9 | `dotnet-aspnet-codegenerator controller ...` | Gerar controllers, API e telas de cada entidade |
| 10 | `dotnet run --project MiniCrm.DbFirst --urls http://localhost:8095` | Subir a aplicação na porta 8095 |
| 11 | (navegador) | Testar o CRUD nas telas e no Swagger |

### Passo 0 — Preparar o terminal

```bash
cd dbfirst
export DOTNET_ROOT=$HOME/.dotnet PATH=$HOME/.dotnet:$HOME/.dotnet-tools:$PATH DOTNET_CLI_TELEMETRY_OPTOUT=1 DOTNET_NOLOGO=1
```

**O que faz:** entra na pasta `dbfirst` e ensina **este terminal** onde estão o `dotnet`, o `dotnet-ef` e o `dotnet-aspnet-codegenerator` (instalados no seu usuário, fora do `PATH` padrão). As duas últimas variáveis desligam a telemetria e o banner do `dotnet`.
**Vale só nesta janela:** se abrir outro terminal, repita o `export`.
**Você deve ver:** `dotnet --version` responde `9.0.x`. Se responder `command not found`, o `export` não foi feito neste terminal.

### Passo 1 — Subir o banco

```bash
docker compose up -d --wait
```

**O que faz:** baixa (na primeira vez) e sobe o MySQL 8.4 no Docker, na porta 3307. Ele cria o banco `mini_crm_db`, carrega `banco/schema.sql` (as tabelas) e `banco/dados-demo.sql` (os dados de exemplo) e **espera** o MySQL ficar pronto (`--wait`).
**Você deve ver:** no fim, `Container minicrm-mysql-dbfirst  Healthy`.

Para conferir (opcional):

```bash
docker exec minicrm-mysql-dbfirst mysql -uminiCrmUser -p'crm123P@' mini_crm_db -e "show tables;"
```

Deve listar as 9 tabelas: `cliente`, `historico_etapa`, `interacao`, `motivo_perda`, `notificacao`, `oportunidade`, `tarefa`, `token_recuperacao` e `usuario`.

### Passo 2 — Limpar o projeto (opcional)

```bash
rm -rf MiniCrm.DbFirst
```

**O que faz:** apaga o projeto **inteiro**, inclusive os arquivos escritos à mão. Os passos 3 a 5 recriam tudo, então depois deste passo você refaz o projeto do zero, na ordem.
**Quando usar:** para **ver tudo nascer do zero** (demonstração) ou quando uma **tabela saiu do banco** (os arquivos dela ficariam órfãos e o projeto deixaria de compilar).
**Quando pular:** na primeira vez, ou quando só quer gerar de novo. Os passos 6 a 9 já **sobrescrevem** o código gerado (`--force` no scaffold e `-f` no gerador), e uma tabela ou coluna **nova** entra sozinha, sem apagar nada.
**Se você apagou por engano:** `git restore dbfirst/MiniCrm.DbFirst`, a partir da raiz do repositório, traz o projeto de volta.

### Passo 3 — Criar a base do projeto

Antes do Database First é preciso um **projeto web** onde o código gerado vai morar. O `dotnet new` cria esse esqueleto a partir de um modelo que já vem dentro do SDK.

```bash
dotnet new mvc -n MiniCrm.DbFirst -o MiniCrm.DbFirst --no-restore
```

| Parte do comando | Significado |
|---|---|
| `dotnet new mvc` | Cria um projeto a partir do modelo "Aplicativo Web do ASP.NET Core (Model-View-Controller)". (`mvn` é o Maven, do Java; no .NET é `mvc`) |
| `-n MiniCrm.DbFirst` | O **nome** do projeto: dá nome ao arquivo `MiniCrm.DbFirst.csproj` e vira o *namespace* inicial do código |
| `-o MiniCrm.DbFirst` | A **pasta** onde criar o projeto (`dbfirst/MiniCrm.DbFirst/`) |
| `--no-restore` | Não baixa os pacotes agora; isso acontece no passo 4 |

**Você deve ver:** `O modelo "Aplicativo Web do ASP.NET Core (Model-View-Controller)" foi criado com êxito.`

#### O que o comando cria e o que fazemos com cada item

| Item | Para que serve | Situação neste projeto |
|---|---|---|
| `MiniCrm.DbFirst.csproj` | Descreve o projeto: versão do .NET e pacotes | **Alterado** nos passos 4 e 5: entram os pacotes e `Nullable` passa de `enable` para `disable` |
| `Program.cs` | Ponto de partida da aplicação (29 linhas no modelo) | **Reescrito** no passo 8: registra o `MiniCrmContext` com MySQL, evita ciclo no JSON, liga o Swagger e muda a página inicial para `Cliente` |
| `appsettings.json` | Configurações da aplicação | **Reescrito** no passo 5: acrescenta a `ConnectionStrings:MiniCrm`, o "como conectar no banco" |
| `appsettings.Development.json` | Configurações só do modo de desenvolvimento | Igual ao modelo |
| `Properties/launchSettings.json` | Como o `dotnet run` sobe: define o ambiente `Development`, que serve o CSS e o Swagger | Igual ao modelo. As portas são **sorteadas** quando o projeto é criado, por isso o passo 10 fixa a porta com `--urls` |
| `Controllers/HomeController.cs` | Controller da página inicial padrão e da página de erro | Igual (a rota inicial passa a ser `Cliente`) |
| `Views/Home/` (`Index`, `Privacy`) | Telas de exemplo do modelo | Igual |
| `Views/Shared/_Layout.cshtml` e `_Layout.cshtml.css` | **O esqueleto de toda página**: menu, rodapé e Bootstrap | Igual |
| `Views/Shared/Error.cshtml` | Tela de erro | Igual |
| `Views/Shared/_ValidationScriptsPartial.cshtml` | Scripts de validação dos formulários | Igual |
| `Views/_ViewStart.cshtml` | Faz **toda** tela usar o `_Layout` (`Layout = "_Layout";`) | Igual |
| `Views/_ViewImports.cshtml` | Os `@using` comuns a todas as telas, inclusive `MiniCrm.DbFirst.Models` | Igual |
| `Models/ErrorViewModel.cs` | Classe usada pela tela de erro | Igual |
| `wwwroot/` (`css`, `js`, `favicon.ico` e `lib/` com Bootstrap, jQuery e jQuery Validation) | Arquivos estáticos | Igual, **menos os 24 arquivos `.map`** (6 MB de sourcemaps, só úteis para depurar no navegador), que o passo 5 remove |

#### Por que a base importa para o Database First

As ferramentas escrevem **só** as entidades, o `DbContext`, o CRUD e as telas de cada tabela. A **casca** em volta (layout, Bootstrap, validação, página inicial) vem do modelo `dotnet new mvc`, e as telas geradas **dependem dela**: o `_ViewStart.cshtml` manda toda tela usar o `_Layout`, e o `_ViewImports.cshtml` importa o *namespace* `MiniCrm.DbFirst.Models`. As ferramentas **não** recriam a casca (testado: sem ela, só o `_ValidationScriptsPartial.cshtml` volta), por isso o passo 3 vem primeiro.

### Passo 4 — Instalar os pacotes

```bash
cd MiniCrm.DbFirst
dotnet add package Pomelo.EntityFrameworkCore.MySql --version 9.0.0
dotnet add package Microsoft.EntityFrameworkCore.Design --version 9.0.23
dotnet add package Microsoft.EntityFrameworkCore.Tools --version 9.0.23
dotnet add package Microsoft.EntityFrameworkCore.SqlServer --version 9.0.23
dotnet add package Microsoft.VisualStudio.Web.CodeGeneration.Design --version 9.0.13
dotnet add package Swashbuckle.AspNetCore --version 10.3.1
cd ..
```

**O que faz:** baixa cada pacote do NuGet para o seu computador e o registra no `MiniCrm.DbFirst.csproj`. A saída é longa (uma linha por pacote baixado); o que importa é cada comando terminar com `PackageReference ... adicionada ao arquivo`. As **versões são fixas** (`--version`) para o resultado ser sempre o mesmo.

| Pacote | Para quê |
|---|---|
| `Pomelo.EntityFrameworkCore.MySql` | O *provider*: faz o EF Core falar com o MySQL |
| `Microsoft.EntityFrameworkCore.Design` | Peças que o `dotnet ef` usa na geração |
| `Microsoft.EntityFrameworkCore.Tools` | O gerador de CRUD exige |
| `Microsoft.EntityFrameworkCore.SqlServer` | **Não é usado pela aplicação.** O gerador de CRUD só conhece SQL Server, SQLite, Cosmos e Postgres e se recusa a rodar sem ele, mesmo com MySQL (veja *Limitações*) |
| `Microsoft.VisualStudio.Web.CodeGeneration.Design` | O motor do `dotnet-aspnet-codegenerator` |
| `Swashbuckle.AspNetCore` | O Swagger (`/swagger`) |

### Passo 5 — Escrever a configuração

Dois ajustes, todos no projeto `MiniCrm.DbFirst`. É o único trecho de código escrito à mão antes da geração.

**5a. Ajustes no `.csproj` e limpeza dos `.map`:**

```bash
cd MiniCrm.DbFirst
sed -i 's#<Nullable>enable</Nullable>#<Nullable>disable</Nullable>#' MiniCrm.DbFirst.csproj
find wwwroot -name '*.map' -delete
```

- O `sed` desliga o `Nullable`: as entidades geradas não marcam o que pode ser nulo, e com ele ligado o compilador despejaria centenas de avisos.
- O `find` apaga os `.map` (sourcemaps) do Bootstrap e do jQuery: 6 MB que só servem para depurar no navegador.

**5b. A connection string** (o "como conectar no banco"; nome do banco, porta, usuário e senha ficam só aqui):

```bash
cat > appsettings.json <<'EOF'
{
  "Logging": {
    "LogLevel": {
      "Default": "Information",
      "Microsoft.AspNetCore": "Warning"
    }
  },
  "AllowedHosts": "*",
  "ConnectionStrings": {
    "MiniCrm": "Server=localhost;Port=3307;Database=mini_crm_db;User=miniCrmUser;Password=crm123P@"
  }
}
EOF
cd ..
```

### Passo 6 — Gerar as entidades a partir do banco

```bash
dotnet ef dbcontext scaffold "Name=ConnectionStrings:MiniCrm" Pomelo.EntityFrameworkCore.MySql \
  --project MiniCrm.DbFirst --output-dir Models --context MiniCrmContext --context-dir Data \
  --data-annotations --no-onconfiguring --no-pluralize --force
```

**O que faz:** é o **Database First em si**. O `dotnet ef` conecta no MySQL, lê as tabelas, colunas, chaves e relacionamentos e escreve uma classe C# por tabela mais o `MiniCrmContext`.

| Parte do comando | Significado |
|---|---|
| `dotnet ef dbcontext scaffold` | "gerar o `DbContext` e as entidades a partir de um banco que já existe" |
| `"Name=ConnectionStrings:MiniCrm"` | **como conectar**: lê a connection string chamada `MiniCrm` do `MiniCrm.DbFirst/appsettings.json` (passo 5b) |
| `Pomelo.EntityFrameworkCore.MySql` | o **provider**, o tradutor que sabe falar com o MySQL |
| `--project MiniCrm.DbFirst` | em qual projeto gravar |
| `--output-dir Models` | pasta das entidades |
| `--context MiniCrmContext --context-dir Data` | nome e pasta do `DbContext` |
| `--data-annotations` | marca as classes com `[Key]`, `[Required]`, `[StringLength]`, que as telas usam para validar |
| `--no-onconfiguring` | **não** grava a connection string dentro do `DbContext` |
| `--no-pluralize` | **não** aplica plural nem singular aos nomes: a tabela `interacao` vira a entidade `Interacao` e a coleção `Interacao`. Sem ele, o EF usaria regras do inglês e sairia `Interacaos`, e `motivo_perda` viraria `MotivoPerdum` |
| `--force` | sobrescreve o que já existe |

**Você deve ver:**

```
Build started...
Build succeeded.
Using ServerVersion '8.4.13-mysql'.
```

e a pasta `MiniCrm.DbFirst/Models/` com 9 entidades (`ls MiniCrm.DbFirst/Models`; o 10º arquivo, `ErrorViewModel.cs`, é do modelo do passo 3) mais `MiniCrm.DbFirst/Data/MiniCrmContext.cs`.

> **Por que o build passa agora:** o scaffold compila o projeto antes de gerar. Neste ponto o `Program.cs` ainda é o do modelo, que não usa o `MiniCrmContext`; por isso ele só é reescrito no passo 8, **depois** do scaffold.

### Passo 7 — Consertar o `[MaxLength]` das datas

```bash
perl -0pi -e 's/[ \t]*\[MaxLength\(\d+\)\]\r?\n(?=(?:[ \t]*\[[^\n]*\]\r?\n)*[ \t]*public (?:DateTime|DateOnly|TimeOnly)\??\s)//g' MiniCrm.DbFirst/Models/*.cs
```

**Por que existe:** com `--data-annotations`, o Pomelo marca as colunas `datetime(6)` com `[MaxLength(6)]`, e o ASP.NET quebra ao validar um campo de data (erro HTTP 500 ao criar ou editar). É um defeito do gerador.
**O que faz:** o `perl` edita os arquivos **no lugar** (`-pi`) e apaga o `[MaxLength(n)]` **somente** quando ele pertence a uma propriedade `DateTime`, `DateOnly` ou `TimeOnly` (o `(?=...)` olha as linhas seguintes sem apagá-las). Preserva os fins de linha da ferramenta.
**Você deve ver:** nenhuma saída. Para conferir:

```bash
grep -l MaxLength MiniCrm.DbFirst/Models/*.cs || echo "nenhum [MaxLength] sobrou"
```

### Passo 8 — Ligar o `DbContext` à aplicação (`Program.cs`)

```bash
cat > MiniCrm.DbFirst/Program.cs <<'EOF'
using System.Text.Json.Serialization;
using Microsoft.EntityFrameworkCore;
using MiniCrm.DbFirst.Data;

var builder = WebApplication.CreateBuilder(args);

// Database First: o banco manda. Não há migrations nem EnsureCreated; o EF só lê e grava dados.
var conexao = builder.Configuration.GetConnectionString("MiniCrm")
    ?? throw new InvalidOperationException("Connection string 'MiniCrm' não encontrada.");
builder.Services.AddDbContext<MiniCrmContext>(options =>
    options.UseMySql(conexao, new MySqlServerVersion(new Version(8, 4, 0))));

builder.Services.AddControllersWithViews()
    // Cliente -> Oportunidades -> Cliente... : sem isso a API entraria em ciclo ao serializar as associações.
    .AddJsonOptions(o => o.JsonSerializerOptions.ReferenceHandler = ReferenceHandler.IgnoreCycles);

builder.Services.AddEndpointsApiExplorer();
builder.Services.AddSwaggerGen();

var app = builder.Build();

if (!app.Environment.IsDevelopment())
{
    app.UseExceptionHandler("/Home/Error");
}

// Documentação da API gerada: http://localhost:8095/swagger
app.UseSwagger();
app.UseSwaggerUI();

app.UseRouting();
app.UseAuthorization();

app.MapStaticAssets();

// A tela inicial é a lista de clientes gerada.
app.MapControllerRoute(
    name: "default",
    pattern: "{controller=Cliente}/{action=Index}/{id?}")
    .WithStaticAssets();

app.Run();
EOF
```

**O que faz:** troca o `Program.cs` do modelo pelo da aplicação. Ele registra o `MiniCrmContext` (`AddDbContext`), diz que o banco é MySQL (`UseMySql`, com a connection string do `appsettings.json`), evita o ciclo Cliente → Oportunidades → Cliente ao serializar a API, liga o Swagger e faz a tela inicial ser a lista de `Cliente`.
**Por que só agora:** esse arquivo usa o `MiniCrmContext`, que só existe depois do passo 6. E o passo 9 **precisa** dele: o gerador de CRUD cria o contexto a partir do `AddDbContext` e, sem ele, falha com `Unable to resolve service for type 'DbContextOptions<MiniCrmContext>'`. Ele falha **mas termina sem erro**, e nenhuma tela é gerada; se isso acontecer, o passo 8 foi pulado.

### Passo 9 — Gerar o CRUD e as telas de cada entidade

O gerador de controllers (`dotnet-aspnet-codegenerator`) só conhece SQL Server, SQLite, Cosmos e Postgres e **se recusa a rodar sem o pacote `Microsoft.EntityFrameworkCore.SqlServer`**, que o passo 4 já instalou. A aplicação **não usa** esse pacote: ela continua falando com o MySQL, e o código gerado nem o menciona.

**9a. Entrar na pasta do projeto:**

```bash
cd MiniCrm.DbFirst
```

**9b. Gerar para todas as entidades, de uma vez** (um único comando para colar no terminal; o gerador compila o projeto sozinho antes de gerar, então a primeira chamada demora um pouco):

```bash
sed -nE 's/^ *public virtual DbSet<([A-Za-z]+)> ([A-Za-z]+) \{ get; set; \}.*/\1 \2/p' Data/MiniCrmContext.cs |
while read entidade colecao; do
  dotnet-aspnet-codegenerator controller -name "${colecao}Controller" -m "$entidade" -dc MiniCrmContext --relativeFolderPath Controllers --useDefaultLayout --referenceScriptLibraries -f >/dev/null </dev/null
  dotnet-aspnet-codegenerator controller -name "${colecao}Controller" -m "$entidade" -dc MiniCrmContext -api --relativeFolderPath Controllers/Api -f >/dev/null </dev/null
  echo "  $entidade -> tela /$colecao  |  API /api/$colecao"
done
```

**O que faz**, em três partes:

1. **`sed ... Data/MiniCrmContext.cs`** lê o `DbContext` que o passo 6 gerou e extrai, de cada linha `public virtual DbSet<Cliente> Cliente { get; set; }`, o par `Cliente Cliente` (entidade e coleção, que têm o mesmo nome por causa do `--no-pluralize`). Como a lista vem do banco, uma tabela nova entra sozinha.
2. **`while read entidade colecao; do ... done`** repete o bloco para cada par.
3. **Dentro do laço**, duas chamadas do gerador:
   - a **primeira** (sem `-api`) cria o controller MVC e as **5 telas** (`Index`, `Details`, `Create`, `Edit`, `Delete`);
   - a **segunda** (`-api`) cria o controller de **API REST**, que o Swagger documenta.

| Opção do gerador | Significado |
|---|---|
| `-name ClienteController` | nome do controller |
| `-m Cliente` | o **model**, a entidade em que ele se baseia |
| `-dc MiniCrmContext` | o **data context**, que fala com o banco |
| `--relativeFolderPath` | pasta onde gravar |
| `--useDefaultLayout` / `--referenceScriptLibraries` | as telas usam o layout do site e a validação nos formulários |
| `-f` | sobrescreve o que já existe |
| `</dev/null` | impede o gerador de "engolir" a lista que o `sed` está entregando ao laço |

**Você deve ver** 9 linhas, uma por entidade:

```
  Cliente -> tela /Cliente  |  API /api/Cliente
  HistoricoEtapa -> tela /HistoricoEtapa  |  API /api/HistoricoEtapa
  ...
  Usuario -> tela /Usuario  |  API /api/Usuario
```

**9c. Voltar para a pasta `dbfirst/`:**

```bash
cd ..
```

Resultado: 9 controllers MVC, 9 de API e 45 views. O `MiniCrm.DbFirst.csproj` não muda.

### Passo 10 — Subir a aplicação

```bash
dotnet run --project MiniCrm.DbFirst --urls http://localhost:8095
```

**O que faz:** compila e sobe o site, conectado ao MySQL do passo 1. O `--urls` fixa a porta 8095 (sem ele, o `dotnet run` usa a porta sorteada no `launchSettings.json` do modelo e tenta abrir o navegador). O ambiente `Development`, que serve o CSS e o Swagger, vem desse mesmo `launchSettings.json`.
**Você deve ver:** `Now listening on: http://localhost:8095`. Deixe esse terminal aberto; `Ctrl+C` encerra.

### Passo 11 — Testar o CRUD no navegador

- **http://localhost:8095** abre a lista de clientes (telas geradas). Use `Create New`, `Edit`, `Details` e `Delete`.
- **http://localhost:8095/swagger** lista os 9 recursos da API com GET, POST, PUT e DELETE. Expanda um endpoint e use `Try it out`.
- As demais telas: `/Oportunidade`, `/Tarefa`, `/Interacao`, `/Usuario`, `/HistoricoEtapa`, `/MotivoPerda`, `/Notificacao` e `/TokenRecuperacao`.

### Começar do zero de novo

É o passo 2: `rm -rf MiniCrm.DbFirst` e repita os **passos 3 a 9**. Como o projeto inteiro é recriado por comandos, ele volta **idêntico** ao que está no repositório, e o `git status` fica limpo.

### O que mostrar: o banco manda

Crie uma tabela direto no MySQL e regenere: a entidade, o CRUD e a tela nascem sozinhos.

```bash
docker exec minicrm-mysql-dbfirst mysql -uroot -prootpw mini_crm_db -e \
  "CREATE TABLE produto (id_produto INT AUTO_INCREMENT PRIMARY KEY, nome VARCHAR(100) NOT NULL, preco DECIMAL(10,2) NOT NULL);
   INSERT INTO produto (nome, preco) VALUES ('Plano Premium', 199.90);"
```

Repita os **passos 6, 7 e 9** (o `sed` do passo 9b descobre a tabela nova sozinho; o projeto e o `Program.cs` não mudam) e o **passo 10**. Surgem `Models/Produto.cs`, `ProdutoController` e `Views/Produto`; a tela fica em `http://localhost:8095/Produto`.

Para voltar ao estado original: `DROP TABLE produto;` no MySQL e refaça **do passo 2 ao 9** (o passo 2 apaga os arquivos de `Produto`; sem apagar, eles ficariam órfãos e o projeto deixaria de compilar).

## Opção secundária: scripts

Os mesmos comandos, agrupados em arquivos, para repetir rápido. Rodam de dentro de `dbfirst/` e fazem o `export` do passo 0 sozinhos (veja `scripts/ambiente.sh`); se o SDK estiver em outro lugar, ajuste `DOTNET_ROOT`.

| Script | O que faz | Equivale a |
|---|---|---|
| `scripts/ambiente.sh` | Não roda sozinho: é carregado pelos outros. Define o `PATH` do .NET, os dados do banco (`DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD`) e a connection string | Passo 0 |
| `scripts/gerar.sh` | **Apaga** só o código gerado, cria um `DbContext` provisório (para o scaffold compilar com o `Program.cs` final), roda o scaffold e conserta o `[MaxLength]` | Passos 6 e 7 |
| `scripts/gerar-crud.sh` | Compila e gera o CRUD das entidades. Se o pacote do SQL Server já estiver no projeto (como agora), não instala nem remove nada | Passo 9 |

Os scripts **partem do projeto já criado** (passos 3 a 5 e 8, que estão no repositório): eles só regeram o que as ferramentas geram.

```bash
cd dbfirst

docker compose up -d --wait   # 1. banco com as 9 tabelas e dados de exemplo (MySQL 8.4, porta 3307)
./scripts/gerar.sh            # 2. entidades e DbContext gerados a partir do banco
./scripts/gerar-crud.sh       # 3. controllers, telas e API gerados a partir das entidades
dotnet run --project MiniCrm.DbFirst --urls http://localhost:8095   # 4. aplicação em http://localhost:8095
```

Se o terminal responder `dotnet: command not found` no último comando, rode antes o `export` do passo 0 (os dois scripts já o fazem sozinhos, mas o `dotnet run` está fora deles).

Os scripts **apagam e regeram tudo a cada execução**, então servem também para "começar do zero" sem digitar os comandos de apagar. O teste de tabela nova funciona igual:

```bash
docker exec minicrm-mysql-dbfirst mysql -uroot -prootpw mini_crm_db -e \
  "CREATE TABLE produto (id_produto INT AUTO_INCREMENT PRIMARY KEY, nome VARCHAR(100) NOT NULL, preco DECIMAL(10,2) NOT NULL);
   INSERT INTO produto (nome, preco) VALUES ('Plano Premium', 199.90);"
./scripts/gerar.sh && ./scripts/gerar-crud.sh   # surgem Models/Produto.cs, ProdutoController e Views/Produto
dotnet run --project MiniCrm.DbFirst --urls http://localhost:8095   # http://localhost:8095/Produto
```

Para voltar ao estado original: `DROP TABLE produto;` e rode `gerar.sh` e `gerar-crud.sh` de novo.

## Banco de dados

`banco/schema.sql` e `banco/dados-demo.sql` são o `mysqldump` do banco criado pelo backend do Mini CRM e populado pela API real (usuários, clientes, interações, oportunidades e tarefas fictícios). O `docker-compose.yml` carrega os dois na primeira subida. O projeto nunca roda migrations nem `EnsureCreated`: o EF só lê e grava dados.

## Limitações e peculiaridades conhecidas

- **Sem segurança.** A API e as telas geradas não têm login nem permissões. Servem para demonstrar a geração; não exponha fora da máquina local. O sistema real é o backend em `../backend`.
- **Sem camada de service nem regras de negócio.** Os controllers gerados usam o `MiniCrmContext` direto. Escopo de carteira, transições do funil e status automático continuam no backend principal.
- **Arquivos estáticos do template.** O `wwwroot/lib` (Bootstrap e jQuery do template MVC) vem sem os `.map` (6 MB de sourcemaps, só úteis para depurar no navegador).
- **Telas genéricas**, em inglês (`Index`, `Create New`) e com os campos na ordem alfabética das colunas.
- **Nomes mecânicos.** A chave `id_vendedor` vira a navegação `IdVendedorNavigation`. Com `--no-pluralize`, a coleção tem o mesmo nome da entidade (`_context.Cliente`, `/Cliente`, `/api/Cliente`), e a lista de clientes dentro de `Usuario` se chama `Cliente`, não `Clientes`. O C# aceita e compila, mas a leitura fica menos clara. Sem a opção, o pluralizador padrão do EF (só inglês) geraria `Interacaos` e `MotivoPerdum`.
- **Tipos.** `bit(1)` (`excluido`) vira `ulong` e `datetime` vira `DateTime`.
- **`[MaxLength(6)]` em campos de data.** Com `--data-annotations`, o Pomelo marca colunas `datetime(6)` com `[MaxLength(6)]`, e o ASP.NET quebra na validação (HTTP 500). O passo 7 (ou o `gerar.sh`) remove só esse atributo.
- **O gerador de controllers não conhece MySQL.** O `dotnet-aspnet-codegenerator` só oferece SQL Server, SQLite, Cosmos e Postgres e exige o pacote do SQL Server no projeto, mesmo com MySQL. Por isso o passo 4 instala o `Microsoft.EntityFrameworkCore.SqlServer` sem a aplicação usá-lo: custa cerca de 50 MB no cache do NuGet e algumas DLLs no `bin/`. O gerador também exige `Microsoft.EntityFrameworkCore.Tools`, instalado no mesmo passo. Quando existir uma versão do gerador que reconheça MySQL, o pacote do SQL Server pode ser removido.
- **O gerador de controllers precisa do `Program.cs` final.** Ele cria o `DbContext` a partir do `AddDbContext` do `Program.cs`. Sem isso, ele imprime `Unable to resolve service for type 'DbContextOptions<MiniCrmContext>'`, **mas termina com código 0** e não gera nada. Por isso o passo 8 vem antes do passo 9.
- **Compilação antes do scaffold.** O scaffold compila o projeto antes de gerar. No passo a passo isso funciona porque o `Program.cs` do modelo ainda não usa o `MiniCrmContext` (o passo 8 o troca depois). Já com o projeto pronto e o código gerado apagado, o `Program.cs` final não compila sem o contexto; por isso o `gerar.sh` cria um `DbContext` provisório antes do scaffold.
- **Fins de linha.** Os arquivos gerados saem com fim de linha CRLF (foi o que observamos, mesmo no Linux) e o projeto os mantém assim, para a geração repetida não gerar diferença nenhuma no Git.
- **Versões.** O provider Pomelo 9.0.0 exige EF Core 9.0.x, então o projeto usa o SDK 9. O Swashbuckle está na 10.3.1 porque a 11.0.0 só suporta .NET 10 ou superior.

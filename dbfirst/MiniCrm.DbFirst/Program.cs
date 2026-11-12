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

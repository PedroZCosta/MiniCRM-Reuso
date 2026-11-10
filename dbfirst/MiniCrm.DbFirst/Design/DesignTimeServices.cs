using Microsoft.EntityFrameworkCore.Design;
using Microsoft.Extensions.DependencyInjection;

namespace MiniCrm.DbFirst.Design;

// Descoberto automaticamente pelo "dotnet ef": só roda na geração do código, nunca na aplicação.
public class DesignTimeServices : IDesignTimeServices
{
    public void ConfigureDesignTimeServices(IServiceCollection services)
        => services.AddSingleton<IPluralizer, PluralizadorPortugues>();
}

/// <summary>
/// O pluralizador padrão do EF só conhece inglês e gera nomes como "MotivoPerdum" e "Interacaos".
/// As tabelas do Mini CRM já são singulares (cliente, interacao, motivo_perda), então só o plural
/// precisa de regra; o singular fica como está.
/// </summary>
public class PluralizadorPortugues : IPluralizer
{
    public string Pluralize(string identifier)
    {
        if (string.IsNullOrEmpty(identifier)) return identifier;
        if (identifier.EndsWith("ao", StringComparison.Ordinal)) return identifier[..^2] + "oes"; // Interacao -> Interacoes
        if (identifier.EndsWith("m", StringComparison.Ordinal)) return identifier[..^1] + "ns";   // item -> itens
        if (identifier.EndsWith("l", StringComparison.Ordinal)) return identifier[..^1] + "is";   // papel -> papeis
        if (identifier.EndsWith("r", StringComparison.Ordinal) || identifier.EndsWith("z", StringComparison.Ordinal))
            return identifier + "es";                                                              // valor -> valores
        if (identifier.EndsWith("s", StringComparison.Ordinal)) return identifier;                 // ja esta no plural
        return identifier + "s";                                                                   // Cliente -> Clientes
    }

    public string Singularize(string identifier) => identifier;
}

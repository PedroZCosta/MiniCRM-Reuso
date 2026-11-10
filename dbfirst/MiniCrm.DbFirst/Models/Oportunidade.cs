using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;
using Microsoft.EntityFrameworkCore;

namespace MiniCrm.DbFirst.Models;

[Table("oportunidade")]
[Index("IdVendedor", Name = "FK7f2s3p199aepg07k8p1t8p4sl")]
[Index("IdMotivoPerda", Name = "FKrxrgun0ityxrxus161q4modsv")]
[Index("IdCliente", Name = "FKtplw02c3tfwkn4qtio5hcpeau")]
public partial class Oportunidade
{
    [Key]
    [Column("id_oportunidade")]
    public int IdOportunidade { get; set; }

    [Column("atualizado_em")]
    public DateTime AtualizadoEm { get; set; }

    [Column("criado_em")]
    public DateTime CriadoEm { get; set; }

    [Column("data_prevista")]
    public DateOnly? DataPrevista { get; set; }

    [Required]
    [Column("etapa", TypeName = "enum('CONTATO','GANHA','PERDIDA','PROPOSTA','PROSPECCAO')")]
    public string Etapa { get; set; }

    [Column("fechada_em")]
    public DateTime? FechadaEm { get; set; }

    [Required]
    [Column("titulo")]
    [StringLength(120)]
    public string Titulo { get; set; }

    [Column("valor_estimado")]
    [Precision(12, 2)]
    public decimal ValorEstimado { get; set; }

    [Column("id_cliente")]
    public int IdCliente { get; set; }

    [Column("id_motivo_perda")]
    public sbyte? IdMotivoPerda { get; set; }

    [Column("id_vendedor")]
    public int IdVendedor { get; set; }

    [InverseProperty("IdOportunidadeNavigation")]
    public virtual ICollection<HistoricoEtapa> HistoricoEtapas { get; set; } = new List<HistoricoEtapa>();

    [ForeignKey("IdCliente")]
    [InverseProperty("Oportunidades")]
    public virtual Cliente IdClienteNavigation { get; set; }

    [ForeignKey("IdMotivoPerda")]
    [InverseProperty("Oportunidades")]
    public virtual MotivoPerda IdMotivoPerdaNavigation { get; set; }

    [ForeignKey("IdVendedor")]
    [InverseProperty("Oportunidades")]
    public virtual Usuario IdVendedorNavigation { get; set; }

    [InverseProperty("IdOportunidadeNavigation")]
    public virtual ICollection<Tarefa> Tarefas { get; set; } = new List<Tarefa>();
}

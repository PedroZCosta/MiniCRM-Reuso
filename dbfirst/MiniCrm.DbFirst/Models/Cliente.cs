using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;
using Microsoft.EntityFrameworkCore;

namespace MiniCrm.DbFirst.Models;

[Table("cliente")]
[Index("IdVendedor", Name = "FKsn714u8ael2eiyov98ub9vvp1")]
public partial class Cliente
{
    [Key]
    [Column("id_cliente")]
    public int IdCliente { get; set; }

    [Column("atualizado_em")]
    public DateTime AtualizadoEm { get; set; }

    [Column("criado_em")]
    public DateTime CriadoEm { get; set; }

    [Required]
    [Column("email")]
    [StringLength(160)]
    public string Email { get; set; }

    [Column("empresa")]
    [StringLength(120)]
    public string Empresa { get; set; }

    [Column("excluido", TypeName = "bit(1)")]
    public ulong Excluido { get; set; }

    [Required]
    [Column("nome")]
    [StringLength(120)]
    public string Nome { get; set; }

    [Required]
    [Column("status", TypeName = "enum('ATIVO','INATIVO','PROSPECT')")]
    public string Status { get; set; }

    [Column("telefone")]
    [StringLength(20)]
    public string Telefone { get; set; }

    [Column("id_vendedor")]
    public int IdVendedor { get; set; }

    [ForeignKey("IdVendedor")]
    [InverseProperty("Clientes")]
    public virtual Usuario IdVendedorNavigation { get; set; }

    [InverseProperty("IdClienteNavigation")]
    public virtual ICollection<Interacao> Interacoes { get; set; } = new List<Interacao>();

    [InverseProperty("IdClienteNavigation")]
    public virtual ICollection<Oportunidade> Oportunidades { get; set; } = new List<Oportunidade>();

    [InverseProperty("IdClienteNavigation")]
    public virtual ICollection<Tarefa> Tarefas { get; set; } = new List<Tarefa>();
}

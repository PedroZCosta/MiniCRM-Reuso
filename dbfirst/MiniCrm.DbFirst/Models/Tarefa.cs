using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;
using Microsoft.EntityFrameworkCore;

namespace MiniCrm.DbFirst.Models;

[Table("tarefa")]
[Index("IdCliente", Name = "FK1anhroi0h6alih0wrb8nuk2q")]
[Index("IdOportunidade", Name = "FKb8bi67xylwpxvpa9bwertaar8")]
[Index("IdUsuario", Name = "FKky19t41n18yhyhw739ocwwofy")]
public partial class Tarefa
{
    [Key]
    [Column("id_tarefa")]
    public int IdTarefa { get; set; }

    [Column("concluida", TypeName = "bit(1)")]
    public ulong Concluida { get; set; }

    [Column("concluida_em")]
    public DateTime? ConcluidaEm { get; set; }

    [Column("criado_em")]
    public DateTime CriadoEm { get; set; }

    [Column("data_vencimento")]
    public DateOnly DataVencimento { get; set; }

    [Column("descricao", TypeName = "text")]
    public string Descricao { get; set; }

    [Required]
    [Column("titulo")]
    [StringLength(120)]
    public string Titulo { get; set; }

    [Column("id_cliente")]
    public int? IdCliente { get; set; }

    [Column("id_oportunidade")]
    public int? IdOportunidade { get; set; }

    [Column("id_usuario")]
    public int IdUsuario { get; set; }

    [ForeignKey("IdCliente")]
    [InverseProperty("Tarefa")]
    public virtual Cliente IdClienteNavigation { get; set; }

    [ForeignKey("IdOportunidade")]
    [InverseProperty("Tarefa")]
    public virtual Oportunidade IdOportunidadeNavigation { get; set; }

    [ForeignKey("IdUsuario")]
    [InverseProperty("Tarefa")]
    public virtual Usuario IdUsuarioNavigation { get; set; }

    [InverseProperty("IdTarefaNavigation")]
    public virtual ICollection<Notificacao> Notificacao { get; set; } = new List<Notificacao>();
}

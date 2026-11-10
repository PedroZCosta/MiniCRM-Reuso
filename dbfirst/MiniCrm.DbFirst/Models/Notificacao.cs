using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;
using Microsoft.EntityFrameworkCore;

namespace MiniCrm.DbFirst.Models;

[Table("notificacao")]
[Index("IdTarefa", Name = "FKdah20j3hws6fyss28na762eju")]
[Index("IdUsuario", Name = "FKlklb6xjbjlatiuvdywow2ig5s")]
public partial class Notificacao
{
    [Key]
    [Column("id_notificacao")]
    public int IdNotificacao { get; set; }

    [Column("gerada_em")]
    public DateTime GeradaEm { get; set; }

    [Column("lida", TypeName = "bit(1)")]
    public ulong Lida { get; set; }

    [Required]
    [Column("mensagem")]
    [StringLength(255)]
    public string Mensagem { get; set; }

    [Required]
    [Column("tipo", TypeName = "enum('D1','D15','HOJE','REUNIAO','VENCIDA')")]
    public string Tipo { get; set; }

    [Column("id_tarefa")]
    public int? IdTarefa { get; set; }

    [Column("id_usuario")]
    public int IdUsuario { get; set; }

    [ForeignKey("IdTarefa")]
    [InverseProperty("Notificacoes")]
    public virtual Tarefa IdTarefaNavigation { get; set; }

    [ForeignKey("IdUsuario")]
    [InverseProperty("Notificacoes")]
    public virtual Usuario IdUsuarioNavigation { get; set; }
}

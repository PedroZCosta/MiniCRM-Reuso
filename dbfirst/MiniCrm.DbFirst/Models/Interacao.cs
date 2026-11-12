using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;
using Microsoft.EntityFrameworkCore;

namespace MiniCrm.DbFirst.Models;

[Table("interacao")]
[Index("IdCliente", Name = "FK6lym9f6oyb2e3ipeme43rtbid")]
[Index("IdUsuario", Name = "FKj6n4fspujfbiwnsagdpgr57q2")]
public partial class Interacao
{
    [Key]
    [Column("id_interacao")]
    public int IdInteracao { get; set; }

    [Column("criado_em")]
    public DateTime CriadoEm { get; set; }

    [Column("data_interacao")]
    public DateTime DataInteracao { get; set; }

    [Column("observacoes", TypeName = "text")]
    public string Observacoes { get; set; }

    [Required]
    [Column("tipo", TypeName = "enum('EMAIL','LIGACAO','REUNIAO')")]
    public string Tipo { get; set; }

    [Column("id_cliente")]
    public int IdCliente { get; set; }

    [Column("id_usuario")]
    public int IdUsuario { get; set; }

    [ForeignKey("IdCliente")]
    [InverseProperty("Interacoes")]
    public virtual Cliente IdClienteNavigation { get; set; }

    [ForeignKey("IdUsuario")]
    [InverseProperty("Interacoes")]
    public virtual Usuario IdUsuarioNavigation { get; set; }
}

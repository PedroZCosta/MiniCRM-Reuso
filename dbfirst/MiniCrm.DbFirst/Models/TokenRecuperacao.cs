using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;
using Microsoft.EntityFrameworkCore;

namespace MiniCrm.DbFirst.Models;

[Table("token_recuperacao")]
[Index("IdUsuario", Name = "FKbi75rsxrvtf5mwgn7hxaw1nth")]
public partial class TokenRecuperacao
{
    [Key]
    [Column("id_token")]
    public int IdToken { get; set; }

    [Required]
    [Column("codigo_hash")]
    [StringLength(255)]
    public string CodigoHash { get; set; }

    [Column("criado_em")]
    public DateTime CriadoEm { get; set; }

    [Column("expira_em")]
    public DateTime ExpiraEm { get; set; }

    [Column("usado", TypeName = "bit(1)")]
    public ulong Usado { get; set; }

    [Column("id_usuario")]
    public int IdUsuario { get; set; }

    [ForeignKey("IdUsuario")]
    [InverseProperty("TokenRecuperacoes")]
    public virtual Usuario IdUsuarioNavigation { get; set; }
}

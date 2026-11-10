using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;
using Microsoft.EntityFrameworkCore;

namespace MiniCrm.DbFirst.Models;

[Table("historico_etapa")]
[Index("IdOportunidade", Name = "FKdumvisdk7v0x7u38ggtc8mn12")]
[Index("IdUsuario", Name = "FKrbxy52xma3ket1t3d532v3x95")]
public partial class HistoricoEtapa
{
    [Key]
    [Column("id_historico")]
    public int IdHistorico { get; set; }

    [Column("alterado_em")]
    public DateTime AlteradoEm { get; set; }

    [Column("etapa_anterior", TypeName = "enum('CONTATO','GANHA','PERDIDA','PROPOSTA','PROSPECCAO')")]
    public string EtapaAnterior { get; set; }

    [Required]
    [Column("etapa_nova", TypeName = "enum('CONTATO','GANHA','PERDIDA','PROPOSTA','PROSPECCAO')")]
    public string EtapaNova { get; set; }

    [Column("id_oportunidade")]
    public int IdOportunidade { get; set; }

    [Column("id_usuario")]
    public int IdUsuario { get; set; }

    [ForeignKey("IdOportunidade")]
    [InverseProperty("HistoricoEtapas")]
    public virtual Oportunidade IdOportunidadeNavigation { get; set; }

    [ForeignKey("IdUsuario")]
    [InverseProperty("HistoricoEtapas")]
    public virtual Usuario IdUsuarioNavigation { get; set; }
}

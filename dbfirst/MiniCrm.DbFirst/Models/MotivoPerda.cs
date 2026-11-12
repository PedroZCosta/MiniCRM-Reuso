using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;
using Microsoft.EntityFrameworkCore;

namespace MiniCrm.DbFirst.Models;

[Table("motivo_perda")]
public partial class MotivoPerda
{
    [Key]
    [Column("id_motivo")]
    public sbyte IdMotivo { get; set; }

    [Required]
    [Column("descricao")]
    [StringLength(60)]
    public string Descricao { get; set; }

    [InverseProperty("IdMotivoPerdaNavigation")]
    public virtual ICollection<Oportunidade> Oportunidade { get; set; } = new List<Oportunidade>();
}

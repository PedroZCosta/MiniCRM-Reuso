using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;
using Microsoft.EntityFrameworkCore;

namespace MiniCrm.DbFirst.Models;

[Table("usuario")]
[Index("IdGerente", Name = "FKnkejpolfup2j1ea206tux8vxa")]
[Index("Email", Name = "UK5171l57faosmj8myawaucatdw", IsUnique = true)]
public partial class Usuario
{
    [Key]
    [Column("id_usuario")]
    public int IdUsuario { get; set; }

    [Column("ativo", TypeName = "bit(1)")]
    public ulong Ativo { get; set; }

    [Column("atualizado_em")]
    public DateTime AtualizadoEm { get; set; }

    [Column("bloqueado_ate")]
    public DateTime? BloqueadoAte { get; set; }

    [Column("criado_em")]
    public DateTime CriadoEm { get; set; }

    [Required]
    [Column("email")]
    [StringLength(160)]
    public string Email { get; set; }

    [Required]
    [Column("nome")]
    [StringLength(120)]
    public string Nome { get; set; }

    [Required]
    [Column("perfil", TypeName = "enum('ADMIN','GERENTE','VENDEDOR')")]
    public string Perfil { get; set; }

    [Required]
    [Column("senha_hash")]
    [StringLength(255)]
    public string SenhaHash { get; set; }

    [Column("tentativas_login")]
    public sbyte TentativasLogin { get; set; }

    [Column("trocar_senha", TypeName = "bit(1)")]
    public ulong TrocarSenha { get; set; }

    [Column("ultimo_acesso")]
    public DateTime? UltimoAcesso { get; set; }

    [Column("id_gerente")]
    public int? IdGerente { get; set; }

    [InverseProperty("IdVendedorNavigation")]
    public virtual ICollection<Cliente> Cliente { get; set; } = new List<Cliente>();

    [InverseProperty("IdUsuarioNavigation")]
    public virtual ICollection<HistoricoEtapa> HistoricoEtapa { get; set; } = new List<HistoricoEtapa>();

    [ForeignKey("IdGerente")]
    [InverseProperty("InverseIdGerenteNavigation")]
    public virtual Usuario IdGerenteNavigation { get; set; }

    [InverseProperty("IdUsuarioNavigation")]
    public virtual ICollection<Interacao> Interacao { get; set; } = new List<Interacao>();

    [InverseProperty("IdGerenteNavigation")]
    public virtual ICollection<Usuario> InverseIdGerenteNavigation { get; set; } = new List<Usuario>();

    [InverseProperty("IdUsuarioNavigation")]
    public virtual ICollection<Notificacao> Notificacao { get; set; } = new List<Notificacao>();

    [InverseProperty("IdVendedorNavigation")]
    public virtual ICollection<Oportunidade> Oportunidade { get; set; } = new List<Oportunidade>();

    [InverseProperty("IdUsuarioNavigation")]
    public virtual ICollection<Tarefa> Tarefa { get; set; } = new List<Tarefa>();

    [InverseProperty("IdUsuarioNavigation")]
    public virtual ICollection<TokenRecuperacao> TokenRecuperacao { get; set; } = new List<TokenRecuperacao>();
}

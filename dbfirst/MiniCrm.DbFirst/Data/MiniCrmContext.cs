using System;
using System.Collections.Generic;
using Microsoft.EntityFrameworkCore;
using MiniCrm.DbFirst.Models;

namespace MiniCrm.DbFirst.Data;

public partial class MiniCrmContext : DbContext
{
    public MiniCrmContext(DbContextOptions<MiniCrmContext> options)
        : base(options)
    {
    }

    public virtual DbSet<Cliente> Clientes { get; set; }

    public virtual DbSet<HistoricoEtapa> HistoricoEtapas { get; set; }

    public virtual DbSet<Interacao> Interacoes { get; set; }

    public virtual DbSet<MotivoPerda> MotivoPerdas { get; set; }

    public virtual DbSet<Notificacao> Notificacoes { get; set; }

    public virtual DbSet<Oportunidade> Oportunidades { get; set; }

    public virtual DbSet<Tarefa> Tarefas { get; set; }

    public virtual DbSet<TokenRecuperacao> TokenRecuperacoes { get; set; }

    public virtual DbSet<Usuario> Usuarios { get; set; }

    protected override void OnModelCreating(ModelBuilder modelBuilder)
    {
        modelBuilder
            .UseCollation("utf8mb4_0900_ai_ci")
            .HasCharSet("utf8mb4");

        modelBuilder.Entity<Cliente>(entity =>
        {
            entity.HasKey(e => e.IdCliente).HasName("PRIMARY");

            entity.HasOne(d => d.IdVendedorNavigation).WithMany(p => p.Clientes)
                .OnDelete(DeleteBehavior.ClientSetNull)
                .HasConstraintName("FKsn714u8ael2eiyov98ub9vvp1");
        });

        modelBuilder.Entity<HistoricoEtapa>(entity =>
        {
            entity.HasKey(e => e.IdHistorico).HasName("PRIMARY");

            entity.HasOne(d => d.IdOportunidadeNavigation).WithMany(p => p.HistoricoEtapas)
                .OnDelete(DeleteBehavior.ClientSetNull)
                .HasConstraintName("FKdumvisdk7v0x7u38ggtc8mn12");

            entity.HasOne(d => d.IdUsuarioNavigation).WithMany(p => p.HistoricoEtapas)
                .OnDelete(DeleteBehavior.ClientSetNull)
                .HasConstraintName("FKrbxy52xma3ket1t3d532v3x95");
        });

        modelBuilder.Entity<Interacao>(entity =>
        {
            entity.HasKey(e => e.IdInteracao).HasName("PRIMARY");

            entity.HasOne(d => d.IdClienteNavigation).WithMany(p => p.Interacoes)
                .OnDelete(DeleteBehavior.ClientSetNull)
                .HasConstraintName("FK6lym9f6oyb2e3ipeme43rtbid");

            entity.HasOne(d => d.IdUsuarioNavigation).WithMany(p => p.Interacoes)
                .OnDelete(DeleteBehavior.ClientSetNull)
                .HasConstraintName("FKj6n4fspujfbiwnsagdpgr57q2");
        });

        modelBuilder.Entity<MotivoPerda>(entity =>
        {
            entity.HasKey(e => e.IdMotivo).HasName("PRIMARY");
        });

        modelBuilder.Entity<Notificacao>(entity =>
        {
            entity.HasKey(e => e.IdNotificacao).HasName("PRIMARY");

            entity.HasOne(d => d.IdTarefaNavigation).WithMany(p => p.Notificacoes).HasConstraintName("FKdah20j3hws6fyss28na762eju");

            entity.HasOne(d => d.IdUsuarioNavigation).WithMany(p => p.Notificacoes)
                .OnDelete(DeleteBehavior.ClientSetNull)
                .HasConstraintName("FKlklb6xjbjlatiuvdywow2ig5s");
        });

        modelBuilder.Entity<Oportunidade>(entity =>
        {
            entity.HasKey(e => e.IdOportunidade).HasName("PRIMARY");

            entity.HasOne(d => d.IdClienteNavigation).WithMany(p => p.Oportunidades)
                .OnDelete(DeleteBehavior.ClientSetNull)
                .HasConstraintName("FKtplw02c3tfwkn4qtio5hcpeau");

            entity.HasOne(d => d.IdMotivoPerdaNavigation).WithMany(p => p.Oportunidades).HasConstraintName("FKrxrgun0ityxrxus161q4modsv");

            entity.HasOne(d => d.IdVendedorNavigation).WithMany(p => p.Oportunidades)
                .OnDelete(DeleteBehavior.ClientSetNull)
                .HasConstraintName("FK7f2s3p199aepg07k8p1t8p4sl");
        });

        modelBuilder.Entity<Tarefa>(entity =>
        {
            entity.HasKey(e => e.IdTarefa).HasName("PRIMARY");

            entity.HasOne(d => d.IdClienteNavigation).WithMany(p => p.Tarefas).HasConstraintName("FK1anhroi0h6alih0wrb8nuk2q");

            entity.HasOne(d => d.IdOportunidadeNavigation).WithMany(p => p.Tarefas).HasConstraintName("FKb8bi67xylwpxvpa9bwertaar8");

            entity.HasOne(d => d.IdUsuarioNavigation).WithMany(p => p.Tarefas)
                .OnDelete(DeleteBehavior.ClientSetNull)
                .HasConstraintName("FKky19t41n18yhyhw739ocwwofy");
        });

        modelBuilder.Entity<TokenRecuperacao>(entity =>
        {
            entity.HasKey(e => e.IdToken).HasName("PRIMARY");

            entity.HasOne(d => d.IdUsuarioNavigation).WithMany(p => p.TokenRecuperacoes)
                .OnDelete(DeleteBehavior.ClientSetNull)
                .HasConstraintName("FKbi75rsxrvtf5mwgn7hxaw1nth");
        });

        modelBuilder.Entity<Usuario>(entity =>
        {
            entity.HasKey(e => e.IdUsuario).HasName("PRIMARY");

            entity.HasOne(d => d.IdGerenteNavigation).WithMany(p => p.InverseIdGerenteNavigation).HasConstraintName("FKnkejpolfup2j1ea206tux8vxa");
        });

        OnModelCreatingPartial(modelBuilder);
    }

    partial void OnModelCreatingPartial(ModelBuilder modelBuilder);
}

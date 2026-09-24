package com.miniCRM.miniCRM.model;

import com.miniCRM.miniCRM.model.enums.EtapaOportunidade;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "historico_etapa")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"oportunidade", "usuario"})
public class HistoricoEtapa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_historico")
    @EqualsAndHashCode.Include
    private Integer idHistorico;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_oportunidade", nullable = false)
    private Oportunidade oportunidade;

    @Enumerated(EnumType.STRING)
    @Column(name = "etapa_anterior", length = 20)
    private EtapaOportunidade etapaAnterior;

    @Enumerated(EnumType.STRING)
    @Column(name = "etapa_nova", nullable = false, length = 20)
    private EtapaOportunidade etapaNova;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @CreationTimestamp
    @Column(name = "alterado_em", nullable = false, updatable = false)
    private LocalDateTime alteradoEm;
}

package com.miniCRM.miniCRM.repository;

import com.miniCRM.miniCRM.model.Oportunidade;
import com.miniCRM.miniCRM.model.enums.EtapaOportunidade;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OportunidadeRepository extends JpaRepository<Oportunidade, Integer> {

    @Query("""
            SELECT o FROM Oportunidade o
            WHERE (:etapa IS NULL OR o.etapa = :etapa)
              AND (:clienteId IS NULL OR o.cliente.idCliente = :clienteId)
            """)
    Page<Oportunidade> buscar(@Param("etapa") EtapaOportunidade etapa,
                              @Param("clienteId") Integer clienteId,
                              Pageable pageable);

    @Query("""
            SELECT o FROM Oportunidade o
            WHERE (:etapa IS NULL OR o.etapa = :etapa)
              AND (:clienteId IS NULL OR o.cliente.idCliente = :clienteId)
              AND o.vendedor.idUsuario IN :vendedores
            """)
    Page<Oportunidade> buscarDosVendedores(@Param("etapa") EtapaOportunidade etapa,
                                           @Param("clienteId") Integer clienteId,
                                           @Param("vendedores") List<Integer> vendedores,
                                           Pageable pageable);

    @Query("""
            SELECT o FROM Oportunidade o
            JOIN FETCH o.cliente
            JOIN FETCH o.vendedor
            LEFT JOIN FETCH o.motivoPerda
            """)
    List<Oportunidade> listarTodas();

    @Query("""
            SELECT o FROM Oportunidade o
            JOIN FETCH o.cliente
            JOIN FETCH o.vendedor
            LEFT JOIN FETCH o.motivoPerda
            WHERE o.vendedor.idUsuario IN :vendedores
            """)
    List<Oportunidade> listarDosVendedores(@Param("vendedores") List<Integer> vendedores);

    // JOIN FETCH o.vendedor acrescentado para o ranking (SPEC-04 §4.2): neutro no result set
    // (vendedor e optional=false, nullable=false - o INNER JOIN nao corta linha nenhuma) e
    // mata o N+1 de o.getVendedor().getNome() fora de transacao.
    @Query("""
            SELECT o FROM Oportunidade o
            JOIN FETCH o.vendedor
            WHERE o.fechadaEm >= :inicio AND o.fechadaEm < :fim
            """)
    List<Oportunidade> fechadasEntre(@Param("inicio") LocalDateTime inicio,
                                     @Param("fim") LocalDateTime fim);

    @Query("""
            SELECT o FROM Oportunidade o
            JOIN FETCH o.vendedor
            WHERE o.fechadaEm >= :inicio AND o.fechadaEm < :fim
              AND o.vendedor.idUsuario IN :vendedores
            """)
    List<Oportunidade> fechadasEntreDosVendedores(@Param("inicio") LocalDateTime inicio,
                                                 @Param("fim") LocalDateTime fim,
                                                 @Param("vendedores") List<Integer> vendedores);

    List<Oportunidade> findByClienteIdClienteOrderByCriadoEmDesc(Integer idCliente);

    /**
     * RF12/RN-06: resultado completo do filtro para o relatorio. RF21: o periodo filtra
     * criadoEm, NAO fechadaEm - fechadaEm e null em toda oportunidade aberta, e filtrar por
     * ele descartaria PROSPECCAO/CONTATO/PROPOSTA e tornaria o filtro ?etapa= inutil.
     */
    @Query("""
            SELECT o FROM Oportunidade o
            JOIN FETCH o.cliente
            JOIN FETCH o.vendedor
            WHERE (:etapa  IS NULL OR o.etapa = :etapa)
              AND (:inicio IS NULL OR o.criadoEm >= :inicio)
              AND (:fim    IS NULL OR o.criadoEm <  :fim)
            ORDER BY o.criadoEm DESC
            """)
    List<Oportunidade> paraRelatorio(@Param("etapa") EtapaOportunidade etapa,
                                     @Param("inicio") LocalDateTime inicio,
                                     @Param("fim") LocalDateTime fim);

    @Query("""
            SELECT o FROM Oportunidade o
            JOIN FETCH o.cliente
            JOIN FETCH o.vendedor
            WHERE (:etapa  IS NULL OR o.etapa = :etapa)
              AND (:inicio IS NULL OR o.criadoEm >= :inicio)
              AND (:fim    IS NULL OR o.criadoEm <  :fim)
              AND o.vendedor.idUsuario IN :vendedores
            ORDER BY o.criadoEm DESC
            """)
    List<Oportunidade> paraRelatorioDosVendedores(@Param("etapa") EtapaOportunidade etapa,
                                                  @Param("inicio") LocalDateTime inicio,
                                                  @Param("fim") LocalDateTime fim,
                                                  @Param("vendedores") List<Integer> vendedores);
}

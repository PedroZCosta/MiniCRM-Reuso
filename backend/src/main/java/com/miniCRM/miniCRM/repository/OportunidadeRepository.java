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

    @Query("""
            SELECT o FROM Oportunidade o
            WHERE o.fechadaEm >= :inicio AND o.fechadaEm < :fim
            """)
    List<Oportunidade> fechadasEntre(@Param("inicio") LocalDateTime inicio,
                                     @Param("fim") LocalDateTime fim);

    @Query("""
            SELECT o FROM Oportunidade o
            WHERE o.fechadaEm >= :inicio AND o.fechadaEm < :fim
              AND o.vendedor.idUsuario IN :vendedores
            """)
    List<Oportunidade> fechadasEntreDosVendedores(@Param("inicio") LocalDateTime inicio,
                                                 @Param("fim") LocalDateTime fim,
                                                 @Param("vendedores") List<Integer> vendedores);

    List<Oportunidade> findByClienteIdClienteOrderByCriadoEmDesc(Integer idCliente);
}

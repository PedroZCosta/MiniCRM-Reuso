package com.miniCRM.miniCRM.repository;

import com.miniCRM.miniCRM.model.Cliente;
import com.miniCRM.miniCRM.model.enums.StatusCliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Integer>, JpaSpecificationExecutor<Cliente> {

    /** RF12/RN-06: resultado completo do filtro para o relatorio, nunca paginado. */
    @Query("""
            SELECT c FROM Cliente c
            JOIN FETCH c.vendedor
            WHERE c.excluido = false
              AND (:status IS NULL OR c.status = :status)
              AND (:inicio IS NULL OR c.criadoEm >= :inicio)
              AND (:fim    IS NULL OR c.criadoEm <  :fim)
            ORDER BY c.nome
            """)
    List<Cliente> paraRelatorio(@Param("status") StatusCliente status,
                                @Param("inicio") LocalDateTime inicio,
                                @Param("fim") LocalDateTime fim);

    @Query("""
            SELECT c FROM Cliente c
            JOIN FETCH c.vendedor
            WHERE c.excluido = false
              AND (:status IS NULL OR c.status = :status)
              AND (:inicio IS NULL OR c.criadoEm >= :inicio)
              AND (:fim    IS NULL OR c.criadoEm <  :fim)
              AND c.vendedor.idUsuario IN :vendedores
            ORDER BY c.nome
            """)
    List<Cliente> paraRelatorioDosVendedores(@Param("status") StatusCliente status,
                                             @Param("inicio") LocalDateTime inicio,
                                             @Param("fim") LocalDateTime fim,
                                             @Param("vendedores") List<Integer> vendedores);
}

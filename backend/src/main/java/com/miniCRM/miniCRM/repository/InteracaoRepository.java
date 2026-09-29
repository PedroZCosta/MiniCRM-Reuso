package com.miniCRM.miniCRM.repository;

import com.miniCRM.miniCRM.model.Interacao;
import com.miniCRM.miniCRM.model.enums.TipoInteracao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface InteracaoRepository extends JpaRepository<Interacao, Integer> {

    Page<Interacao> findByClienteIdClienteOrderByDataInteracaoDesc(Integer idCliente, Pageable pageable);

    List<Interacao> findByClienteIdCliente(Integer idCliente);

    /** Interações de um tipo numa janela, de cliente não excluído e usuário ativo (RF33). */
    @Query("""
            SELECT i FROM Interacao i
            JOIN FETCH i.cliente c
            JOIN FETCH i.usuario u
            WHERE i.tipo = :tipo
              AND i.dataInteracao BETWEEN :inicio AND :fim
              AND c.excluido = false
              AND u.ativo = true
            """)
    List<Interacao> agendadasEntre(@Param("tipo") TipoInteracao tipo,
                                   @Param("inicio") LocalDateTime inicio,
                                   @Param("fim") LocalDateTime fim);
}

package com.miniCRM.miniCRM.repository;

import com.miniCRM.miniCRM.model.Tarefa;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TarefaRepository extends JpaRepository<Tarefa, Integer> {

    @Query("""
            SELECT t FROM Tarefa t
            WHERE (:clienteId IS NULL OR t.cliente.idCliente = :clienteId)
              AND (:concluida IS NULL OR t.concluida = :concluida)
              AND (:de IS NULL OR t.dataVencimento >= :de)
              AND (:ate IS NULL OR t.dataVencimento <= :ate)
            """)
    Page<Tarefa> buscar(@Param("clienteId") Integer clienteId,
                        @Param("concluida") Boolean concluida,
                        @Param("de") LocalDate de,
                        @Param("ate") LocalDate ate,
                        Pageable pageable);

    @Query("""
            SELECT t FROM Tarefa t
            WHERE (:clienteId IS NULL OR t.cliente.idCliente = :clienteId)
              AND (:concluida IS NULL OR t.concluida = :concluida)
              AND (:de IS NULL OR t.dataVencimento >= :de)
              AND (:ate IS NULL OR t.dataVencimento <= :ate)
              AND t.usuario.idUsuario IN :vendedores
            """)
    Page<Tarefa> buscarDosVendedores(@Param("clienteId") Integer clienteId,
                                     @Param("concluida") Boolean concluida,
                                     @Param("de") LocalDate de,
                                     @Param("ate") LocalDate ate,
                                     @Param("vendedores") List<Integer> vendedores,
                                     Pageable pageable);

    /**
     * Job §3.1: candidatas a notificacao do dia. Sem EscopoCarteira - o job roda fora de
     * requisicao HTTP. JOIN FETCH obrigatorio: o job nao e transacional e open-in-view=false.
     * u.ativo = true atende o RF33 (usuario desativado nao acumula notificacao).
     */
    @Query("""
            SELECT t FROM Tarefa t
            JOIN FETCH t.usuario u
            WHERE t.concluida = false
              AND u.ativo = true
              AND (t.dataVencimento < :hoje OR t.dataVencimento IN :datas)
            """)
    List<Tarefa> candidatasANotificar(@Param("hoje") LocalDate hoje,
                                      @Param("datas") List<LocalDate> datas);

    /** Card do dashboard (RF13): contagem de tarefa aberta numa janela de vencimento. */
    @Query("""
            SELECT COUNT(t) FROM Tarefa t
            WHERE t.concluida = false
              AND (:de IS NULL OR t.dataVencimento >= :de)
              AND (:ate IS NULL OR t.dataVencimento <= :ate)
            """)
    long contarAbertasNaJanela(@Param("de") LocalDate de, @Param("ate") LocalDate ate);

    @Query("""
            SELECT COUNT(t) FROM Tarefa t
            WHERE t.concluida = false
              AND (:de IS NULL OR t.dataVencimento >= :de)
              AND (:ate IS NULL OR t.dataVencimento <= :ate)
              AND t.usuario.idUsuario IN :vendedores
            """)
    long contarAbertasNaJanelaDosVendedores(@Param("de") LocalDate de,
                                            @Param("ate") LocalDate ate,
                                            @Param("vendedores") List<Integer> vendedores);

    /** RF16: timeline do cliente (contrato TarefaConsultaService). */
    @Query("""
            SELECT t FROM Tarefa t
            JOIN FETCH t.usuario
            WHERE t.cliente.idCliente = :idCliente
            ORDER BY t.dataVencimento DESC
            """)
    List<Tarefa> listarDoCliente(@Param("idCliente") Integer idCliente);
}

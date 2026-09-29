package com.miniCRM.miniCRM.repository;

import com.miniCRM.miniCRM.model.Notificacao;
import com.miniCRM.miniCRM.model.enums.TipoNotificacao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificacaoRepository extends JpaRepository<Notificacao, Integer> {

    @Query("""
            SELECT n FROM Notificacao n
            WHERE n.usuario.idUsuario = :idUsuario
              AND (:lida IS NULL OR n.lida = :lida)
            """)
    Page<Notificacao> buscarDoUsuario(@Param("idUsuario") Integer idUsuario,
                                      @Param("lida") Boolean lida,
                                      Pageable pageable);

    long countByUsuarioIdUsuarioAndLidaFalse(Integer idUsuario);

    @Modifying
    @Query("""
            UPDATE Notificacao n SET n.lida = true
            WHERE n.usuario.idUsuario = :idUsuario AND n.lida = false
            """)
    int marcarTodasComoLidas(@Param("idUsuario") Integer idUsuario);

    /** RN-04: unicidade logica (tarefa, faixa, usuario), verificada ANTES do insert. */
    boolean existsByTarefaIdTarefaAndTipoAndUsuarioIdUsuario(Integer idTarefa,
                                                             TipoNotificacao tipo,
                                                             Integer idUsuario);

    /** RN-04 para REUNIAO, que nao tem tarefa: a mensagem (cliente + data) e a chave natural. */
    boolean existsByUsuarioIdUsuarioAndTipoAndTarefaIsNullAndMensagem(Integer idUsuario,
                                                                      TipoNotificacao tipo,
                                                                      String mensagem);
}

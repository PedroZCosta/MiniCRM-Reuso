package com.miniCRM.miniCRM.service;

import com.miniCRM.miniCRM.dto.comum.PageResponse;
import com.miniCRM.miniCRM.dto.notificacao.ContadorNotificacoesResponse;
import com.miniCRM.miniCRM.dto.notificacao.NotificacaoResponse;
import com.miniCRM.miniCRM.exception.RecursoNaoEncontradoException;
import com.miniCRM.miniCRM.model.Notificacao;
import com.miniCRM.miniCRM.model.Usuario;
import com.miniCRM.miniCRM.repository.NotificacaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * RF23: notificacoes internas do sistema, sem e-mail.
 *
 * <p>Nao usa EscopoCarteira de proposito: notificacao e do usuario autenticado e ponto -
 * nem gerente enxerga a do proprio vendedor. Por isso todo metodo recebe o Usuario logado.
 */
@Service
@Transactional
@RequiredArgsConstructor
public class NotificacaoService {

    private static final int TAMANHO_PAGINA = 20;

    private final NotificacaoRepository notificacaoRepository;

    @Transactional(readOnly = true)
    public PageResponse<NotificacaoResponse> listar(Usuario logado, Boolean lida, int page) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), TAMANHO_PAGINA,
                Sort.by("geradaEm").descending());

        return PageResponse.de(notificacaoRepository
                .buscarDoUsuario(logado.getIdUsuario(), lida, pageable)
                .map(NotificacaoResponse::de));
    }

    @Transactional(readOnly = true)
    public ContadorNotificacoesResponse contador(Usuario logado) {
        return new ContadorNotificacoesResponse(
                notificacaoRepository.countByUsuarioIdUsuarioAndLidaFalse(logado.getIdUsuario()));
    }

    /** Notificacao de outro usuario da 404, nunca 403: nao se revela que ela existe (RF23). */
    public NotificacaoResponse marcarComoLida(Integer idNotificacao, Usuario logado) {
        Notificacao notificacao = notificacaoRepository.findById(idNotificacao)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Notificação não encontrada"));

        if (!notificacao.getUsuario().getIdUsuario().equals(logado.getIdUsuario())) {
            throw new RecursoNaoEncontradoException("Notificação não encontrada");
        }

        notificacao.setLida(true);
        return NotificacaoResponse.de(notificacaoRepository.save(notificacao));
    }

    /** RF23 "Marcar todas como lidas". Devolve o contador zerado para o badge do front. */
    public ContadorNotificacoesResponse marcarTodas(Usuario logado) {
        notificacaoRepository.marcarTodasComoLidas(logado.getIdUsuario());
        return new ContadorNotificacoesResponse(0);
    }
}

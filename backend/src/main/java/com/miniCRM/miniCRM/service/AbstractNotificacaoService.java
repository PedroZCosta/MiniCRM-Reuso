package com.miniCRM.miniCRM.service;

import com.miniCRM.miniCRM.event.TarefaVencendoEvent;
import com.miniCRM.miniCRM.model.Notificacao;
import com.miniCRM.miniCRM.repository.NotificacaoRepository;
import com.miniCRM.miniCRM.repository.UsuarioRepository;

/**
 * Factory Method (SPEC-04 §3.2): {@link #gerar} é o esqueleto fixo e {@link #instanciar} é o
 * factory method, implementado só pelos tipos de notificação cuja criação difere (tarefa e reunião).
 *
 * <p>Não anotar as concretas com {@code @Transactional}: {@code gerar} é final e o proxy do Spring
 * não intercepta método final. A transação é aberta pelo NotificacaoProcessador.
 */
public abstract class AbstractNotificacaoService {

    protected final NotificacaoRepository notificacaoRepository;
    private final UsuarioRepository usuarioRepository;

    protected AbstractNotificacaoService(NotificacaoRepository notificacaoRepository,
                                         UsuarioRepository usuarioRepository) {
        this.notificacaoRepository = notificacaoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    /** RN-04: devolve false, sem gravar, quando a notificação já existe. */
    public final boolean gerar(TarefaVencendoEvent evento) {
        Notificacao notificacao = instanciar(evento);
        notificacao.setTipo(evento.faixa());
        notificacao.setLida(false);                     // RN-05

        if (jaExiste(evento, notificacao.getMensagem())) {
            return false;
        }

        notificacao.setUsuario(usuarioRepository.getReferenceById(evento.idUsuario()));
        vincular(notificacao, evento);
        notificacaoRepository.save(notificacao);
        return true;
    }

    /** O factory method: monta a notificação com a mensagem do tipo. */
    protected abstract Notificacao instanciar(TarefaVencendoEvent evento);

    /** Regra de idempotência de cada tipo (RN-04). */
    protected abstract boolean jaExiste(TarefaVencendoEvent evento, String mensagem);

    /** Gancho opcional para ligar a notificação a outra entidade (a tarefa, por exemplo). */
    protected void vincular(Notificacao notificacao, TarefaVencendoEvent evento) {
    }
}

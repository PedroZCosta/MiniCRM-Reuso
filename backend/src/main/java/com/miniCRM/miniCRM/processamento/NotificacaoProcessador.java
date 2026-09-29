package com.miniCRM.miniCRM.processamento;

import com.miniCRM.miniCRM.event.TarefaVencendoEvent;
import com.miniCRM.miniCRM.model.enums.TipoNotificacao;
import com.miniCRM.miniCRM.service.AbstractNotificacaoService;
import com.miniCRM.miniCRM.service.NotificacaoReuniaoService;
import com.miniCRM.miniCRM.service.NotificacaoTarefaService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Observer (lado consumidor) da SPEC-04 §3: recebe o TarefaVencendoEvent publicado pelos services
 * e pede ao service do tipo certo para gerar a notificação.
 *
 * <p>Não trocar por {@code @TransactionalEventListener(AFTER_COMMIT)}: o publicador é o job, fora
 * de transação, e esse listener nunca seria chamado.
 */
@Component
@RequiredArgsConstructor
public class NotificacaoProcessador {

    private final NotificacaoTarefaService notificacaoTarefaService;
    private final NotificacaoReuniaoService notificacaoReuniaoService;

    @EventListener
    @Transactional
    public void aoVencerTarefa(TarefaVencendoEvent evento) {
        AbstractNotificacaoService service = evento.faixa() == TipoNotificacao.REUNIAO
                ? notificacaoReuniaoService
                : notificacaoTarefaService;
        service.gerar(evento);
    }
}

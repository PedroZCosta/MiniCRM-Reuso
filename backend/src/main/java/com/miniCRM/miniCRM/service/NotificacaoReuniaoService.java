package com.miniCRM.miniCRM.service;

import com.miniCRM.miniCRM.dto.cliente.ReuniaoAgendada;
import com.miniCRM.miniCRM.event.TarefaVencendoEvent;
import com.miniCRM.miniCRM.model.Notificacao;
import com.miniCRM.miniCRM.model.enums.FaixaTarefa;
import com.miniCRM.miniCRM.model.enums.TipoNotificacao;
import com.miniCRM.miniCRM.repository.NotificacaoRepository;
import com.miniCRM.miniCRM.repository.UsuarioRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Notificações de reunião agendada (RF24, UC07/UC12). Reunião não tem tarefa, então o evento
 * usa a convenção do TarefaVencendoEvent: {@code idTarefa = null}, {@code tituloTarefa = nome do
 * cliente} e {@code dataVencimento = dia da reunião}. Por isso a mensagem (cliente + dia) é a
 * chave natural da idempotência (RN-04).
 */
@Service
public class NotificacaoReuniaoService extends AbstractNotificacaoService {

    private static final String PREFIXO = "Você tem uma reunião agendada com ";
    private static final DateTimeFormatter DATA_BR = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final InteracaoService interacaoService;
    private final ApplicationEventPublisher publisher;

    public NotificacaoReuniaoService(NotificacaoRepository notificacaoRepository,
                                     UsuarioRepository usuarioRepository,
                                     InteracaoService interacaoService,
                                     ApplicationEventPublisher publisher) {
        super(notificacaoRepository, usuarioRepository);
        this.interacaoService = interacaoService;
        this.publisher = publisher;
    }

    /** Publica um evento por reunião marcada nos próximos 15 dias. */
    public int varrer(LocalDate hoje) {
        LocalDateTime inicio = hoje.atStartOfDay();
        LocalDateTime fim = hoje.plusDays(FaixaTarefa.DIAS_ANTECEDENCIA).atTime(LocalTime.MAX);

        List<ReuniaoAgendada> reunioes = interacaoService.reunioesAgendadasEntre(inicio, fim);
        reunioes.forEach(reuniao -> publisher.publishEvent(new TarefaVencendoEvent(
                null, reuniao.idUsuario(), reuniao.cliente(),
                TipoNotificacao.REUNIAO, reuniao.dataInteracao().toLocalDate())));
        return reunioes.size();
    }

    @Override
    protected Notificacao instanciar(TarefaVencendoEvent evento) {
        Notificacao notificacao = new Notificacao();
        notificacao.setMensagem(PREFIXO + evento.tituloTarefa() + " em " + DATA_BR.format(evento.dataVencimento()));
        return notificacao;
    }

    @Override
    protected boolean jaExiste(TarefaVencendoEvent evento, String mensagem) {
        return notificacaoRepository.existsByUsuarioIdUsuarioAndTipoAndTarefaIsNullAndMensagem(
                evento.idUsuario(), evento.faixa(), mensagem);
    }
}

package com.miniCRM.miniCRM.service;

import com.miniCRM.miniCRM.event.TarefaVencendoEvent;
import com.miniCRM.miniCRM.exception.RegraNegocioException;
import com.miniCRM.miniCRM.model.Notificacao;
import com.miniCRM.miniCRM.model.Tarefa;
import com.miniCRM.miniCRM.model.enums.FaixaTarefa;
import com.miniCRM.miniCRM.model.enums.TipoNotificacao;
import com.miniCRM.miniCRM.repository.NotificacaoRepository;
import com.miniCRM.miniCRM.repository.TarefaRepository;
import com.miniCRM.miniCRM.repository.UsuarioRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/** Notificações de tarefa (RF24): faixas D15, D1, HOJE e VENCIDA. */
@Service
public class NotificacaoTarefaService extends AbstractNotificacaoService {

    private final TarefaRepository tarefaRepository;
    private final ApplicationEventPublisher publisher;

    public NotificacaoTarefaService(NotificacaoRepository notificacaoRepository,
                                    UsuarioRepository usuarioRepository,
                                    TarefaRepository tarefaRepository,
                                    ApplicationEventPublisher publisher) {
        super(notificacaoRepository, usuarioRepository);
        this.tarefaRepository = tarefaRepository;
        this.publisher = publisher;
    }

    /**
     * Publica um evento por tarefa que cai numa faixa. Sem EscopoCarteira: o job roda fora de
     * requisição HTTP, e quem recebe é o dono da tarefa. Recebe "hoje" para o teste fixar a data.
     */
    public int varrer(LocalDate hoje) {
        List<LocalDate> datas = List.of(hoje, hoje.plusDays(1), hoje.plusDays(FaixaTarefa.DIAS_ANTECEDENCIA));
        int publicados = 0;

        for (Tarefa tarefa : tarefaRepository.candidatasANotificar(hoje, datas)) {
            FaixaTarefa faixa = FaixaTarefa.classificarParaNotificacao(tarefa.getDataVencimento(), hoje);
            if (faixa == null) {
                continue;
            }
            publisher.publishEvent(new TarefaVencendoEvent(
                    tarefa.getIdTarefa(), tarefa.getUsuario().getIdUsuario(), tarefa.getTitulo(),
                    faixa.tipoNotificacao(), tarefa.getDataVencimento()));
            publicados++;
        }
        return publicados;
    }

    @Override
    protected Notificacao instanciar(TarefaVencendoEvent evento) {
        Notificacao notificacao = new Notificacao();
        notificacao.setMensagem(prefixo(evento.faixa()) + evento.tituloTarefa());
        return notificacao;
    }

    @Override
    protected boolean jaExiste(TarefaVencendoEvent evento, String mensagem) {
        return notificacaoRepository.existsByTarefaIdTarefaAndTipoAndUsuarioIdUsuario(
                evento.idTarefa(), evento.faixa(), evento.idUsuario());
    }

    @Override
    protected void vincular(Notificacao notificacao, TarefaVencendoEvent evento) {
        notificacao.setTarefa(tarefaRepository.getReferenceById(evento.idTarefa()));
    }

    private String prefixo(TipoNotificacao faixa) {
        return switch (faixa) {
            case D15 -> "Sua atividade vence em 15 dias: ";
            case D1 -> "Sua atividade vai vencer amanhã: ";
            case HOJE -> "Sua atividade vence hoje: ";
            case VENCIDA -> "Sua atividade está atrasada: ";
            case REUNIAO -> throw new RegraNegocioException("A faixa REUNIAO não pertence a uma tarefa");
        };
    }
}

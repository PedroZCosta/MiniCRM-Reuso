package com.miniCRM.miniCRM.service;

import com.miniCRM.miniCRM.dto.cliente.ReuniaoAgendada;
import com.miniCRM.miniCRM.event.TarefaVencendoEvent;
import com.miniCRM.miniCRM.model.Notificacao;
import com.miniCRM.miniCRM.model.Usuario;
import com.miniCRM.miniCRM.model.enums.TipoNotificacao;
import com.miniCRM.miniCRM.repository.NotificacaoRepository;
import com.miniCRM.miniCRM.repository.UsuarioRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificacaoReuniaoServiceTest {

    private static final String MENSAGEM = "Você tem uma reunião agendada com Construtora Horizonte em 20/09/2026";

    @Mock
    private NotificacaoRepository notificacaoRepository;
    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private InteracaoService interacaoService;
    @Mock
    private ApplicationEventPublisher publisher;

    @InjectMocks
    private NotificacaoReuniaoService service;

    private final TarefaVencendoEvent evento = new TarefaVencendoEvent(
            null, 1, "Construtora Horizonte", TipoNotificacao.REUNIAO, LocalDate.of(2026, 9, 20));

    @Test
    @DisplayName("gera mensagem com cliente e data, sem tarefa, e nasce nao lida")
    void geraMensagemComClienteEData() {
        when(notificacaoRepository.existsByUsuarioIdUsuarioAndTipoAndTarefaIsNullAndMensagem(
                1, TipoNotificacao.REUNIAO, MENSAGEM)).thenReturn(false);
        when(usuarioRepository.getReferenceById(1)).thenReturn(new Usuario());

        service.gerar(evento);

        ArgumentCaptor<Notificacao> captor = ArgumentCaptor.forClass(Notificacao.class);
        verify(notificacaoRepository).save(captor.capture());
        assertThat(captor.getValue().getTipo()).isEqualTo(TipoNotificacao.REUNIAO);
        assertThat(captor.getValue().getMensagem()).isEqualTo(MENSAGEM);
        assertThat(captor.getValue().getLida()).isFalse();
        assertThat(captor.getValue().getTarefa()).isNull();
    }

    @Test
    @DisplayName("usa a mensagem como chave de idempotencia e nao consulta por tarefa")
    void usaAMensagemComoChaveDeIdempotencia() {
        when(notificacaoRepository.existsByUsuarioIdUsuarioAndTipoAndTarefaIsNullAndMensagem(
                eq(1), eq(TipoNotificacao.REUNIAO), eq(MENSAGEM))).thenReturn(true);

        boolean gerou = service.gerar(evento);

        assertThat(gerou).isFalse();
        verify(notificacaoRepository, never()).save(any());
        verify(notificacaoRepository, never())
                .existsByTarefaIdTarefaAndTipoAndUsuarioIdUsuario(any(), any(), any());
    }

    @Test
    @DisplayName("varredura publica evento de reuniao com cliente e data")
    void varreduraPublicaEventoDeReuniaoComClienteEData() {
        when(interacaoService.reunioesAgendadasEntre(any(), any())).thenReturn(List.of(
                new ReuniaoAgendada(1, "Construtora Horizonte", LocalDateTime.of(2026, 9, 20, 14, 0))));

        int publicados = service.varrer(LocalDate.of(2026, 9, 10));

        ArgumentCaptor<TarefaVencendoEvent> captor = ArgumentCaptor.forClass(TarefaVencendoEvent.class);
        verify(publisher).publishEvent(captor.capture());
        assertThat(publicados).isEqualTo(1);
        assertThat(captor.getValue().faixa()).isEqualTo(TipoNotificacao.REUNIAO);
        assertThat(captor.getValue().tituloTarefa()).isEqualTo("Construtora Horizonte");
        assertThat(captor.getValue().dataVencimento()).isEqualTo(LocalDate.of(2026, 9, 20));
        assertThat(captor.getValue().idTarefa()).isNull();
    }

    @Test
    @DisplayName("varredura sem reunioes nao publica nada")
    void varreduraSemReunioesNaoPublicaNada() {
        when(interacaoService.reunioesAgendadasEntre(any(), any())).thenReturn(List.of());

        assertThat(service.varrer(LocalDate.now())).isZero();
        verify(publisher, never()).publishEvent(any());
    }
}

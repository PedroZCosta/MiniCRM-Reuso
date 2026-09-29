package com.miniCRM.miniCRM.service;

import com.miniCRM.miniCRM.event.TarefaVencendoEvent;
import com.miniCRM.miniCRM.exception.RegraNegocioException;
import com.miniCRM.miniCRM.model.Notificacao;
import com.miniCRM.miniCRM.model.Tarefa;
import com.miniCRM.miniCRM.model.Usuario;
import com.miniCRM.miniCRM.model.enums.TipoNotificacao;
import com.miniCRM.miniCRM.repository.NotificacaoRepository;
import com.miniCRM.miniCRM.repository.TarefaRepository;
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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** RF24 e RN-04: mensagem de cada faixa e idempotência das notificações de tarefa. */
@ExtendWith(MockitoExtension.class)
class NotificacaoTarefaServiceTest {

    @Mock
    private NotificacaoRepository notificacaoRepository;
    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private TarefaRepository tarefaRepository;
    @Mock
    private ApplicationEventPublisher publisher;

    @InjectMocks
    private NotificacaoTarefaService service;

    private TarefaVencendoEvent evento(TipoNotificacao faixa) {
        return new TarefaVencendoEvent(10, 1, "Ligar para cliente", faixa, LocalDate.of(2026, 9, 11));
    }

    private Notificacao gerarEObter(TarefaVencendoEvent evento) {
        when(notificacaoRepository.existsByTarefaIdTarefaAndTipoAndUsuarioIdUsuario(10, evento.faixa(), 1))
                .thenReturn(false);
        when(usuarioRepository.getReferenceById(1)).thenReturn(new Usuario());
        when(tarefaRepository.getReferenceById(10)).thenReturn(new Tarefa());

        service.gerar(evento);

        ArgumentCaptor<Notificacao> captor = ArgumentCaptor.forClass(Notificacao.class);
        verify(notificacaoRepository).save(captor.capture());
        return captor.getValue();
    }

    @Test
    @DisplayName("d15 gera mensagem de quinze dias e nasce nao lida")
    void d15GeraMensagemDeQuinzeDias() {
        Notificacao n = gerarEObter(evento(TipoNotificacao.D15));

        assertThat(n.getTipo()).isEqualTo(TipoNotificacao.D15);
        assertThat(n.getMensagem()).isEqualTo("Sua atividade vence em 15 dias: Ligar para cliente");
        assertThat(n.getLida()).isFalse();
    }

    @Test
    @DisplayName("d1 gera mensagem de amanha")
    void d1GeraMensagemDeAmanha() {
        Notificacao n = gerarEObter(evento(TipoNotificacao.D1));

        assertThat(n.getMensagem()).isEqualTo("Sua atividade vai vencer amanhã: Ligar para cliente");
    }

    @Test
    @DisplayName("hoje gera mensagem de vencimento hoje")
    void hojeGeraMensagemDeVencimentoHoje() {
        Notificacao n = gerarEObter(evento(TipoNotificacao.HOJE));

        assertThat(n.getMensagem()).isEqualTo("Sua atividade vence hoje: Ligar para cliente");
    }

    @Test
    @DisplayName("vencida gera mensagem de atraso")
    void vencidaGeraMensagemDeAtraso() {
        Notificacao n = gerarEObter(evento(TipoNotificacao.VENCIDA));

        assertThat(n.getMensagem()).isEqualTo("Sua atividade está atrasada: Ligar para cliente");
    }

    @Test
    @DisplayName("notificacao fica ligada ao usuario e a tarefa")
    void notificacaoFicaLigadaAoUsuarioEATarefa() {
        Usuario usuario = new Usuario();
        Tarefa tarefa = new Tarefa();
        when(notificacaoRepository.existsByTarefaIdTarefaAndTipoAndUsuarioIdUsuario(10, TipoNotificacao.D1, 1))
                .thenReturn(false);
        when(usuarioRepository.getReferenceById(1)).thenReturn(usuario);
        when(tarefaRepository.getReferenceById(10)).thenReturn(tarefa);

        service.gerar(evento(TipoNotificacao.D1));

        ArgumentCaptor<Notificacao> captor = ArgumentCaptor.forClass(Notificacao.class);
        verify(notificacaoRepository).save(captor.capture());
        assertThat(captor.getValue().getUsuario()).isSameAs(usuario);
        assertThat(captor.getValue().getTarefa()).isSameAs(tarefa);
    }

    @Test
    @DisplayName("segunda execucao com a mesma faixa nao duplica a notificacao")
    void segundaExecucaoNaoDuplicaNotificacaoDaMesmaFaixa() {
        TarefaVencendoEvent evento = evento(TipoNotificacao.D1);
        when(notificacaoRepository.existsByTarefaIdTarefaAndTipoAndUsuarioIdUsuario(10, TipoNotificacao.D1, 1))
                .thenReturn(false, true);
        when(usuarioRepository.getReferenceById(1)).thenReturn(new Usuario());
        when(tarefaRepository.getReferenceById(10)).thenReturn(new Tarefa());

        boolean primeira = service.gerar(evento);
        boolean segunda = service.gerar(evento);

        assertThat(primeira).isTrue();
        assertThat(segunda).isFalse();
        verify(notificacaoRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("faixa de reuniao nao e aceita pelo service de tarefa")
    void faixaDeReuniaoNaoEAceitaPeloServiceDeTarefa() {
        assertThatThrownBy(() -> service.gerar(evento(TipoNotificacao.REUNIAO)))
                .isInstanceOf(RegraNegocioException.class);
    }

    private Tarefa tarefaComVencimento(Integer idTarefa, Integer idUsuario, LocalDate vencimento) {
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(idUsuario);

        Tarefa tarefa = new Tarefa();
        tarefa.setIdTarefa(idTarefa);
        tarefa.setTitulo("Tarefa " + idTarefa);
        tarefa.setDataVencimento(vencimento);
        tarefa.setUsuario(usuario);
        return tarefa;
    }

    @Test
    @DisplayName("varredura publica um evento por faixa correta")
    void varreduraPublicaUmEventoPorFaixaCorreta() {
        LocalDate hoje = LocalDate.of(2026, 9, 10);
        when(tarefaRepository.candidatasANotificar(any(), any())).thenReturn(List.of(
                tarefaComVencimento(1, 10, hoje.minusDays(3)),   // VENCIDA
                tarefaComVencimento(2, 10, hoje),                // HOJE
                tarefaComVencimento(3, 10, hoje.plusDays(1)),    // D1
                tarefaComVencimento(4, 10, hoje.plusDays(15))    // D15
        ));

        int publicados = service.varrer(hoje);

        ArgumentCaptor<TarefaVencendoEvent> captor = ArgumentCaptor.forClass(TarefaVencendoEvent.class);
        verify(publisher, times(4)).publishEvent(captor.capture());
        assertThat(publicados).isEqualTo(4);
        assertThat(captor.getAllValues().stream().map(TarefaVencendoEvent::faixa).toList())
                .containsExactlyInAnyOrder(
                        TipoNotificacao.VENCIDA, TipoNotificacao.HOJE, TipoNotificacao.D1, TipoNotificacao.D15);
    }

    @Test
    @DisplayName("varredura ignora tarefa fora das quatro faixas")
    void varreduraIgnoraTarefaForaDasFaixas() {
        LocalDate hoje = LocalDate.of(2026, 9, 10);
        when(tarefaRepository.candidatasANotificar(any(), any()))
                .thenReturn(List.of(tarefaComVencimento(1, 10, hoje.plusDays(7))));

        int publicados = service.varrer(hoje);

        assertThat(publicados).isZero();
        verify(publisher, times(0)).publishEvent(any());
    }
}

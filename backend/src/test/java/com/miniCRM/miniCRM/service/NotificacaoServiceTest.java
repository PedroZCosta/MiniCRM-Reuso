package com.miniCRM.miniCRM.service;

import com.miniCRM.miniCRM.dto.notificacao.ContadorNotificacoesResponse;
import com.miniCRM.miniCRM.dto.notificacao.NotificacaoResponse;
import com.miniCRM.miniCRM.exception.RecursoNaoEncontradoException;
import com.miniCRM.miniCRM.model.Notificacao;
import com.miniCRM.miniCRM.model.Tarefa;
import com.miniCRM.miniCRM.model.Usuario;
import com.miniCRM.miniCRM.model.enums.TipoNotificacao;
import com.miniCRM.miniCRM.repository.NotificacaoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificacaoServiceTest {

    @Mock
    private NotificacaoRepository notificacaoRepository;

    @InjectMocks
    private NotificacaoService notificacaoService;

    private Usuario logado;

    private Usuario usuario(Integer id) {
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(id);
        return usuario;
    }

    @Test
    @DisplayName("listagem usa sempre o id do usuario autenticado")
    void listagemUsaSempreOIdDoUsuarioAutenticado() {
        logado = usuario(3);
        ArgumentCaptor<Integer> idCaptor = ArgumentCaptor.forClass(Integer.class);
        when(notificacaoRepository.buscarDoUsuario(idCaptor.capture(), any(), any()))
                .thenReturn(new PageImpl<>(List.of()));

        notificacaoService.listar(logado, null, 0);

        assertThat(idCaptor.getValue()).isEqualTo(3);
    }

    @Test
    @DisplayName("contador conta somente as nao lidas")
    void contadorSomenteNaoLidas() {
        logado = usuario(3);
        when(notificacaoRepository.countByUsuarioIdUsuarioAndLidaFalse(3)).thenReturn(4L);

        ContadorNotificacoesResponse contador = notificacaoService.contador(logado);

        assertThat(contador.naoLidas()).isEqualTo(4L);
    }

    @Test
    @DisplayName("marcar como lida de notificacao de outro usuario nao encontra")
    void marcarComoLidaDeOutroUsuarioNaoEncontra() {
        logado = usuario(3);
        Notificacao notificacao = new Notificacao();
        notificacao.setIdNotificacao(1);
        notificacao.setUsuario(usuario(99));
        when(notificacaoRepository.findById(1)).thenReturn(Optional.of(notificacao));

        assertThatThrownBy(() -> notificacaoService.marcarComoLida(1, logado))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }

    @Test
    @DisplayName("marcar como lida grava lida true")
    void marcarComoLidaGravaLidaTrue() {
        logado = usuario(3);
        Notificacao notificacao = new Notificacao();
        notificacao.setIdNotificacao(1);
        notificacao.setUsuario(logado);
        notificacao.setLida(false);
        when(notificacaoRepository.findById(1)).thenReturn(Optional.of(notificacao));
        when(notificacaoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        NotificacaoResponse resposta = notificacaoService.marcarComoLida(1, logado);

        assertThat(resposta.lida()).isTrue();
    }

    @Test
    @DisplayName("marcar todas delega para o update e devolve contador zero")
    void marcarTodasDelegaParaOUpdateEDevolveZero() {
        logado = usuario(3);

        ContadorNotificacoesResponse contador = notificacaoService.marcarTodas(logado);

        verify(notificacaoRepository).marcarTodasComoLidas(3);
        assertThat(contador.naoLidas()).isZero();
    }

    @Test
    @DisplayName("listagem repassa o filtro de lida e ordena pela geracao mais recente")
    void listagemRepassaOFiltroDeLidaEOrdenaPelaGeracaoMaisRecente() {
        logado = usuario(3);
        ArgumentCaptor<org.springframework.data.domain.Pageable> captor =
                ArgumentCaptor.forClass(org.springframework.data.domain.Pageable.class);
        when(notificacaoRepository.buscarDoUsuario(eq(3), eq(false), captor.capture()))
                .thenReturn(new PageImpl<>(List.of()));

        notificacaoService.listar(logado, false, 2);

        assertThat(captor.getValue().getPageNumber()).isEqualTo(2);
        assertThat(captor.getValue().getSort().getOrderFor("geradaEm").getDirection())
                .isEqualTo(org.springframework.data.domain.Sort.Direction.DESC);
    }

    @Test
    @DisplayName("pagina negativa vira a primeira pagina")
    void paginaNegativaViraAPrimeiraPagina() {
        logado = usuario(3);
        ArgumentCaptor<org.springframework.data.domain.Pageable> captor =
                ArgumentCaptor.forClass(org.springframework.data.domain.Pageable.class);
        when(notificacaoRepository.buscarDoUsuario(eq(3), any(), captor.capture()))
                .thenReturn(new PageImpl<>(List.of()));

        notificacaoService.listar(logado, null, -1);

        assertThat(captor.getValue().getPageNumber()).isZero();
    }

    @Test
    @DisplayName("listagem converte a notificacao com o id da tarefa")
    void listagemConverteANotificacaoComOIdDaTarefa() {
        logado = usuario(3);
        Tarefa tarefa = new Tarefa();
        tarefa.setIdTarefa(10);
        Notificacao notificacao = new Notificacao();
        notificacao.setIdNotificacao(1);
        notificacao.setTipo(TipoNotificacao.D1);
        notificacao.setMensagem("Sua atividade vai vencer amanhã: Ligar");
        notificacao.setLida(false);
        notificacao.setTarefa(tarefa);
        when(notificacaoRepository.buscarDoUsuario(eq(3), any(), any()))
                .thenReturn(new PageImpl<>(List.of(notificacao)));

        var pagina = notificacaoService.listar(logado, null, 0);

        assertThat(pagina.conteudo()).hasSize(1);
        assertThat(pagina.conteudo().get(0).tipo()).isEqualTo(TipoNotificacao.D1);
        assertThat(pagina.conteudo().get(0).idTarefa()).isEqualTo(10);
        assertThat(pagina.conteudo().get(0).lida()).isFalse();
    }

    @Test
    @DisplayName("notificacao de reuniao volta sem id de tarefa")
    void notificacaoDeReuniaoVoltaSemIdDeTarefa() {
        Notificacao notificacao = new Notificacao();
        notificacao.setTipo(TipoNotificacao.REUNIAO);
        notificacao.setLida(null);

        NotificacaoResponse resposta = NotificacaoResponse.de(notificacao);

        assertThat(resposta.idTarefa()).isNull();
        assertThat(resposta.lida()).isFalse();
    }

    @Test
    @DisplayName("marcar como lida de notificacao inexistente nao encontra")
    void marcarComoLidaDeNotificacaoInexistenteNaoEncontra() {
        logado = usuario(3);
        when(notificacaoRepository.findById(1)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> notificacaoService.marcarComoLida(1, logado))
                .isInstanceOf(RecursoNaoEncontradoException.class);
        verify(notificacaoRepository, never()).save(any());
    }

    @Test
    @DisplayName("marcar como lida de outro usuario nao grava nada")
    void marcarComoLidaDeOutroUsuarioNaoGravaNada() {
        logado = usuario(3);
        Notificacao notificacao = new Notificacao();
        notificacao.setIdNotificacao(1);
        notificacao.setUsuario(usuario(99));
        notificacao.setLida(false);
        when(notificacaoRepository.findById(1)).thenReturn(Optional.of(notificacao));

        assertThatThrownBy(() -> notificacaoService.marcarComoLida(1, logado))
                .isInstanceOf(RecursoNaoEncontradoException.class);

        assertThat(notificacao.getLida()).isFalse();
        verify(notificacaoRepository, never()).save(any());
    }

    @Test
    @DisplayName("contador sem notificacoes nao lidas devolve zero")
    void contadorSemNaoLidasDevolveZero() {
        logado = usuario(3);
        when(notificacaoRepository.countByUsuarioIdUsuarioAndLidaFalse(3)).thenReturn(0L);

        assertThat(notificacaoService.contador(logado).naoLidas()).isZero();
    }
}

package com.miniCRM.miniCRM.service;

import com.miniCRM.miniCRM.dto.notificacao.ContadorNotificacoesResponse;
import com.miniCRM.miniCRM.dto.notificacao.NotificacaoResponse;
import com.miniCRM.miniCRM.exception.RecursoNaoEncontradoException;
import com.miniCRM.miniCRM.model.Notificacao;
import com.miniCRM.miniCRM.model.Usuario;
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
}

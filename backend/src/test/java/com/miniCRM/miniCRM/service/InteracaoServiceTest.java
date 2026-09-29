package com.miniCRM.miniCRM.service;

import com.miniCRM.miniCRM.dto.cliente.CriarInteracaoRequest;
import com.miniCRM.miniCRM.dto.cliente.ReuniaoAgendada;
import com.miniCRM.miniCRM.exception.RegraNegocioException;
import com.miniCRM.miniCRM.model.Cliente;
import com.miniCRM.miniCRM.model.Interacao;
import com.miniCRM.miniCRM.model.Usuario;
import com.miniCRM.miniCRM.model.enums.TipoInteracao;
import com.miniCRM.miniCRM.repository.InteracaoRepository;
import com.miniCRM.miniCRM.repository.UsuarioRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InteracaoServiceTest {

    @Mock
    private InteracaoRepository interacaoRepository;
    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private ClienteService clienteService;

    @InjectMocks
    private InteracaoService interacaoService;

    @Test
    @DisplayName("interacao em cliente excluido da erro")
    void interacaoEmClienteExcluidoDaErro() {
        Cliente cliente = new Cliente();
        cliente.setIdCliente(3);
        cliente.setExcluido(true);
        when(clienteService.buscarPorId(3)).thenReturn(cliente);

        CriarInteracaoRequest dto = new CriarInteracaoRequest(TipoInteracao.LIGACAO, null, "contato feito");

        assertThatThrownBy(() -> interacaoService.criar(3, dto, 1))
                .isInstanceOf(RegraNegocioException.class);
    }

    @Test
    @DisplayName("reunioes agendadas viram contrato com usuario, cliente e data")
    void reunioesAgendadasViramContratoComUsuarioClienteEData() {
        LocalDateTime inicio = LocalDateTime.of(2026, 9, 10, 0, 0);
        LocalDateTime fim = LocalDateTime.of(2026, 9, 25, 23, 59);

        Cliente cliente = new Cliente();
        cliente.setNome("Construtora Horizonte");
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(7);
        Interacao reuniao = new Interacao();
        reuniao.setCliente(cliente);
        reuniao.setUsuario(usuario);
        reuniao.setDataInteracao(LocalDateTime.of(2026, 9, 20, 14, 0));
        when(interacaoRepository.agendadasEntre(TipoInteracao.REUNIAO, inicio, fim)).thenReturn(List.of(reuniao));

        List<ReuniaoAgendada> reunioes = interacaoService.reunioesAgendadasEntre(inicio, fim);

        assertThat(reunioes).containsExactly(
                new ReuniaoAgendada(7, "Construtora Horizonte", LocalDateTime.of(2026, 9, 20, 14, 0)));
    }
}

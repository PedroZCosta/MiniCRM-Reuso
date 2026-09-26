package com.miniCRM.miniCRM.service;

import com.miniCRM.miniCRM.dto.cliente.CriarInteracaoRequest;
import com.miniCRM.miniCRM.exception.RegraNegocioException;
import com.miniCRM.miniCRM.model.Cliente;
import com.miniCRM.miniCRM.model.enums.TipoInteracao;
import com.miniCRM.miniCRM.repository.InteracaoRepository;
import com.miniCRM.miniCRM.repository.UsuarioRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
}

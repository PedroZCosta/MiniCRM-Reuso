package com.miniCRM.miniCRM.service;

import com.miniCRM.miniCRM.dto.cliente.CriarInteracaoRequest;
import com.miniCRM.miniCRM.exception.RecursoNaoEncontradoException;
import com.miniCRM.miniCRM.exception.RegraNegocioException;
import com.miniCRM.miniCRM.model.Cliente;
import com.miniCRM.miniCRM.model.Interacao;
import com.miniCRM.miniCRM.model.Usuario;
import com.miniCRM.miniCRM.repository.InteracaoRepository;
import com.miniCRM.miniCRM.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class InteracaoService {

    private static final int TAMANHO_PAGINA = 20;
    private static final int OBSERVACOES_MAX = 65535;

    private final InteracaoRepository interacaoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ClienteService clienteService;

    public Interacao criar(Integer idCliente, CriarInteracaoRequest dto, Integer idUsuarioLogado) {
        Cliente cliente = clienteService.buscarPorId(idCliente);
        if (cliente.getExcluido()) {
            throw new RegraNegocioException("Cliente excluído não pode receber interação");
        }
        if (dto.tipo() == null) {
            throw new RegraNegocioException("Tipo da interação é obrigatório");
        }
        if (dto.observacoes() != null && dto.observacoes().length() > OBSERVACOES_MAX) {
            throw new RegraNegocioException("Observações excedem o tamanho máximo permitido");
        }

        Usuario usuario = usuarioRepository.findById(idUsuarioLogado)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado"));

        Interacao interacao = new Interacao();
        interacao.setCliente(cliente);
        interacao.setUsuario(usuario);
        interacao.setTipo(dto.tipo());
        interacao.setDataInteracao(dto.dataInteracao() != null ? dto.dataInteracao() : LocalDateTime.now());
        interacao.setObservacoes(dto.observacoes());
        return interacaoRepository.save(interacao);
    }

    public Page<Interacao> listar(Integer idCliente, int page) {
        clienteService.buscarPorId(idCliente);
        return interacaoRepository.findByClienteIdClienteOrderByDataInteracaoDesc(
                idCliente, PageRequest.of(page, TAMANHO_PAGINA));
    }
}

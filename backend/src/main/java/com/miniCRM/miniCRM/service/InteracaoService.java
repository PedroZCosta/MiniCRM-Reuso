package com.miniCRM.miniCRM.service;

import com.miniCRM.miniCRM.dto.cliente.CriarInteracaoRequest;
import com.miniCRM.miniCRM.dto.cliente.ReuniaoAgendada;
import com.miniCRM.miniCRM.exception.RecursoNaoEncontradoException;
import com.miniCRM.miniCRM.exception.RegraNegocioException;
import com.miniCRM.miniCRM.model.Cliente;
import com.miniCRM.miniCRM.model.Interacao;
import com.miniCRM.miniCRM.model.Usuario;
import com.miniCRM.miniCRM.model.enums.TipoInteracao;
import com.miniCRM.miniCRM.repository.InteracaoRepository;
import com.miniCRM.miniCRM.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

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
        int paginaSegura = Math.max(page, 0);
        return interacaoRepository.findByClienteIdClienteOrderByDataInteracaoDesc(
                idCliente, PageRequest.of(paginaSegura, TAMANHO_PAGINA));
    }

    /** Consulta da SPEC-04 (job de notificações): reuniões marcadas dentro da janela, de qualquer cliente. */
    @Transactional(readOnly = true)
    public List<ReuniaoAgendada> reunioesAgendadasEntre(LocalDateTime inicio, LocalDateTime fim) {
        return interacaoRepository.agendadasEntre(TipoInteracao.REUNIAO, inicio, fim).stream()
                .map(i -> new ReuniaoAgendada(
                        i.getUsuario().getIdUsuario(), i.getCliente().getNome(), i.getDataInteracao()))
                .toList();
    }
}

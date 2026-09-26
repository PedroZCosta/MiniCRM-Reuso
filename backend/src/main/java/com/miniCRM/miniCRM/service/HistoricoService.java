package com.miniCRM.miniCRM.service;

import com.miniCRM.miniCRM.dto.comum.HistoricoItem;
import com.miniCRM.miniCRM.dto.comum.PageResponse;
import com.miniCRM.miniCRM.dto.comum.TipoHistoricoItem;
import com.miniCRM.miniCRM.model.Interacao;
import com.miniCRM.miniCRM.repository.InteracaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HistoricoService {

    private static final int TAMANHO_PAGINA = 20;

    private final InteracaoRepository interacaoRepository;
    private final ClienteService clienteService;
    private final OportunidadeConsultaService oportunidadeConsultaService;
    private final TarefaConsultaService tarefaConsultaService;

    public PageResponse<HistoricoItem> listar(Integer idCliente, int page) {
        clienteService.buscarPorId(idCliente);

        List<HistoricoItem> itens = new ArrayList<>();
        itens.addAll(interacoesDoCliente(idCliente));
        itens.addAll(historicoOu(() -> oportunidadeConsultaService.historicoDoCliente(idCliente)));
        itens.addAll(historicoOu(() -> tarefaConsultaService.historicoDoCliente(idCliente)));
        itens.sort(Comparator.comparing(HistoricoItem::data).reversed());

        int paginaSegura = Math.max(page, 0);
        int totalRegistros = itens.size();
        int totalPaginas = (int) Math.ceil(totalRegistros / (double) TAMANHO_PAGINA);
        int inicio = Math.min(paginaSegura * TAMANHO_PAGINA, totalRegistros);
        int fim = Math.min(inicio + TAMANHO_PAGINA, totalRegistros);

        return new PageResponse<>(itens.subList(inicio, fim), paginaSegura, TAMANHO_PAGINA, totalPaginas, totalRegistros);
    }

    private List<HistoricoItem> interacoesDoCliente(Integer idCliente) {
        return interacaoRepository.findByClienteIdCliente(idCliente).stream()
                .map(this::paraHistoricoItem)
                .toList();
    }

    private HistoricoItem paraHistoricoItem(Interacao interacao) {
        return new HistoricoItem(
                TipoHistoricoItem.INTERACAO,
                interacao.getDataInteracao(),
                interacao.getTipo().name(),
                interacao.getObservacoes(),
                interacao.getUsuario().getNome());
    }

    private List<HistoricoItem> historicoOu(Fornecedor fornecedor) {
        try {
            return fornecedor.buscar();
        } catch (UnsupportedOperationException e) {
            return Collections.emptyList();
        }
    }

    @FunctionalInterface
    private interface Fornecedor {
        List<HistoricoItem> buscar();
    }
}

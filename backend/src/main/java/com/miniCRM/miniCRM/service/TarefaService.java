package com.miniCRM.miniCRM.service;

import com.miniCRM.miniCRM.dto.comum.HistoricoItem;
import com.miniCRM.miniCRM.dto.comum.PageResponse;
import com.miniCRM.miniCRM.dto.comum.TipoHistoricoItem;
import com.miniCRM.miniCRM.dto.tarefa.AlertasTarefas;
import com.miniCRM.miniCRM.dto.tarefa.CriarTarefaRequest;
import com.miniCRM.miniCRM.dto.tarefa.EditarTarefaRequest;
import com.miniCRM.miniCRM.dto.tarefa.TarefaResponse;
import com.miniCRM.miniCRM.exception.ConflitoException;
import com.miniCRM.miniCRM.exception.RecursoNaoEncontradoException;
import com.miniCRM.miniCRM.exception.RegraNegocioException;
import com.miniCRM.miniCRM.model.Cliente;
import com.miniCRM.miniCRM.model.Oportunidade;
import com.miniCRM.miniCRM.model.Tarefa;
import com.miniCRM.miniCRM.model.Usuario;
import com.miniCRM.miniCRM.model.enums.FaixaTarefa;
import com.miniCRM.miniCRM.model.enums.SituacaoTarefa;
import com.miniCRM.miniCRM.repository.TarefaRepository;
import com.miniCRM.miniCRM.security.EscopoCarteira;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class TarefaService implements TarefaConsultaService {

    private static final int TAMANHO_PAGINA = 20;

    private final TarefaRepository tarefaRepository;
    private final ClienteService clienteService;
    private final OportunidadeService oportunidadeService;
    private final EscopoCarteira escopoCarteira;
    private final EntityManager entityManager;

    /** RN-01 (RF10). Cliente e oportunidade sao validados pelas interfaces publicas dos donos. */
    public TarefaResponse criar(CriarTarefaRequest request, Usuario logado) {
        validarCampos(request.titulo(), request.dataVencimento());

        Tarefa tarefa = new Tarefa();
        tarefa.setTitulo(request.titulo());
        tarefa.setDescricao(request.descricao());
        tarefa.setDataVencimento(request.dataVencimento());
        tarefa.setConcluida(false);
        tarefa.setUsuario(definirResponsavel(request.idUsuario(), logado));

        if (request.idCliente() != null) {
            // buscarPorId ja aplica o EscopoCarteira: 404 para inexistente E para fora do escopo.
            Cliente cliente = clienteService.buscarPorId(request.idCliente());
            if (Boolean.TRUE.equals(cliente.getExcluido())) {
                throw new RegraNegocioException("Cliente excluído não pode receber tarefa");
            }
            tarefa.setCliente(cliente);
        }

        if (request.idOportunidade() != null) {
            Oportunidade oportunidade = oportunidadeService.buscarNoEscopo(request.idOportunidade());
            tarefa.setOportunidade(oportunidade);
        }

        return paraResponse(tarefaRepository.save(tarefa), LocalDate.now());
    }

    /**
     * RN-03. "situacao" e "faixa" viram SEMPRE aritmetica de data: nao existe coluna "vencida"
     * no banco para consultar (RF28). Listagem recortada pelo EscopoCarteira.
     */
    @Transactional(readOnly = true)
    public PageResponse<TarefaResponse> listar(SituacaoTarefa situacao, FaixaTarefa faixa,
                                               Integer clienteId, int page) {
        if (situacao != null && faixa != null) {
            throw new RegraNegocioException("Informe situacao ou faixa, não os dois");
        }

        LocalDate hoje = LocalDate.now();
        Boolean concluida = null;
        LocalDate de = null;
        LocalDate ate = null;

        if (faixa != null) {
            concluida = false;                  // a tela inicial (RF13) so mostra tarefa aberta
            de = faixa.inicio(hoje);
            ate = faixa.fim(hoje);
        } else if (situacao != null) {
            switch (situacao) {
                case PENDENTE -> {
                    concluida = false;
                    de = hoje;
                }
                case VENCIDA -> {
                    concluida = false;
                    ate = hoje.minusDays(1);
                }
                case CONCLUIDA -> concluida = true;
            }
        }

        Pageable pageable = PageRequest.of(Math.max(page, 0), TAMANHO_PAGINA,
                Sort.by("dataVencimento").ascending().and(Sort.by("idTarefa").ascending()));
        List<Integer> vendedores = escopoCarteira.vendedoresVisiveis().orElse(null);

        Page<Tarefa> pagina = (vendedores == null)
                ? tarefaRepository.buscar(clienteId, concluida, de, ate, pageable)
                : tarefaRepository.buscarDosVendedores(clienteId, concluida, de, ate, vendedores, pageable);

        return PageResponse.de(pagina.map(t -> paraResponse(t, hoje)));
    }

    public TarefaResponse editar(Integer idTarefa, EditarTarefaRequest request) {
        Tarefa tarefa = buscarNoEscopo(idTarefa);
        validarCampos(request.titulo(), request.dataVencimento());

        // RN-02: editar nunca toca em concluida/concluidaEm.
        tarefa.setTitulo(request.titulo());
        tarefa.setDescricao(request.descricao());
        tarefa.setDataVencimento(request.dataVencimento());

        return paraResponse(tarefaRepository.save(tarefa), LocalDate.now());
    }

    /** RN-02: a unica coisa persistida e "concluida"; "vencida" segue calculada. */
    public TarefaResponse concluir(Integer idTarefa) {
        Tarefa tarefa = buscarNoEscopo(idTarefa);
        if (Boolean.TRUE.equals(tarefa.getConcluida())) {
            throw new ConflitoException("Tarefa já está concluída");
        }
        tarefa.setConcluida(true);
        tarefa.setConcluidaEm(LocalDateTime.now());
        return paraResponse(tarefaRepository.save(tarefa), LocalDate.now());
    }

    public TarefaResponse reabrir(Integer idTarefa) {
        Tarefa tarefa = buscarNoEscopo(idTarefa);
        if (!Boolean.TRUE.equals(tarefa.getConcluida())) {
            throw new ConflitoException("Somente tarefa concluída pode ser reaberta");
        }
        tarefa.setConcluida(false);
        tarefa.setConcluidaEm(null);
        return paraResponse(tarefaRepository.save(tarefa), LocalDate.now());
    }

    /**
     * Cards da tela inicial (RF13) para o DashboardFacade. Recebe o escopo ja resolvido
     * (null = ADMIN = sem filtro) porque a facade resolve o EscopoCarteira uma unica vez.
     */
    @Transactional(readOnly = true)
    public AlertasTarefas alertas(List<Integer> vendedoresOuNull) {
        LocalDate hoje = LocalDate.now();
        return new AlertasTarefas(
                contar(FaixaTarefa.VENCIDA, hoje, vendedoresOuNull),
                contar(FaixaTarefa.HOJE, hoje, vendedoresOuNull),
                contar(FaixaTarefa.D1, hoje, vendedoresOuNull),
                contar(FaixaTarefa.D15, hoje, vendedoresOuNull));
    }

    /**
     * Contrato da SPEC-02 §2.3 (RF16). Sem EscopoCarteira aqui: o HistoricoService ja validou
     * o cliente no escopo antes de chamar (mesmo contrato de OportunidadeService.listarDoCliente).
     *
     * <p>A data da timeline e concluidaEm quando a tarefa foi concluida e o vencimento quando
     * ainda esta aberta: e o instante que o usuario reconhece na linha do tempo.
     */
    @Override
    @Transactional(readOnly = true)
    public List<HistoricoItem> historicoDoCliente(Integer idCliente) {
        return tarefaRepository.listarDoCliente(idCliente).stream()
                .map(t -> new HistoricoItem(
                        TipoHistoricoItem.TAREFA,
                        t.getConcluidaEm() != null
                                ? t.getConcluidaEm()
                                : t.getDataVencimento().atStartOfDay(),
                        t.getTitulo(),
                        t.getDescricao(),
                        t.getUsuario().getNome()))
                .toList();
    }

    private long contar(FaixaTarefa faixa, LocalDate hoje, List<Integer> vendedoresOuNull) {
        LocalDate de = faixa.inicio(hoje);
        LocalDate ate = faixa.fim(hoje);

        return (vendedoresOuNull == null)
                ? tarefaRepository.contarAbertasNaJanela(de, ate)
                : tarefaRepository.contarAbertasNaJanelaDosVendedores(de, ate, vendedoresOuNull);
    }

    private void validarCampos(String titulo, LocalDate dataVencimento) {
        if (titulo == null || titulo.isBlank()) {
            throw new RegraNegocioException("O título é obrigatório");
        }
        if (dataVencimento == null) {
            throw new RegraNegocioException("A data de vencimento é obrigatória");
        }
    }

    private Tarefa buscarNoEscopo(Integer idTarefa) {
        Tarefa tarefa = tarefaRepository.findById(idTarefa)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Tarefa não encontrada"));

        Optional<List<Integer>> escopo = escopoCarteira.vendedoresVisiveis();
        if (escopo.isPresent() && !escopo.get().contains(tarefa.getUsuario().getIdUsuario())) {
            throw new RecursoNaoEncontradoException("Tarefa não encontrada");
        }
        return tarefa;
    }

    /**
     * Responsavel = autenticado. GERENTE/ADMIN podem atribuir a outro, e o EscopoCarteira
     * ja limita a quem: vendedor so consegue a si, gerente so a equipe, admin a qualquer um.
     */
    private Usuario definirResponsavel(Integer idUsuario, Usuario logado) {
        if (idUsuario == null) {
            return entityManager.getReference(Usuario.class, logado.getIdUsuario());
        }

        Optional<List<Integer>> escopo = escopoCarteira.vendedoresVisiveis();
        boolean foraDoEscopo = escopo.isPresent() && !escopo.get().contains(idUsuario);
        Usuario responsavel = entityManager.find(Usuario.class, idUsuario);

        if (foraDoEscopo || responsavel == null) {
            throw new RecursoNaoEncontradoException("Usuário responsável não encontrado");
        }
        return responsavel;
    }

    private TarefaResponse paraResponse(Tarefa t, LocalDate hoje) {
        Cliente cliente = t.getCliente();
        Oportunidade oportunidade = t.getOportunidade();

        return new TarefaResponse(
                t.getIdTarefa(),
                t.getTitulo(),
                t.getDescricao(),
                t.getDataVencimento(),
                t.situacao(hoje),
                t.vencida(hoje),
                Boolean.TRUE.equals(t.getConcluida()),
                t.getConcluidaEm(),
                t.getUsuario().getNome(),
                cliente != null ? cliente.getIdCliente() : null,
                cliente != null ? cliente.getNome() : null,
                oportunidade != null ? oportunidade.getIdOportunidade() : null,
                oportunidade != null ? oportunidade.getTitulo() : null,
                t.getCriadoEm());
    }
}

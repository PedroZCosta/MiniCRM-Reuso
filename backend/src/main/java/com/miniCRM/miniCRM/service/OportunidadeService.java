package com.miniCRM.miniCRM.service;

import com.miniCRM.miniCRM.dto.comum.PageResponse;
import com.miniCRM.miniCRM.dto.funil.ColunaFunilResponse;
import com.miniCRM.miniCRM.dto.funil.CriarOportunidadeRequest;
import com.miniCRM.miniCRM.dto.funil.EditarOportunidadeRequest;
import com.miniCRM.miniCRM.dto.funil.FunilResponse;
import com.miniCRM.miniCRM.dto.funil.HistoricoEtapaResponse;
import com.miniCRM.miniCRM.dto.funil.MotivoPerdaResponse;
import com.miniCRM.miniCRM.dto.funil.MoverEtapaRequest;
import com.miniCRM.miniCRM.dto.funil.OportunidadeDetalheResponse;
import com.miniCRM.miniCRM.dto.funil.OportunidadeResponse;
import com.miniCRM.miniCRM.dto.funil.TotalEtapa;
import com.miniCRM.miniCRM.event.OportunidadeFechadaEvent;
import com.miniCRM.miniCRM.exception.ConflitoException;
import com.miniCRM.miniCRM.exception.RecursoNaoEncontradoException;
import com.miniCRM.miniCRM.exception.RegraNegocioException;
import com.miniCRM.miniCRM.model.Cliente;
import com.miniCRM.miniCRM.model.HistoricoEtapa;
import com.miniCRM.miniCRM.model.MotivoPerda;
import com.miniCRM.miniCRM.model.Oportunidade;
import com.miniCRM.miniCRM.model.Usuario;
import com.miniCRM.miniCRM.model.enums.EtapaOportunidade;
import com.miniCRM.miniCRM.repository.HistoricoEtapaRepository;
import com.miniCRM.miniCRM.repository.MotivoPerdaRepository;
import com.miniCRM.miniCRM.repository.OportunidadeRepository;
import com.miniCRM.miniCRM.security.EscopoCarteira;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class OportunidadeService {

    private static final int TAMANHO_PAGINA = 20;

    private final OportunidadeRepository oportunidadeRepository;
    private final HistoricoEtapaRepository historicoEtapaRepository;
    private final MotivoPerdaRepository motivoPerdaRepository;
    private final ClienteService clienteService;
    private final EscopoCarteira escopoCarteira;
    private final ApplicationEventPublisher publisher;
    private final EntityManager entityManager;

    public OportunidadeResponse criar(CriarOportunidadeRequest request) {
        if (request.idCliente() == null) {
            throw new RegraNegocioException("O cliente é obrigatório");
        }
        validarCampos(request.titulo(), request.valorEstimado());

        Cliente cliente = clienteService.buscarClienteAtivo(request.idCliente());
        Usuario vendedor = definirVendedor(request.idVendedor());

        Oportunidade oportunidade = new Oportunidade();
        oportunidade.setCliente(cliente);
        oportunidade.setVendedor(vendedor);
        oportunidade.setTitulo(request.titulo());
        oportunidade.setValorEstimado(request.valorEstimado());
        oportunidade.setDataPrevista(request.dataPrevista());
        oportunidade.setEtapa(EtapaOportunidade.PROSPECCAO);

        Oportunidade salva = oportunidadeRepository.save(oportunidade);
        return paraResponse(salva);
    }

    @Transactional(readOnly = true)
    public PageResponse<OportunidadeResponse> listar(EtapaOportunidade etapa, Integer clienteId, int page) {
        Pageable pageable = PageRequest.of(page, TAMANHO_PAGINA, Sort.by("criadoEm").descending());
        List<Integer> vendedores = escopoCarteira.vendedoresVisiveis().orElse(null);

        Page<Oportunidade> pagina = (vendedores == null)
                ? oportunidadeRepository.buscar(etapa, clienteId, pageable)
                : oportunidadeRepository.buscarDosVendedores(etapa, clienteId, vendedores, pageable);

        return PageResponse.de(pagina.map(this::paraResponse));
    }

    @Transactional(readOnly = true)
    public OportunidadeDetalheResponse buscarPorId(Integer idOportunidade) {
        Oportunidade oportunidade = buscarNoEscopo(idOportunidade);

        List<HistoricoEtapaResponse> historico = historicoEtapaRepository
                .findByOportunidadeIdOportunidadeOrderByIdHistoricoAsc(idOportunidade)
                .stream()
                .map(h -> new HistoricoEtapaResponse(
                        h.getEtapaAnterior(),
                        h.getEtapaNova(),
                        h.getUsuario().getNome(),
                        h.getAlteradoEm()))
                .toList();

        return new OportunidadeDetalheResponse(paraResponse(oportunidade), historico);
    }

    public OportunidadeResponse editar(Integer idOportunidade, EditarOportunidadeRequest request) {
        Oportunidade oportunidade = buscarNoEscopo(idOportunidade);

        if (oportunidade.getEtapa().fechada()) {
            throw new ConflitoException("Oportunidade fechada precisa ser reaberta antes de editar");
        }

        validarCampos(request.titulo(), request.valorEstimado());

        oportunidade.setTitulo(request.titulo());
        oportunidade.setValorEstimado(request.valorEstimado());
        oportunidade.setDataPrevista(request.dataPrevista());

        return paraResponse(oportunidadeRepository.save(oportunidade));
    }

    public OportunidadeResponse moverEtapa(Integer idOportunidade, MoverEtapaRequest request) {
        Oportunidade oportunidade = buscarNoEscopo(idOportunidade);
        EtapaOportunidade etapaAtual = oportunidade.getEtapa();
        EtapaOportunidade novaEtapa = request.novaEtapa();

        if (novaEtapa == null) {
            throw new RegraNegocioException("A nova etapa é obrigatória");
        }

        if (!etapaAtual.podeIrPara(novaEtapa)) {
            String mensagem = etapaAtual.fechada()
                    ? "Oportunidade fechada precisa ser reaberta antes de mover"
                    : "A oportunidade já está nessa etapa";
            throw new ConflitoException(mensagem);
        }

        if (novaEtapa == EtapaOportunidade.PERDIDA) {
            if (request.idMotivoPerda() == null) {
                throw new RegraNegocioException("O motivo da perda é obrigatório");
            }
            MotivoPerda motivo = motivoPerdaRepository.findById(request.idMotivoPerda())
                    .orElseThrow(() -> new RegraNegocioException("Motivo da perda inválido"));
            oportunidade.setMotivoPerda(motivo);
            oportunidade.setFechadaEm(LocalDateTime.now());
        } else if (novaEtapa == EtapaOportunidade.GANHA) {
            oportunidade.setMotivoPerda(null);
            oportunidade.setFechadaEm(LocalDateTime.now());
        }

        oportunidade.setEtapa(novaEtapa);
        oportunidadeRepository.save(oportunidade);

        registrarHistorico(oportunidade, etapaAtual, novaEtapa);

        if (novaEtapa.fechada()) {
            publisher.publishEvent(new OportunidadeFechadaEvent(
                    oportunidade.getIdOportunidade(),
                    oportunidade.getCliente().getIdCliente(),
                    oportunidade.getVendedor().getIdUsuario(),
                    novaEtapa,
                    oportunidade.getValorEstimado(),
                    oportunidade.getFechadaEm()));
        }

        return paraResponse(oportunidade);
    }

    public OportunidadeResponse reabrir(Integer idOportunidade) {
        Oportunidade oportunidade = buscarNoEscopo(idOportunidade);
        EtapaOportunidade etapaAtual = oportunidade.getEtapa();

        if (!etapaAtual.fechada()) {
            throw new ConflitoException("Somente oportunidade fechada pode ser reaberta");
        }

        oportunidade.setEtapa(EtapaOportunidade.PROPOSTA);
        oportunidade.setFechadaEm(null);
        oportunidade.setMotivoPerda(null);
        oportunidadeRepository.save(oportunidade);

        registrarHistorico(oportunidade, etapaAtual, EtapaOportunidade.PROPOSTA);

        return paraResponse(oportunidade);
    }

    @Transactional(readOnly = true)
    public FunilResponse montarFunil() {
        List<Oportunidade> todas = listarDoEscopo(escopoCarteira.vendedoresVisiveis().orElse(null));

        List<ColunaFunilResponse> colunas = new ArrayList<>();
        colunas.add(montarColuna("PROSPECCAO", filtrarPorEtapa(todas, EtapaOportunidade.PROSPECCAO)));
        colunas.add(montarColuna("CONTATO", filtrarPorEtapa(todas, EtapaOportunidade.CONTATO)));
        colunas.add(montarColuna("PROPOSTA", filtrarPorEtapa(todas, EtapaOportunidade.PROPOSTA)));
        colunas.add(montarColuna("FECHADO", todas.stream().filter(o -> o.getEtapa().fechada()).toList()));

        return new FunilResponse(colunas, somarValores(filtrarAbertas(todas)));
    }

    @Transactional(readOnly = true)
    public List<MotivoPerdaResponse> listarMotivosPerda() {
        return motivoPerdaRepository.findAll().stream()
                .map(m -> new MotivoPerdaResponse(m.getIdMotivo(), m.getDescricao()))
                .toList();
    }

    @Transactional(readOnly = true)
    public Map<EtapaOportunidade, TotalEtapa> totaisPorEtapa(List<Integer> vendedoresOuNull) {
        List<Oportunidade> lista = listarDoEscopo(vendedoresOuNull);

        Map<EtapaOportunidade, TotalEtapa> totais = new EnumMap<>(EtapaOportunidade.class);
        for (EtapaOportunidade etapa : EtapaOportunidade.values()) {
            List<Oportunidade> daEtapa = filtrarPorEtapa(lista, etapa);
            totais.put(etapa, new TotalEtapa(daEtapa.size(), somarValores(daEtapa)));
        }
        return totais;
    }

    @Transactional(readOnly = true)
    public BigDecimal valorEmNegociacao(List<Integer> vendedoresOuNull) {
        return somarValores(filtrarAbertas(listarDoEscopo(vendedoresOuNull)));
    }

    @Transactional(readOnly = true)
    public List<Oportunidade> fechadasNoPeriodo(LocalDate inicio, LocalDate fim, List<Integer> vendedoresOuNull) {
        LocalDateTime deInicio = inicio.atStartOfDay();
        LocalDateTime ateFim = fim.plusDays(1).atStartOfDay();

        return (vendedoresOuNull == null)
                ? oportunidadeRepository.fechadasEntre(deInicio, ateFim)
                : oportunidadeRepository.fechadasEntreDosVendedores(deInicio, ateFim, vendedoresOuNull);
    }

    @Transactional(readOnly = true)
    public List<Oportunidade> listarDoCliente(Integer idCliente) {
        return oportunidadeRepository.findByClienteIdClienteOrderByCriadoEmDesc(idCliente);
    }

    private void validarCampos(String titulo, BigDecimal valorEstimado) {
        if (titulo == null || titulo.isBlank()) {
            throw new RegraNegocioException("O título é obrigatório");
        }
        if (valorEstimado == null || valorEstimado.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RegraNegocioException("O valor estimado deve ser maior que zero");
        }
    }

    private Oportunidade buscarNoEscopo(Integer idOportunidade) {
        Oportunidade oportunidade = oportunidadeRepository.findById(idOportunidade)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Oportunidade não encontrada"));

        Optional<List<Integer>> escopo = escopoCarteira.vendedoresVisiveis();
        if (escopo.isPresent() && !escopo.get().contains(oportunidade.getVendedor().getIdUsuario())) {
            throw new RecursoNaoEncontradoException("Oportunidade não encontrada");
        }
        return oportunidade;
    }

    private Usuario definirVendedor(Integer idVendedor) {
        if (idVendedor == null) {
            return usuarioLogado();
        }

        Optional<List<Integer>> escopo = escopoCarteira.vendedoresVisiveis();
        boolean foraDoEscopo = escopo.isPresent() && !escopo.get().contains(idVendedor);
        Usuario vendedor = entityManager.find(Usuario.class, idVendedor);

        if (foraDoEscopo || vendedor == null) {
            throw new RecursoNaoEncontradoException("Vendedor não encontrado");
        }
        return vendedor;
    }

    private Usuario usuarioLogado() {
        String idDoToken = SecurityContextHolder.getContext().getAuthentication().getName();
        return entityManager.getReference(Usuario.class, Integer.valueOf(idDoToken));
    }

    private void registrarHistorico(Oportunidade oportunidade, EtapaOportunidade anterior, EtapaOportunidade nova) {
        HistoricoEtapa historico = new HistoricoEtapa();
        historico.setOportunidade(oportunidade);
        historico.setEtapaAnterior(anterior);
        historico.setEtapaNova(nova);
        historico.setUsuario(usuarioLogado());
        historicoEtapaRepository.save(historico);
    }

    private List<Oportunidade> listarDoEscopo(List<Integer> vendedoresOuNull) {
        return (vendedoresOuNull == null)
                ? oportunidadeRepository.listarTodas()
                : oportunidadeRepository.listarDosVendedores(vendedoresOuNull);
    }

    private List<Oportunidade> filtrarPorEtapa(List<Oportunidade> lista, EtapaOportunidade etapa) {
        return lista.stream().filter(o -> o.getEtapa() == etapa).toList();
    }

    private List<Oportunidade> filtrarAbertas(List<Oportunidade> lista) {
        return lista.stream().filter(o -> !o.getEtapa().fechada()).toList();
    }

    private BigDecimal somarValores(List<Oportunidade> lista) {
        return lista.stream()
                .map(Oportunidade::getValorEstimado)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private ColunaFunilResponse montarColuna(String nomeColuna, List<Oportunidade> daColuna) {
        List<OportunidadeResponse> cards = daColuna.stream().map(this::paraResponse).toList();
        return new ColunaFunilResponse(nomeColuna, daColuna.size(), somarValores(daColuna), cards);
    }

    private OportunidadeResponse paraResponse(Oportunidade o) {
        EtapaOportunidade etapa = o.getEtapa();
        return new OportunidadeResponse(
                o.getIdOportunidade(),
                o.getTitulo(),
                o.getCliente().getNome(),
                o.getValorEstimado(),
                o.getVendedor().getNome(),
                o.getDataPrevista(),
                etapa,
                etapa.fechada() ? etapa : null,
                o.getMotivoPerda() != null ? o.getMotivoPerda().getDescricao() : null);
    }
}

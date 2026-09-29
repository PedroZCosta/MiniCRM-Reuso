package com.miniCRM.miniCRM.service;

import com.miniCRM.miniCRM.dto.comum.HistoricoItem;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TarefaServiceTest {

    @Mock
    private TarefaRepository tarefaRepository;
    @Mock
    private ClienteService clienteService;
    @Mock
    private OportunidadeService oportunidadeService;
    @Mock
    private EscopoCarteira escopoCarteira;
    @Mock
    private EntityManager entityManager;

    @InjectMocks
    private TarefaService tarefaService;

    private Usuario logado;

    @BeforeEach
    void prepararLogado() {
        logado = new Usuario();
        logado.setIdUsuario(1);
    }

    private CriarTarefaRequest criarRequest(String titulo, LocalDate vencimento) {
        return new CriarTarefaRequest(titulo, "descricao", vencimento, null, null, null);
    }

    private Tarefa novaTarefa(Integer idUsuario) {
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(idUsuario);
        usuario.setNome("Responsável");

        Tarefa tarefa = new Tarefa();
        tarefa.setIdTarefa(10);
        tarefa.setTitulo("Ligar para cliente");
        tarefa.setDataVencimento(LocalDate.now().plusDays(3));
        tarefa.setConcluida(false);
        tarefa.setUsuario(usuario);
        return tarefa;
    }

    // ---- RN-01: criar ----

    @Test
    @DisplayName("criar sem titulo falha com regra de negocio")
    void criarSemTituloFalha() {

        assertThatThrownBy(() -> tarefaService.criar(criarRequest(" ", LocalDate.now()), logado))
                .isInstanceOf(RegraNegocioException.class);
    }

    @Test
    @DisplayName("criar sem data de vencimento falha com regra de negocio")
    void criarSemVencimentoFalha() {

        assertThatThrownBy(() -> tarefaService.criar(criarRequest("Titulo", null), logado))
                .isInstanceOf(RegraNegocioException.class);
    }

    @Test
    @DisplayName("criar sem cliente nem oportunidade funciona pois sao independentes")
    void criarSemClienteNemOportunidadeFunciona() {
        when(entityManager.getReference(Usuario.class, 1)).thenReturn(logado);
        when(tarefaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        TarefaResponse resposta = tarefaService.criar(criarRequest("Enviar proposta", LocalDate.now()), logado);

        assertThat(resposta.idCliente()).isNull();
        assertThat(resposta.idOportunidade()).isNull();
        verify(clienteService, never()).buscarPorId(any());
        verify(oportunidadeService, never()).buscarNoEscopo(any());
    }

    @Test
    @DisplayName("criar com cliente fora do escopo nao encontra")
    void criarComClienteForaDoEscopoNaoEncontra() {
        when(entityManager.getReference(Usuario.class, 1)).thenReturn(logado);
        when(clienteService.buscarPorId(7))
                .thenThrow(new RecursoNaoEncontradoException("Cliente não encontrado"));

        CriarTarefaRequest request = new CriarTarefaRequest("Titulo", null, LocalDate.now(), 7, null, null);

        assertThatThrownBy(() -> tarefaService.criar(request, logado))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }

    @Test
    @DisplayName("criar com oportunidade fora do escopo nao encontra")
    void criarComOportunidadeForaDoEscopoNaoEncontra() {
        when(entityManager.getReference(Usuario.class, 1)).thenReturn(logado);
        when(oportunidadeService.buscarNoEscopo(9))
                .thenThrow(new RecursoNaoEncontradoException("Oportunidade não encontrada"));

        CriarTarefaRequest request = new CriarTarefaRequest("Titulo", null, LocalDate.now(), null, 9, null);

        assertThatThrownBy(() -> tarefaService.criar(request, logado))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }

    @Test
    @DisplayName("criar com cliente excluido falha com regra de negocio")
    void criarComClienteExcluidoFalha() {
        when(entityManager.getReference(Usuario.class, 1)).thenReturn(logado);

        Cliente clienteExcluido = new Cliente();
        clienteExcluido.setIdCliente(7);
        clienteExcluido.setExcluido(true);
        when(clienteService.buscarPorId(7)).thenReturn(clienteExcluido);

        CriarTarefaRequest request = new CriarTarefaRequest("Titulo", null, LocalDate.now(), 7, null, null);

        assertThatThrownBy(() -> tarefaService.criar(request, logado))
                .isInstanceOf(RegraNegocioException.class);
    }

    @Test
    @DisplayName("criar com cliente e oportunidade vincula as duas entidades")
    void criarComClienteEOportunidadeVinculaAsDuas() {
        when(entityManager.getReference(Usuario.class, 1)).thenReturn(logado);

        Cliente cliente = new Cliente();
        cliente.setIdCliente(7);
        cliente.setNome("Construtora Horizonte");
        cliente.setExcluido(false);
        when(clienteService.buscarPorId(7)).thenReturn(cliente);

        Oportunidade oportunidade = new Oportunidade();
        oportunidade.setIdOportunidade(9);
        oportunidade.setTitulo("Reforma sede");
        when(oportunidadeService.buscarNoEscopo(9)).thenReturn(oportunidade);

        when(tarefaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CriarTarefaRequest request = new CriarTarefaRequest("Titulo", null, LocalDate.now(), 7, 9, null);
        TarefaResponse resposta = tarefaService.criar(request, logado);

        assertThat(resposta.idCliente()).isEqualTo(7);
        assertThat(resposta.idOportunidade()).isEqualTo(9);
    }

    // ---- Responsavel ----

    @Test
    @DisplayName("responsavel padrao e o usuario autenticado quando idUsuario nao e informado")
    void responsavelPadraoEhOUsuarioAutenticado() {
        when(entityManager.getReference(Usuario.class, 1)).thenReturn(logado);
        when(tarefaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        tarefaService.criar(criarRequest("Titulo", LocalDate.now()), logado);

        verify(entityManager).getReference(Usuario.class, 1);
    }

    @Test
    @DisplayName("gerente atribui tarefa para vendedor da propria equipe")
    void gerenteAtribuiTarefaParaVendedorDaEquipe() {
        when(escopoCarteira.vendedoresVisiveis()).thenReturn(Optional.of(List.of(1, 2)));

        Usuario vendedorDaEquipe = new Usuario();
        vendedorDaEquipe.setIdUsuario(2);
        when(entityManager.find(Usuario.class, 2)).thenReturn(vendedorDaEquipe);
        when(tarefaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CriarTarefaRequest request = new CriarTarefaRequest("Titulo", null, LocalDate.now(), null, null, 2);
        tarefaService.criar(request, logado);

        verify(entityManager).find(Usuario.class, 2);
    }

    @Test
    @DisplayName("vendedor nao consegue atribuir tarefa para outro usuario fora do escopo")
    void vendedorNaoAtribuiTarefaParaOutro() {
        when(escopoCarteira.vendedoresVisiveis()).thenReturn(Optional.of(List.of(1)));

        CriarTarefaRequest request = new CriarTarefaRequest("Titulo", null, LocalDate.now(), null, null, 99);

        assertThatThrownBy(() -> tarefaService.criar(request, logado))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }

    // ---- RN-02: concluir / reabrir / editar ----

    @Test
    @DisplayName("concluir grava concluida true e a data de conclusao")
    void concluirGravaDataDeConclusao() {
        Tarefa tarefa = novaTarefa(1);
        when(tarefaRepository.findById(10)).thenReturn(Optional.of(tarefa));
        when(escopoCarteira.vendedoresVisiveis()).thenReturn(Optional.empty());
        when(tarefaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        TarefaResponse resposta = tarefaService.concluir(10);

        assertThat(resposta.concluida()).isTrue();
        assertThat(resposta.situacao()).isEqualTo(SituacaoTarefa.CONCLUIDA);
        assertThat(tarefa.getConcluidaEm()).isNotNull();
    }

    @Test
    @DisplayName("reabrir limpa a data de conclusao e volta concluida para false")
    void reabrirLimpaDataDeConclusao() {
        Tarefa tarefa = novaTarefa(1);
        tarefa.setConcluida(true);
        tarefa.setConcluidaEm(java.time.LocalDateTime.now());
        when(tarefaRepository.findById(10)).thenReturn(Optional.of(tarefa));
        when(escopoCarteira.vendedoresVisiveis()).thenReturn(Optional.empty());
        when(tarefaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        TarefaResponse resposta = tarefaService.reabrir(10);

        assertThat(resposta.concluida()).isFalse();
        assertThat(resposta.concluidaEm()).isNull();
    }

    @Test
    @DisplayName("concluir tarefa ja concluida gera conflito")
    void concluirTarefaJaConcluidaConflita() {
        Tarefa tarefa = novaTarefa(1);
        tarefa.setConcluida(true);
        when(tarefaRepository.findById(10)).thenReturn(Optional.of(tarefa));
        when(escopoCarteira.vendedoresVisiveis()).thenReturn(Optional.empty());

        assertThatThrownBy(() -> tarefaService.concluir(10)).isInstanceOf(ConflitoException.class);
    }

    @Test
    @DisplayName("reabrir tarefa aberta gera conflito")
    void reabrirTarefaAbertaConflita() {
        Tarefa tarefa = novaTarefa(1);
        when(tarefaRepository.findById(10)).thenReturn(Optional.of(tarefa));
        when(escopoCarteira.vendedoresVisiveis()).thenReturn(Optional.empty());

        assertThatThrownBy(() -> tarefaService.reabrir(10)).isInstanceOf(ConflitoException.class);
    }

    @Test
    @DisplayName("editar nao mexe na conclusao da tarefa")
    void editarNaoMexeNaConclusao() {
        Tarefa tarefa = novaTarefa(1);
        tarefa.setConcluida(true);
        tarefa.setConcluidaEm(java.time.LocalDateTime.now());
        when(tarefaRepository.findById(10)).thenReturn(Optional.of(tarefa));
        when(escopoCarteira.vendedoresVisiveis()).thenReturn(Optional.empty());
        when(tarefaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        EditarTarefaRequest request = new EditarTarefaRequest("Novo titulo", "Nova descricao", LocalDate.now().plusDays(5));
        TarefaResponse resposta = tarefaService.editar(10, request);

        assertThat(resposta.titulo()).isEqualTo("Novo titulo");
        assertThat(resposta.concluida()).isTrue();
        assertThat(resposta.concluidaEm()).isNotNull();
    }

    // ---- Escopo ----

    @Test
    @DisplayName("vendedor nao enxerga tarefa de outro vendedor")
    void vendedorNaoEnxergaTarefaDeOutro() {
        Tarefa tarefa = novaTarefa(2);
        when(tarefaRepository.findById(10)).thenReturn(Optional.of(tarefa));
        when(escopoCarteira.vendedoresVisiveis()).thenReturn(Optional.of(List.of(1)));

        assertThatThrownBy(() -> tarefaService.concluir(10)).isInstanceOf(RecursoNaoEncontradoException.class);
    }

    // ---- RN-03: listagem ----

    @SuppressWarnings("unchecked")
    private Page<Tarefa> paginaVazia() {
        return new PageImpl<>(List.of());
    }

    @Test
    @DisplayName("listagem de admin nao filtra por vendedores")
    void listagemDeAdminNaoFiltraVendedores() {
        when(escopoCarteira.vendedoresVisiveis()).thenReturn(Optional.empty());
        when(tarefaRepository.buscar(isNull(), any(), any(), any(), any())).thenReturn(paginaVazia());

        tarefaService.listar(null, null, null, 0);

        verify(tarefaRepository).buscar(isNull(), any(), any(), any(), any());
        verify(tarefaRepository, never()).buscarDosVendedores(any(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("listagem de vendedor filtra pela carteira")
    void listagemDeVendedorFiltraPelaCarteira() {
        when(escopoCarteira.vendedoresVisiveis()).thenReturn(Optional.of(List.of(1)));
        when(tarefaRepository.buscarDosVendedores(any(), any(), any(), any(), eq(List.of(1)), any()))
                .thenReturn(paginaVazia());

        tarefaService.listar(null, null, null, 0);

        verify(tarefaRepository).buscarDosVendedores(any(), any(), any(), any(), eq(List.of(1)), any());
    }

    @Test
    @DisplayName("filtro faixa vencida traduz para data anterior a hoje")
    void filtroFaixaVencidaTraduzParaDataAnteriorAHoje() {
        when(escopoCarteira.vendedoresVisiveis()).thenReturn(Optional.empty());
        ArgumentCaptor<Boolean> concluidaCaptor = ArgumentCaptor.forClass(Boolean.class);
        ArgumentCaptor<LocalDate> ateCaptor = ArgumentCaptor.forClass(LocalDate.class);
        when(tarefaRepository.buscar(isNull(), concluidaCaptor.capture(), isNull(), ateCaptor.capture(), any()))
                .thenReturn(paginaVazia());

        tarefaService.listar(null, FaixaTarefa.VENCIDA, null, 0);

        assertThat(concluidaCaptor.getValue()).isFalse();
        assertThat(ateCaptor.getValue()).isEqualTo(LocalDate.now().minusDays(1));
    }

    @Test
    @DisplayName("filtro faixa d1 traduz para amanha")
    void filtroFaixaD1TraduzParaAmanha() {
        when(escopoCarteira.vendedoresVisiveis()).thenReturn(Optional.empty());
        ArgumentCaptor<LocalDate> deCaptor = ArgumentCaptor.forClass(LocalDate.class);
        ArgumentCaptor<LocalDate> ateCaptor = ArgumentCaptor.forClass(LocalDate.class);
        when(tarefaRepository.buscar(isNull(), any(), deCaptor.capture(), ateCaptor.capture(), any()))
                .thenReturn(paginaVazia());

        tarefaService.listar(null, FaixaTarefa.D1, null, 0);

        assertThat(deCaptor.getValue()).isEqualTo(LocalDate.now().plusDays(1));
        assertThat(ateCaptor.getValue()).isEqualTo(LocalDate.now().plusDays(1));
    }

    @Test
    @DisplayName("situacao e faixa juntas falham com regra de negocio")
    void situacaoEFaixaJuntasFalham() {
        assertThatThrownBy(() -> tarefaService.listar(SituacaoTarefa.PENDENTE, FaixaTarefa.D1, null, 0))
                .isInstanceOf(RegraNegocioException.class);
    }

    // ---- Historico ----

    @Test
    @DisplayName("historico do cliente mapeia tarefas com tipo tarefa")
    void historicoDoClienteMapeiaTarefasComTipoTarefa() {
        Tarefa tarefa = novaTarefa(1);
        when(tarefaRepository.listarDoCliente(7)).thenReturn(List.of(tarefa));

        List<HistoricoItem> itens = tarefaService.historicoDoCliente(7);

        assertThat(itens).hasSize(1);
        assertThat(itens.get(0).titulo()).isEqualTo("Ligar para cliente");
    }

    // ---- Alertas (dashboard) ----

    @Test
    @DisplayName("alertas do admin nao filtra por vendedores")
    void alertasDoAdminNaoFiltraPorVendedores() {
        when(tarefaRepository.contarAbertasNaJanela(any(), any())).thenReturn(2L);

        AlertasTarefas alertas = tarefaService.alertas(null);

        assertThat(alertas.vencidas()).isEqualTo(2L);
        verify(tarefaRepository, never()).contarAbertasNaJanelaDosVendedores(any(), any(), any());
    }
}

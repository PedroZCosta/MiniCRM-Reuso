package com.miniCRM.miniCRM.service;

import com.miniCRM.miniCRM.dto.cliente.ClienteResumo;
import com.miniCRM.miniCRM.exception.RecursoNaoEncontradoException;
import com.miniCRM.miniCRM.exception.RegraNegocioException;
import com.miniCRM.miniCRM.model.Cliente;
import com.miniCRM.miniCRM.model.Usuario;
import com.miniCRM.miniCRM.model.enums.PerfilUsuario;
import com.miniCRM.miniCRM.model.enums.StatusCliente;
import com.miniCRM.miniCRM.repository.ClienteRepository;
import com.miniCRM.miniCRM.repository.ClienteSpecs;
import com.miniCRM.miniCRM.repository.UsuarioRepository;
import com.miniCRM.miniCRM.security.EscopoCarteira;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClienteServiceTest {

    @Mock
    private ClienteRepository clienteRepository;
    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private EscopoCarteira escopoCarteira;

    @InjectMocks
    private ClienteService clienteService;

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("busca 'hori' encontra por nome e por empresa, ignorando maiusculas e minusculas")
    void buscaLivreEncontraPorNomeEEmpresaCaseInsensitive() {
        Specification<Cliente> spec = ClienteSpecs.buscaLivre("HORI");

        Root<Cliente> root = mock(Root.class);
        CriteriaQuery<?> query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        Path nomePath = mock(Path.class);
        Path empresaPath = mock(Path.class);
        Expression nomeLower = mock(Expression.class);
        Expression empresaLower = mock(Expression.class);
        Predicate likeNome = mock(Predicate.class);
        Predicate likeEmpresa = mock(Predicate.class);
        Predicate resultadoEsperado = mock(Predicate.class);

        when(root.get("nome")).thenReturn(nomePath);
        when(root.get("empresa")).thenReturn(empresaPath);
        when(cb.lower(nomePath)).thenReturn(nomeLower);
        when(cb.lower(empresaPath)).thenReturn(empresaLower);
        when(cb.like(nomeLower, "%hori%")).thenReturn(likeNome);
        when(cb.like(empresaLower, "%hori%")).thenReturn(likeEmpresa);
        when(cb.or(likeNome, likeEmpresa)).thenReturn(resultadoEsperado);

        Predicate resultado = spec.toPredicate(root, query, cb);

        assertThat(resultado).isSameAs(resultadoEsperado);
    }

    @Test
    @DisplayName("vendedor nao enxerga cliente de outro vendedor")
    void vendedorNaoVeClienteDeOutroVendedor() {
        Usuario outroVendedor = new Usuario();
        outroVendedor.setIdUsuario(2);
        Cliente cliente = new Cliente();
        cliente.setIdCliente(10);
        cliente.setVendedor(outroVendedor);

        when(clienteRepository.findById(10)).thenReturn(Optional.of(cliente));
        when(escopoCarteira.vendedoresVisiveis()).thenReturn(Optional.of(List.of(1)));

        assertThatThrownBy(() -> clienteService.buscarPorId(10))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }

    @Test
    @DisplayName("excluir seta excluido=true e status=INATIVO")
    void excluirMarcaClienteComoExcluidoEInativo() {
        Usuario vendedor = new Usuario();
        vendedor.setIdUsuario(1);
        Cliente cliente = new Cliente();
        cliente.setIdCliente(5);
        cliente.setStatus(StatusCliente.ATIVO);
        cliente.setExcluido(false);
        cliente.setVendedor(vendedor);

        when(clienteRepository.findById(5)).thenReturn(Optional.of(cliente));
        when(escopoCarteira.vendedoresVisiveis()).thenReturn(Optional.empty());
        when(clienteRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        clienteService.excluir(5);

        assertThat(cliente.getExcluido()).isTrue();
        assertThat(cliente.getStatus()).isEqualTo(StatusCliente.INATIVO);
    }

    @Test
    @DisplayName("transferir para vendedor desativado da erro")
    void transferirParaVendedorDesativadoDaErro() {
        Usuario vendedorAtual = new Usuario();
        vendedorAtual.setIdUsuario(1);
        Cliente cliente = new Cliente();
        cliente.setIdCliente(7);
        cliente.setVendedor(vendedorAtual);

        Usuario gerenteLogado = new Usuario();
        gerenteLogado.setIdUsuario(99);
        gerenteLogado.setPerfil(PerfilUsuario.GERENTE);

        Usuario vendedorDesativado = new Usuario();
        vendedorDesativado.setIdUsuario(2);
        vendedorDesativado.setAtivo(false);

        when(usuarioRepository.findById(99)).thenReturn(Optional.of(gerenteLogado));
        when(clienteRepository.findById(7)).thenReturn(Optional.of(cliente));
        when(escopoCarteira.vendedoresVisiveis()).thenReturn(Optional.empty());
        when(usuarioRepository.findById(2)).thenReturn(Optional.of(vendedorDesativado));

        assertThatThrownBy(() -> clienteService.transferir(7, 2, 99))
                .isInstanceOf(RegraNegocioException.class);
    }

    @Test
    @DisplayName("buscarAtivo retorna ClienteResumo do cliente nao excluido")
    void buscarAtivoRetornaResumoDoClienteNaoExcluido() {
        Cliente cliente = new Cliente();
        cliente.setIdCliente(8);
        cliente.setNome("Construtora Horizonte");
        cliente.setEmail("contato@horizonte.com");
        cliente.setEmpresa("Horizonte Ltda");
        cliente.setStatus(StatusCliente.ATIVO);
        cliente.setExcluido(false);

        when(clienteRepository.findById(8)).thenReturn(Optional.of(cliente));

        ClienteResumo resumo = clienteService.buscarAtivo(8);

        assertThat(resumo).isEqualTo(new ClienteResumo(8, "Construtora Horizonte",
                "contato@horizonte.com", "Horizonte Ltda", StatusCliente.ATIVO));
    }

    @Test
    @DisplayName("buscarAtivo em cliente excluido da erro de regra de negocio")
    void buscarAtivoEmClienteExcluidoDaErro() {
        Cliente cliente = new Cliente();
        cliente.setIdCliente(9);
        cliente.setExcluido(true);

        when(clienteRepository.findById(9)).thenReturn(Optional.of(cliente));

        assertThatThrownBy(() -> clienteService.buscarAtivo(9))
                .isInstanceOf(RegraNegocioException.class);
    }

    @Test
    @DisplayName("buscarAtivo em id inexistente da 404")
    void buscarAtivoEmIdInexistenteDa404() {
        when(clienteRepository.findById(11)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clienteService.buscarAtivo(11))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }
}
